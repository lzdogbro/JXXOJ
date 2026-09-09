package top.hcode.hoj.manager.wechat;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.config.WechatProperties;
import top.hcode.hoj.dao.assignment.AssignmentEntityService;
import top.hcode.hoj.dao.assignment.AssignmentProblemEntityService;
import top.hcode.hoj.dao.assignment.AssignmentStudentEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.dao.wechat.ParentBindingEntityService;
import top.hcode.hoj.dao.wechat.WechatUserEntityService;
import top.hcode.hoj.pojo.dto.WechatBindDTO;
import top.hcode.hoj.pojo.dto.WechatLoginDTO;
import top.hcode.hoj.pojo.dto.WechatSubscribeDTO;
import top.hcode.hoj.pojo.entity.assignment.Assignment;
import top.hcode.hoj.pojo.entity.assignment.AssignmentStudent;
import top.hcode.hoj.pojo.entity.assignment.ParentBinding;
import top.hcode.hoj.pojo.entity.assignment.WechatUser;
import top.hcode.hoj.pojo.entity.user.UserInfo;
import top.hcode.hoj.pojo.vo.AssignmentProblemVO;
import top.hcode.hoj.pojo.vo.AssignmentVO;
import top.hcode.hoj.pojo.vo.ChildVO;
import top.hcode.hoj.pojo.vo.WechatAssignmentDetailVO;
import top.hcode.hoj.pojo.vo.WechatLoginVO;
import top.hcode.hoj.pojo.vo.WechatProblemVO;
import top.hcode.hoj.pojo.vo.WechatSubmitVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.shiro.WechatProfile;
import top.hcode.hoj.service.wechat.WechatApiClient;
import top.hcode.hoj.service.wechat.WechatMessageService;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.JwtUtils;
import top.hcode.hoj.utils.RedisUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 微信家长侧业务 Manager（登录 / 绑定 / 孩子作业）
 *
 * 与平台 AssignmentManager 复用同一套 EntityService（getMyAssignmentList 已按 uid 过滤），
 * 仅时间窗 / 完成判定 / 三态题目状态在此独立实现（家长视角）。
 */
@Component
@Slf4j(topic = "hoj")
public class WechatManager {

    /**
     * 绑定码字符集：去掉易混淆 0/O、1/I/l，仅大写字母 + 数字
     */
    private static final char[] CODE_ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();

    private static final long BIND_CODE_EXPIRE_SECONDS = 600L;

    private static final int BIND_CODE_MAX_ATTEMPT = 5;

    private static final String QRCODE_KEY_PREFIX = "wechat:qrcode:";

    private static final String QR_PAGE = "pages/bind/bind";

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private WechatApiClient wechatApiClient;

    @Resource
    private WechatMessageService wechatMessageService;

    @Resource
    private WechatProperties wechatProperties;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private WechatUserEntityService wechatUserEntityService;

    @Resource
    private ParentBindingEntityService parentBindingEntityService;

    @Resource
    private UserInfoEntityService userInfoEntityService;

    @Resource
    private AssignmentEntityService assignmentEntityService;

    @Resource
    private AssignmentProblemEntityService assignmentProblemEntityService;

    @Resource
    private AssignmentStudentEntityService assignmentStudentEntityService;

    /**
     * 微信登录：code 换 openid → upsert 微信身份 → 签发微信 token
     */
    public WechatLoginVO login(WechatLoginDTO dto) throws StatusFailException {
        String openid = wechatApiClient.code2session(dto.getCode());

        WechatUser existing = wechatUserEntityService.getById(openid);
        if (existing == null) {
            wechatUserEntityService.save(new WechatUser()
                    .setOpenid(openid)
                    .setNickname(dto.getNickname())
                    .setAvatar(dto.getAvatar()));
        } else if (StrUtil.isNotBlank(dto.getNickname()) || StrUtil.isNotBlank(dto.getAvatar())) {
            existing.setNickname(dto.getNickname()).setAvatar(dto.getAvatar());
            wechatUserEntityService.updateById(existing);
        }

        String token = jwtUtils.generateWechatToken(openid);
        long childCount = parentBindingEntityService.count(new QueryWrapper<ParentBinding>()
                .eq("parent_openid", openid).eq("status", 1));

        WechatLoginVO vo = new WechatLoginVO();
        vo.setToken(token);
        vo.setOpenid(openid);
        vo.setHasChild(childCount > 0);
        return vo;
    }

