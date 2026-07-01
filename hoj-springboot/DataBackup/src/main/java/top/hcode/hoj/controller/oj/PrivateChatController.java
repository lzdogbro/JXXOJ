package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.ChatContactVO;
import top.hcode.hoj.pojo.vo.PrivateChatVO;
import top.hcode.hoj.service.oj.PrivateChatService;

import java.util.List;
import java.util.Map;

/**
 * @Description: 私聊功能接口
 */
@RestController
@RequestMapping("/api")
public class PrivateChatController {

    @Autowired
    private PrivateChatService privateChatService;

    @GetMapping("/get-chat-contacts")
    @RequiresAuthentication
    public CommonResult<List<ChatContactVO>> getChatContacts() {
        return privateChatService.getChatContacts();
    }

    @GetMapping("/get-chat-messages")
    @RequiresAuthentication
    public CommonResult<IPage<PrivateChatVO>> getChatMessages(
            @RequestParam(value = "uid", required = true) String uid,
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
            @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage) {
        return privateChatService.getChatMessages(limit, currentPage, uid);
    }

    @PostMapping("/send-chat-message")
    @RequiresAuthentication
    public CommonResult<Void> sendChatMessage(@RequestBody Map<String, String> body) {
        String recipientId = body.get("recipientId");
        String content = body.get("content");
        return privateChatService.sendMessage(recipientId, content);
    }

    @GetMapping("/chat-unread-count")
    @RequiresAuthentication
    public CommonResult<Integer> getChatUnreadCount() {
        return privateChatService.getUnreadChatCount();
    }
}
