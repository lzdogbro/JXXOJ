package top.hcode.hoj.manager.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.pk.PkMatchEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.dao.user.UserRecordEntityService;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.pk.PkMatch;
import top.hcode.hoj.pojo.entity.user.UserInfo;
import top.hcode.hoj.pojo.vo.PkMatchVO;
import top.hcode.hoj.pojo.vo.PkScoreVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: PK对战业务管理
 */
@Component
public class PkManager {

    @Resource
    private PkMatchEntityService pkMatchEntityService;

    @Resource
    private UserRecordEntityService userRecordEntityService;

    @Resource
    private JudgeEntityService judgeEntityService;

    @Resource
    private ProblemEntityService problemEntityService;

    @Resource
    private UserInfoEntityService userInfoEntityService;

    /**
     * 获取当前登录用户
     */
    private AccountProfile getCurrentUser() {
        return (AccountProfile) SecurityUtils.getSubject().getPrincipal();
    }

    /**
     * 发起PK邀请
     */
    @Transactional
    public Long createPkInvite(String problemId, String opponentUid) throws StatusFailException {
        AccountProfile currentUser = getCurrentUser();

        if (opponentUid.equals(currentUser.getUid())) {
            throw new StatusFailException("不能与自己进行PK对战");
        }

        // 校验题目存在（按problem_id展示ID查询）
        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.eq("problem_id", problemId);
        Problem problem = problemEntityService.getOne(problemQueryWrapper);
        if (problem == null) {
            throw new StatusFailException("题目不存在");
        }

        // 校验当前用户没有活跃PK
        List<PkMatchVO> myActive = pkMatchEntityService.getActivePkMatchByUserId(currentUser.getUid());
        if (myActive != null && !myActive.isEmpty()) {
            throw new StatusFailException("你已有一场进行中或等待中的PK对战，请先结束");
        }

        // 校验对手没有活跃PK
        List<PkMatchVO> opponentActive = pkMatchEntityService.getActivePkMatchByUserId(opponentUid);
        if (opponentActive != null && !opponentActive.isEmpty()) {
            throw new StatusFailException("对手已有一场进行中或等待中的PK对战");
        }

        // 校验对手存在
        UserInfo opponent = userInfoEntityService.getById(opponentUid);
        if (opponent == null) {
            throw new StatusFailException("对手用户不存在");
        }

        PkMatch pkMatch = new PkMatch();
        pkMatch.setProblemId(problem.getId())
                .setInitiatorUid(currentUser.getUid())
                .setOpponentUid(opponentUid)
                .setStatus(0);

        pkMatchEntityService.save(pkMatch);
        return pkMatch.getId();
    }

    /**
     * 响应PK邀请（接受或拒绝）
     */
    @Transactional
    public PkMatchVO respondToPkInvite(Long matchId, boolean accept) throws StatusFailException {
        AccountProfile currentUser = getCurrentUser();

        PkMatchVO matchVO = pkMatchEntityService.getPkMatchById(matchId);
        if (matchVO == null) {
            throw new StatusFailException("PK对战不存在");
        }

        if (matchVO.getStatus() != 0) {
            throw new StatusFailException("该PK邀请已失效");
        }

        if (!currentUser.getUid().equals(matchVO.getOpponentUid())) {
            throw new StatusFailException("你不是该邀请的接收者");
        }

        PkMatch pkMatch = pkMatchEntityService.getById(matchId);
        if (accept) {
            pkMatch.setStatus(1);
            pkMatch.setStartTime(new Date());
        } else {
            pkMatch.setStatus(5);
        }
        pkMatchEntityService.updateById(pkMatch);

        return pkMatchEntityService.getPkMatchById(matchId);
    }

    /**
     * 取消PK邀请（仅发起者可取消待接受的邀请）
     */
    @Transactional
    public void cancelPkInvite(Long matchId) throws StatusFailException {
        AccountProfile currentUser = getCurrentUser();

        PkMatchVO matchVO = pkMatchEntityService.getPkMatchById(matchId);
        if (matchVO == null) {
            throw new StatusFailException("PK对战不存在");
        }

        if (matchVO.getStatus() != 0) {
            throw new StatusFailException("只能取消待接受的PK邀请");
        }

        if (!currentUser.getUid().equals(matchVO.getInitiatorUid())) {
            throw new StatusFailException("只有邀请发起者才能取消");
        }

        PkMatch pkMatch = pkMatchEntityService.getById(matchId);
        pkMatch.setStatus(6);
        pkMatchEntityService.updateById(pkMatch);
    }

