package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.PkMatchVO;
import top.hcode.hoj.pojo.vo.PkScoreVO;

import java.util.List;
import java.util.Map;

public interface PkService {

    CommonResult<Long> createPkInvite(String problemId, String opponentUid);

    CommonResult<PkMatchVO> respondToPkInvite(Long matchId, Boolean accept);

    CommonResult<Void> cancelPkInvite(Long matchId);

    CommonResult<PkMatchVO> surrenderPk(Long matchId);

    CommonResult<PkMatchVO> getPkMatchStatus(Long matchId);

    CommonResult<PkMatchVO> getMyActivePkMatch();

    CommonResult<List<PkMatchVO>> getMyPendingInvites();

    CommonResult<List<PkMatchVO>> getMyAllPendingInvites();

    CommonResult<PkScoreVO> getPkScore(String uid);

    CommonResult<List<Map<String, Object>>> searchUsers(String keyword, Integer limit);

    CommonResult<IPage<PkMatchVO>> getPkHistory(Integer limit, Integer currentPage);
}
