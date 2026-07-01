package top.hcode.hoj.dao.chat;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.chat.PrivateChat;
import top.hcode.hoj.pojo.vo.ChatContactVO;
import top.hcode.hoj.pojo.vo.PrivateChatVO;

import java.util.List;

/**
 * @Description: 私聊消息实体服务接口
 */
public interface PrivateChatEntityService extends IService<PrivateChat> {

    List<ChatContactVO> getChatContacts(String uid);

    IPage<PrivateChatVO> getChatMessages(Page<PrivateChatVO> page, String uid, String contactUid);

    void updateMsgRead(String senderId, String recipientId);
}