    /**
     * 投降
     */
    @Transactional
    public PkMatchVO surrenderPk(Long matchId) throws StatusFailException {
        AccountProfile currentUser = getCurrentUser();
        String myUid = currentUser.getUid();

        PkMatchVO matchVO = pkMatchEntityService.getPkMatchById(matchId);
        if (matchVO == null) {
            throw new StatusFailException("PK对战不存在");
        }

        if (matchVO.getStatus() != 1) {
            throw new StatusFailException("该PK对战不在进行中");
        }

        if (!myUid.equals(matchVO.getInitiatorUid()) && !myUid.equals(matchVO.getOpponentUid())) {
            throw new StatusFailException("你不是该PK对战的参与者");
        }

        PkMatch pkMatch = pkMatchEntityService.getById(matchId);
        pkMatch.setSurrenderUid(myUid);
        pkMatch.setEndTime(new Date());

        // 确定胜者
        String winnerUid;
        if (myUid.equals(matchVO.getInitiatorUid())) {
            // 发起者投降，对手胜
            winnerUid = matchVO.getOpponentUid();
            pkMatch.setStatus(3); // 对手胜
        } else {
            // 对手投降，发起者胜
            winnerUid = matchVO.getInitiatorUid();
            pkMatch.setStatus(2); // 发起者胜
        }
        pkMatch.setWinnerUid(winnerUid);
        pkMatchEntityService.updateById(pkMatch);

        // 更新积分: 胜者+10, 投降者-2
        updatePkScores(matchVO.getInitiatorUid(), matchVO.getOpponentUid(), winnerUid);

        PkMatchVO result = pkMatchEntityService.getPkMatchById(matchId);
        computeScoreChanges(result);
        return result;
    }

    /**
     * 轮询获取PK对战状态（含AC检测和超时检测）
     */
    @Transactional
    public PkMatchVO getPkMatchStatus(Long matchId) throws StatusFailException {
        AccountProfile currentUser = getCurrentUser();
        String myUid = currentUser.getUid();

        PkMatchVO matchVO = pkMatchEntityService.getPkMatchById(matchId);
        if (matchVO == null) {
            throw new StatusFailException("PK对战不存在");
        }

        if (!myUid.equals(matchVO.getInitiatorUid()) && !myUid.equals(matchVO.getOpponentUid())) {
            throw new StatusFailException("你不是该PK对战的参与者");
        }

        // 计算剩余秒数
        if (matchVO.getStatus() == 1 && matchVO.getStartTime() != null) {
            long elapsed = System.currentTimeMillis() - matchVO.getStartTime().getTime();
            long remaining = Math.max(0, 1200 - elapsed / 1000);
            matchVO.setRemainingSeconds(remaining);

            // 检查超时
            if (remaining <= 0) {
                // 超时转平局
                PkMatch pkMatch = pkMatchEntityService.getById(matchId);
                pkMatch.setStatus(4);
                pkMatch.setEndTime(new Date(matchVO.getStartTime().getTime() + 1200 * 1000));
                pkMatchEntityService.updateById(pkMatch);

                matchVO.setStatus(4);
                matchVO.setRemainingSeconds(0L);

                // 平局不更新积分（delta=0）
                PkMatchVO refreshed = pkMatchEntityService.getPkMatchById(matchId);
                computeScoreChanges(refreshed);
                return refreshed;
            }

            // 检查AC（仅在进行中的对战中检测）
            PkMatchVO acResult = checkAcAndEndMatch(matchVO);
            if (acResult != null) {
                return acResult;
            }

            matchVO.setRemainingSeconds(remaining);
        } else if (matchVO.getStatus() == 0) {
            matchVO.setRemainingSeconds(null);
        } else {
            // 已结束的对战
            matchVO.setRemainingSeconds(0L);
            computeScoreChanges(matchVO);
        }

        return matchVO;
    }

    /**
     * 检查是否有参与者AC，若有则结束对战
     */
    private PkMatchVO checkAcAndEndMatch(PkMatchVO matchVO) {
        // 查询对战开始后双方在本题目的AC提交
        QueryWrapper<Judge> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", matchVO.getProblemId())
                .in("uid", Arrays.asList(matchVO.getInitiatorUid(), matchVO.getOpponentUid()))
                .eq("status", Constants.Judge.STATUS_ACCEPTED.getStatus())
                .gt("submit_time", matchVO.getStartTime())
                .orderByAsc("submit_time")
                .last("LIMIT 1");

        Judge acJudge = judgeEntityService.getOne(queryWrapper, false);

        if (acJudge != null) {
            PkMatch pkMatch = pkMatchEntityService.getById(matchVO.getId());
            String winnerUid = acJudge.getUid();
            pkMatch.setWinnerUid(winnerUid);
            pkMatch.setEndTime(new Date());

            if (winnerUid.equals(matchVO.getInitiatorUid())) {
                pkMatch.setStatus(2); // 发起者胜
                pkMatch.setInitiatorSubmitId(acJudge.getSubmitId());
            } else {
                pkMatch.setStatus(3); // 对手胜
                pkMatch.setOpponentSubmitId(acJudge.getSubmitId());
            }

            pkMatchEntityService.updateById(pkMatch);

            // 更新积分
            updatePkScores(matchVO.getInitiatorUid(), matchVO.getOpponentUid(), winnerUid);

            PkMatchVO result = pkMatchEntityService.getPkMatchById(matchVO.getId());
            result.setRemainingSeconds(
                    Math.max(0, 1200 - (System.currentTimeMillis() - matchVO.getStartTime().getTime()) / 1000)
            );
            computeScoreChanges(result);
            return result;
        }

        return null;
    }