    /**
     * 生成一次性绑定码（平台学生登录态）
     */
    public Map<String, Object> bindCode() throws StatusFailException {
        AccountProfile profile = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        String uid = profile.getUid();
        String code = generateUniqueCode();

        parentBindingEntityService.save(new ParentBinding()
                .setParentOpenid(null)
                .setStudentUid(uid)
                .setBindCode(code)
                .setStatus(0)
                .setGmtExpire(new Date(System.currentTimeMillis() + BIND_CODE_EXPIRE_SECONDS * 1000))
                .setAttemptCount(0));

        Map<String, Object> result = new HashMap<>(3);
        result.put("bindCode", code);
        result.put("expireIn", BIND_CODE_EXPIRE_SECONDS);
        // 生成小程序码（扫码绑定）；失败不影响主流程，降级为手输绑定码
        String qrCodeUrl = null;
        try {
            byte[] qrBytes = wechatApiClient.getWxaCodeUnlimited(code, QR_PAGE);
            if (qrBytes != null && qrBytes.length > 0) {
                redisUtils.set(QRCODE_KEY_PREFIX + code,
                        Base64.getEncoder().encodeToString(qrBytes), BIND_CODE_EXPIRE_SECONDS);
                qrCodeUrl = "/api/wechat/qrcode/" + code;
            }
        } catch (StatusFailException e) {
            log.warn("[wechat] 生成绑定码小程序码失败，降级为手输: {}", e.getMessage());
        }
        result.put("qrCodeUrl", qrCodeUrl);
        return result;
    }

