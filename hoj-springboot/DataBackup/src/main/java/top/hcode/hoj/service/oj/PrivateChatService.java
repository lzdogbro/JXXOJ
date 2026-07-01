package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.ChatContactVO;
import top.hcode.hoj.pojo.vo.PrivateChatVO;

import java.util.List;

public interface PrivateChatService {

    public CommonResult<List<ChatContactVO>> getChatContacts();

    public CommonResult<IPage<PrivateChatVO>> getChatMessages(Integer limit, Integer currentPage, String contactUid);

    public CommonResult<Void> sendMessage(String recipientId, String content);

    public CommonResult<Integer> getUnreadChatCount();
}