    /**
     * 更新双方PK积分
     * 胜者+10，败者-2
     */
    private void updatePkScores(String initiatorUid, String opponentUid, String winnerUid) {
        if (winnerUid == null) {
            // 平局，积分不变
            return;
        }

        String loserUid = winnerUid.equals(initiatorUid) ? opponentUid : initiatorUid;
        userRecordEntityService.updatePkScore(winnerUid, 10);
        userRecordEntityService.updatePkScore(loserUid, -2);
    }

    /**
     * 计算比分变化，填充到VO中
     */
    private void computeScoreChanges(PkMatchVO matchVO) {
        if (matchVO.getStatus() == 2) {
            // 发起者胜
            matchVO.setInitiatorScoreChange(10);
            matchVO.setOpponentScoreChange(-2);
        } else if (matchVO.getStatus() == 3) {
            // 对手胜
            matchVO.setInitiatorScoreChange(-2);
            matchVO.setOpponentScoreChange(10);
        } else if (matchVO.getStatus() == 4) {
            // 平局
            matchVO.setInitiatorScoreChange(0);
            matchVO.setOpponentScoreChange(0);
        }
    }

    /**
     * 获取当前用户活跃的PK对战
     */
    public PkMatchVO getMyActivePkMatch() {
        AccountProfile currentUser = getCurrentUser();
        List<PkMatchVO> list = pkMatchEntityService.getActivePkMatchByUserId(currentUser.getUid());
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /**
     * 获取当前用户待处理的邀请
     */
    public List<PkMatchVO> getMyPendingInvites() {
        AccountProfile currentUser = getCurrentUser();
        return pkMatchEntityService.getPendingPkMatchByUserId(currentUser.getUid());
    }

    /**
     * 获取PK积分
     */
    public PkScoreVO getPkScore(String uid) {
        AccountProfile currentUser = getCurrentUser();
        String queryUid = (uid != null && !uid.isEmpty()) ? uid : currentUser.getUid();

        UserInfo userInfo = userInfoEntityService.getById(queryUid);
        if (userInfo == null) {
            return null;
        }

        PkScoreVO vo = new PkScoreVO();
        vo.setUid(queryUid);
        vo.setUsername(userInfo.getUsername());
        vo.setNickname(userInfo.getNickname());

        // 获取PK积分和统计
        QueryWrapper<PkMatch> totalQuery = new QueryWrapper<>();
        totalQuery.and(w -> w.eq("initiator_uid", queryUid).or().eq("opponent_uid", queryUid));
        int totalMatches = pkMatchEntityService.count(totalQuery);

        QueryWrapper<PkMatch> winQuery = new QueryWrapper<>();
        winQuery.eq("winner_uid", queryUid);
        int wins = pkMatchEntityService.count(winQuery);

        QueryWrapper<PkMatch> lossQuery = new QueryWrapper<>();
        lossQuery.and(w -> w.eq("initiator_uid", queryUid).or().eq("opponent_uid", queryUid))
                .in("status", Arrays.asList(2, 3))
                .ne("winner_uid", queryUid);
        int losses = pkMatchEntityService.count(lossQuery);

        QueryWrapper<PkMatch> drawQuery = new QueryWrapper<>();
        drawQuery.and(w -> w.eq("initiator_uid", queryUid).or().eq("opponent_uid", queryUid))
                .eq("status", 4);
        int draws = pkMatchEntityService.count(drawQuery);

        vo.setTotalMatches(totalMatches);
        vo.setWins(wins);
        vo.setLosses(losses);
        vo.setDraws(draws);

        // 从user_record读取pk_score
        vo.setPkScore(getCurrentPkScore(queryUid));

        return vo;
    }

    /**
     * 获取用户的当前PK积分
     */
    private Integer getCurrentPkScore(String uid) {
        top.hcode.hoj.pojo.entity.user.UserRecord userRecord = userRecordEntityService.getOne(
                new QueryWrapper<top.hcode.hoj.pojo.entity.user.UserRecord>().eq("uid", uid)
        );
        return userRecord != null && userRecord.getPkScore() != null ? userRecord.getPkScore() : 0;
    }

    /**
     * 搜索用户（用于邀请时搜索对手）
     */
    public List<Map<String, Object>> searchUsers(String keyword, Integer limit) {
        AccountProfile currentUser = getCurrentUser();
        if (limit == null || limit < 1) limit = 10;

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .ne("uuid", currentUser.getUid())
                .and(w -> w.like("username", keyword).or().like("nickname", keyword))
                .last("LIMIT " + limit);

        List<UserInfo> userList = userInfoEntityService.list(queryWrapper);

        return userList.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("uid", user.getUuid());
            map.put("username", user.getUsername());
            map.put("nickname", user.getNickname());
            map.put("avatar", user.getAvatar());
            map.put("pkScore", getCurrentPkScore(user.getUuid()));
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 获取PK历史
     */
    public IPage<PkMatchVO> getPkHistory(Integer limit, Integer currentPage) {
        AccountProfile currentUser = getCurrentUser();
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 20;

        Page<PkMatchVO> page = new Page<>(currentPage, limit);
        return pkMatchEntityService.getPkHistory(page, currentUser.getUid());
    }
}
