package top.hcode.hoj.manager.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.chat.PrivateChatEntityService;
import top.hcode.hoj.pojo.entity.chat.PrivateChat;
import top.hcode.hoj.pojo.vo.ChatContactVO;
import top.hcode.hoj.pojo.vo.PrivateChatVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.validator.CommonValidator;

import java.util.List;

/**
 * @Description: 私聊业务管理
 */
@Component
public class PrivateChatManager {

    @Autowired
    private PrivateChatEntityService privateChatEntityService;

    @Autowired
    private CommonValidator commonValidator;

    /**
     * 获取当前登录用户的联系人列表
     */
    public List<ChatContactVO> getChatContacts() {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        return privateChatEntityService.getChatContacts(userRolesVo.getUid());
    }

    /**
     * 分页获取与某联系人的聊天记录
     */
    public IPage<PrivateChatVO> getChatMessages(Integer limit, Integer currentPage, String contactUid) {
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 20;

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        Page<PrivateChatVO> page = new Page<>(currentPage, limit);
        IPage<PrivateChatVO> chatMessages = privateChatEntityService.getChatMessages(page, userRolesVo.getUid(), contactUid);

        // 将对方发来的消息标为已读（异步处理）
        if (chatMessages.getTotal() > 0) {
            privateChatEntityService.updateMsgRead(contactUid, userRolesVo.getUid());
        }

        return chatMessages;
    }

    /**
     * 发送私聊消息
     */
    public void sendMessage(String recipientId, String content) throws StatusFailException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        commonValidator.validateNotEmpty(content, "消息内容不能为空");

        if (content.length() > 1000) {
            throw new StatusFailException("消息内容过长，请限制在1000字符以内");
        }

        if (recipientId.equals(userRolesVo.getUid())) {
            throw new StatusFailException("不能给自己发送消息");
        }

        PrivateChat privateChat = new PrivateChat();
        privateChat.setSenderId(userRolesVo.getUid())
                .setRecipientId(recipientId)
                .setContent(content)
                .setState(false);

        privateChatEntityService.save(privateChat);
    }

    /**
     * 获取私聊总未读数
     */
    public Integer getUnreadChatCount() {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        return (int) privateChatEntityService.count(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PrivateChat>()
                        .eq("recipient_id", userRolesVo.getUid())
                        .eq("state", false)
        );
    }
}
