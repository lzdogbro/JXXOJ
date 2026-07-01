package top.hcode.hoj.service.oj.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.oj.PkManager;
import top.hcode.hoj.pojo.vo.PkMatchVO;
import top.hcode.hoj.pojo.vo.PkScoreVO;
import top.hcode.hoj.service.oj.PkService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Description: PK对战服务实现
 */
@Service
public class PkServiceImpl implements PkService {

    @Resource
    private PkManager pkManager;

    @Override
    public CommonResult<Long> createPkInvite(String problemId, String opponentUid) {
        try {
            return CommonResult.successResponse(pkManager.createPkInvite(problemId, opponentUid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<PkMatchVO> respondToPkInvite(Long matchId, Boolean accept) {
        try {
            return CommonResult.successResponse(pkManager.respondToPkInvite(matchId, accept));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<Void> cancelPkInvite(Long matchId) {
        try {
            pkManager.cancelPkInvite(matchId);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<PkMatchVO> surrenderPk(Long matchId) {
        try {
            return CommonResult.successResponse(pkManager.surrenderPk(matchId));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<PkMatchVO> getPkMatchStatus(Long matchId) {
        try {
            return CommonResult.successResponse(pkManager.getPkMatchStatus(matchId));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<PkMatchVO> getMyActivePkMatch() {
        return CommonResult.successResponse(pkManager.getMyActivePkMatch());
    }

    @Override
    public CommonResult<List<PkMatchVO>> getMyPendingInvites() {
        return CommonResult.successResponse(pkManager.getMyPendingInvites());
    }

    @Override
    public CommonResult<PkScoreVO> getPkScore(String uid) {
        return CommonResult.successResponse(pkManager.getPkScore(uid));
    }

    @Override
    public CommonResult<List<Map<String, Object>>> searchUsers(String keyword, Integer limit) {
        return CommonResult.successResponse(pkManager.searchUsers(keyword, limit));
    }

    @Override
    public CommonResult<IPage<PkMatchVO>> getPkHistory(Integer limit, Integer currentPage) {
        return CommonResult.successResponse(pkManager.getPkHistory(limit, currentPage));
    }
}
