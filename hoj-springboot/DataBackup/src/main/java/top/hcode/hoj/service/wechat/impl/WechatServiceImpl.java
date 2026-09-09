package top.hcode.hoj.service.wechat.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.wechat.WechatManager;
import top.hcode.hoj.pojo.dto.WechatBindDTO;
import top.hcode.hoj.pojo.dto.WechatLoginDTO;
import top.hcode.hoj.pojo.dto.WechatSubscribeDTO;
import top.hcode.hoj.pojo.vo.AssignmentVO;
import top.hcode.hoj.pojo.vo.ChildVO;
import top.hcode.hoj.pojo.vo.WechatAssignmentDetailVO;
import top.hcode.hoj.pojo.vo.WechatLoginVO;
import top.hcode.hoj.service.wechat.WechatService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 微信家长侧接口 Service 实现
 */
@Service
public class WechatServiceImpl implements WechatService {

    @Resource
    private WechatManager wechatManager;

    @Override
    public CommonResult<WechatLoginVO> login(WechatLoginDTO dto) {
        try {
            return CommonResult.successResponse(wechatManager.login(dto));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<Map<String, Object>> bindCode() {
        try {
            return CommonResult.successResponse(wechatManager.bindCode());
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<ChildVO> bind(WechatBindDTO dto) {
        try {
            return CommonResult.successResponse(wechatManager.bind(dto));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<Void> unbind(String studentUid) {
        try {
            wechatManager.unbind(studentUid);
            return CommonResult.successResponse();
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        }
    }

    @Override
    public CommonResult<List<ChildVO>> childList() {
        return CommonResult.successResponse(wechatManager.childList());
    }

    @Override
    public CommonResult<IPage<AssignmentVO>> childAssignments(String uid, Integer limit, Integer currentPage) {
        try {
            return CommonResult.successResponse(wechatManager.childAssignments(uid, limit, currentPage));
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<WechatAssignmentDetailVO> childAssignmentDetail(String uid, Long aid) {
        try {
            return CommonResult.successResponse(wechatManager.childAssignmentDetail(uid, aid));
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> subscribe(WechatSubscribeDTO dto) {
        try {
            wechatManager.subscribe(dto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public byte[] getQrcode(String bindCode) {
        return wechatManager.getQrcode(bindCode);
    }
}
