package top.hcode.hoj.service.wechat;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.WechatBindDTO;
import top.hcode.hoj.pojo.dto.WechatLoginDTO;
import top.hcode.hoj.pojo.dto.WechatSubscribeDTO;
import top.hcode.hoj.pojo.vo.AssignmentVO;
import top.hcode.hoj.pojo.vo.ChildVO;
import top.hcode.hoj.pojo.vo.WechatAssignmentDetailVO;
import top.hcode.hoj.pojo.vo.WechatLoginVO;

import java.util.List;
import java.util.Map;

/**
 * 微信家长侧接口 Service
 */
public interface WechatService {

    CommonResult<WechatLoginVO> login(WechatLoginDTO dto);

    CommonResult<Map<String, Object>> bindCode();

    CommonResult<ChildVO> bind(WechatBindDTO dto);

    CommonResult<Void> unbind(String studentUid);

    CommonResult<List<ChildVO>> childList();

    CommonResult<IPage<AssignmentVO>> childAssignments(String uid, Integer limit, Integer currentPage);

    CommonResult<WechatAssignmentDetailVO> childAssignmentDetail(String uid, Long aid);

    CommonResult<Void> subscribe(WechatSubscribeDTO dto);

    byte[] getQrcode(String bindCode);
}