    /**
     * 获取绑定码对应的小程序码图片（无认证），仅 status=0 且未过期时返回
     */
    public byte[] getQrcode(String bindCode) {
        ParentBinding binding = parentBindingEntityService.getOne(new QueryWrapper<ParentBinding>()
                .eq("bind_code", bindCode).eq("status", 0).last("LIMIT 1"), false);
        if (binding == null || binding.getGmtExpire() == null || binding.getGmtExpire().before(new Date())) {
            return null;
        }
        Object cached = redisUtils.get(QRCODE_KEY_PREFIX + bindCode);
        if (cached == null) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(cached.toString());
        } catch (Exception e) {
            log.warn("[wechat] 小程序码缓存解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 家长绑定：绑定码 + 学号 + 姓名
     */
    public ChildVO bind(WechatBindDTO dto) throws StatusFailException {
        String openid = currentOpenid();

        ParentBinding binding = parentBindingEntityService.getOne(new QueryWrapper<ParentBinding>()
                .eq("bind_code", dto.getBindCode()).last("LIMIT 1"), false);
        if (binding == null) {
            long any = parentBindingEntityService.count(new QueryWrapper<ParentBinding>()
                    .eq("bind_code", dto.getBindCode()));
            throw new StatusFailException(any > 0 ? "绑定码已使用" : "绑定码无效");
        }
        if (binding.getStatus() == null || binding.getStatus() != 0) {
            throw new StatusFailException("绑定码已使用");
        }
        if (binding.getGmtExpire() == null || binding.getGmtExpire().before(new Date())) {
            binding.setStatus(3);
            parentBindingEntityService.updateById(binding);
            throw new StatusFailException("绑定码已过期，请重新生成");
        }

        // 校验失败累计尝试次数，超限烧毁
        int attempt = binding.getAttemptCount() == null ? 0 : binding.getAttemptCount() + 1;
        binding.setAttemptCount(attempt);
        parentBindingEntityService.updateById(binding);
        if (attempt > BIND_CODE_MAX_ATTEMPT) {
            binding.setStatus(3);
            parentBindingEntityService.updateById(binding);
            throw new StatusFailException("尝试次数超限，请重新生成");
        }

        String studentUid = binding.getStudentUid();
        UserInfo student = userInfoEntityService.getById(studentUid);
        if (student == null || !Objects.equals(student.getNumber(), dto.getNumber())
                || !Objects.equals(student.getRealname(), dto.getRealname())) {
            throw new StatusFailException("学号或姓名不匹配");
        }

        binding.setParentOpenid(openid);
        binding.setStatus(1);
        parentBindingEntityService.updateById(binding);

        return buildChildVO(student, new Date());
    }

    /**
     * 解绑
     */
    public void unbind(String studentUid) throws StatusNotFoundException {
        String openid = currentOpenid();
        ParentBinding binding = parentBindingEntityService.getOne(new QueryWrapper<ParentBinding>()
                .eq("parent_openid", openid).eq("student_uid", studentUid).eq("status", 1), false);
        if (binding == null) {
            throw new StatusNotFoundException("绑定关系不存在");
        }
        binding.setStatus(2);
        parentBindingEntityService.updateById(binding);
    }

    /**
     * 我的孩子列表
     */
    public List<ChildVO> childList() {
        String openid = currentOpenid();
        List<ParentBinding> bindings = parentBindingEntityService.list(new QueryWrapper<ParentBinding>()
                .eq("parent_openid", openid).eq("status", 1));
        List<ChildVO> result = new ArrayList<>();
        for (ParentBinding b : bindings) {
            UserInfo student = userInfoEntityService.getById(b.getStudentUid());
            if (student == null) {
                continue;
            }
            Date boundAt = b.getGmtModified() != null ? b.getGmtModified() : b.getGmtCreate();
            result.add(buildChildVO(student, boundAt));
        }
        return result;
    }

    /**
     * 孩子作业列表（复用 getMyAssignmentList 按 uid 过滤）
     */
    public IPage<AssignmentVO> childAssignments(String uid, Integer limit, Integer currentPage)
            throws StatusForbiddenException {
        checkBound(uid);
        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (limit == null || limit < 1) {
            limit = 50;
        }
        Page<AssignmentVO> page = new Page<>(currentPage, limit);
        IPage<AssignmentVO> result = assignmentEntityService.getMyAssignmentList(page, uid);
        for (AssignmentVO vo : result.getRecords()) {
            computeTimeStatus(vo);
            vo.setCompleted(isCompleted(vo));
        }
        return result;
    }

    /**
     * 孩子作业详情（含三态题目状态）
     */
    public WechatAssignmentDetailVO childAssignmentDetail(String uid, Long aid)
            throws StatusNotFoundException, StatusForbiddenException {
        checkBound(uid);

        Assignment assignment = assignmentEntityService.getById(aid);
        if (assignment == null || assignment.getIsDeleted() == 1) {
            throw new StatusNotFoundException("该作业不存在！");
        }
        if (assignment.getStatus() == null || assignment.getStatus() != 1) {
            throw new StatusForbiddenException("该作业尚未发布！");
        }
        long assigned = assignmentStudentEntityService.count(new QueryWrapper<AssignmentStudent>()
                .eq("aid", aid).eq("uid", uid));
        if (assigned == 0) {
            throw new StatusForbiddenException("该孩子未被布置该作业！");
        }

        WechatAssignmentDetailVO vo = new WechatAssignmentDetailVO();
        vo.setId(assignment.getId());
        vo.setTitle(assignment.getTitle());
        vo.setDescription(assignment.getDescription());
        vo.setIsRequired(assignment.getIsRequired());
        vo.setStartTime(assignment.getStartTime());
        vo.setEndTime(assignment.getEndTime());

        Date now = new Date();
        boolean ended = assignment.getEndTime() != null && now.after(assignment.getEndTime());
        boolean running = !ended && assignment.getStartTime() != null && now.after(assignment.getStartTime());
        vo.setIsEnded(ended);
        vo.setIsRunning(running);

        // 三态题目状态：AC / 已提交未AC / 未做（实时查 judge）
        List<AssignmentProblemVO> problemList = assignmentProblemEntityService.getAssignmentProblemList(aid);
        List<Long> acceptedPids = assignmentStudentEntityService.getAcceptedPidsByAidUid(
                aid, uid, Constants.Judge.STATUS_ACCEPTED.getStatus());
        Set<Long> acceptedSet = new HashSet<>(acceptedPids);

        Map<Long, Date> submitMap = new HashMap<>();
        List<WechatSubmitVO> submits = assignmentStudentEntityService.getSubmittedPidsByAidUid(aid, uid);
        for (WechatSubmitVO s : submits) {
            submitMap.put(s.getPid(), s.getSubmitTime());
        }

        List<WechatProblemVO> problems = new ArrayList<>();
        int acceptedCount = 0;
        for (AssignmentProblemVO p : problemList) {
            WechatProblemVO wp = new WechatProblemVO();
            wp.setPid(p.getPid());
            wp.setDisplayId(p.getDisplayId());
            wp.setTitle(p.getTitle());
            wp.setScore(p.getScore());
            boolean ac = acceptedSet.contains(p.getPid());
            boolean submitted = submitMap.containsKey(p.getPid());
            if (ac) {
                wp.setStatus("AC");
                acceptedCount++;
            } else if (submitted) {
                wp.setStatus("已提交未AC");
            } else {
                wp.setStatus("未做");
            }
            wp.setSubmitTime(submitMap.get(p.getPid()));
            problems.add(wp);
        }

        vo.setProblemCount(problems.size());
        vo.setAcceptedCount(acceptedCount);
        vo.setCompleted(problems.size() > 0 && acceptedCount >= problems.size());
        vo.setProblems(problems);
        return vo;
    }

    /**
     * 订阅授权上报：eventType → 配置模板 id → 配额 +1
     */
    public void subscribe(WechatSubscribeDTO dto) throws StatusFailException {
        String openid = currentOpenid();
        String eventType = dto.getEventType();
        String templateId;
        if ("assignment_publish".equals(eventType)) {
            templateId = wechatProperties.getTemplatePublish();
        } else if ("assignment_done".equals(eventType)) {
            templateId = wechatProperties.getTemplateDone();
        } else {
            throw new StatusFailException("参数非法");
        }
        if (StrUtil.isBlank(templateId)) {
            throw new StatusFailException("订阅模板未配置");
        }
        wechatMessageService.addQuota(openid, templateId);
    }

    private String currentOpenid() {
        WechatProfile profile = (WechatProfile) SecurityUtils.getSubject().getPrincipal();
        return profile.getOpenid();
    }

    private void checkBound(String uid) throws StatusForbiddenException {
        String openid = currentOpenid();
        long count = parentBindingEntityService.count(new QueryWrapper<ParentBinding>()
                .eq("parent_openid", openid).eq("student_uid", uid).eq("status", 1));
        if (count == 0) {
            throw new StatusForbiddenException("未绑定该孩子");
        }
    }

    private ChildVO buildChildVO(UserInfo student, Date boundAt) {
        ChildVO vo = new ChildVO();
        vo.setStudentUid(student.getUuid());
        vo.setUsername(student.getUsername());
        vo.setRealname(student.getRealname());
        vo.setNumber(student.getNumber());
        vo.setSchool(student.getSchool());
        vo.setBoundAt(boundAt);
        return vo;
    }

    private String generateUniqueCode() throws StatusFailException {
        for (int i = 0; i < 10; i++) {
            StringBuilder sb = new StringBuilder(6);
            for (int j = 0; j < 6; j++) {
                sb.append(CODE_ALPHABET[ThreadLocalRandom.current().nextInt(CODE_ALPHABET.length)]);
            }
            String code = sb.toString();
            long exists = parentBindingEntityService.count(new QueryWrapper<ParentBinding>()
                    .eq("bind_code", code).eq("status", 0));
            if (exists == 0) {
                return code;
            }
        }
        throw new StatusFailException("生成绑定码失败，请重试");
    }

    private void computeTimeStatus(AssignmentVO vo) {
        if (vo == null) {
            return;
        }
        Date now = new Date();
        boolean ended = vo.getEndTime() != null && now.after(vo.getEndTime());
        boolean running = !ended && vo.getStartTime() != null && now.after(vo.getStartTime());
        vo.setIsEnded(ended);
        vo.setIsRunning(running);
    }

    private boolean isCompleted(AssignmentVO vo) {
        Integer problemCount = vo.getProblemCount();
        Integer acceptedCount = vo.getAcceptedCount();
        return problemCount != null && problemCount > 0
                && acceptedCount != null && acceptedCount >= problemCount;
    }
}
