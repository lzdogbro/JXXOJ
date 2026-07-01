package top.hcode.hoj.service.oj.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.oj.PrivateChatManager;
import top.hcode.hoj.pojo.vo.ChatContactVO;
import top.hcode.hoj.pojo.vo.PrivateChatVO;
import top.hcode.hoj.service.oj.PrivateChatService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description: 私聊服务实现
 */
@Service
public class PrivateChatServiceImpl implements PrivateChatService {

    @Resource
    private PrivateChatManager privateChatManager;

    @Override
    public CommonResult<List<ChatContactVO>> getChatContacts() {
        return CommonResult.successResponse(privateChatManager.getChatContacts());
    }

    @Override
    public CommonResult<IPage<PrivateChatVO>> getChatMessages(Integer limit, Integer currentPage, String contactUid) {
        return CommonResult.successResponse(privateChatManager.getChatMessages(limit, currentPage, contactUid));
    }

    @Override
    public CommonResult<Void> sendMessage(String recipientId, String content) {
        try {
            privateChatManager.sendMessage(recipientId, content);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<Integer> getUnreadChatCount() {
        return CommonResult.successResponse(privateChatManager.getUnreadChatCount());
    }
}
