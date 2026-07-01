package top.hcode.hoj.dao.chat.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.chat.PrivateChatEntityService;
import top.hcode.hoj.mapper.PrivateChatMapper;
import top.hcode.hoj.pojo.entity.chat.PrivateChat;
import top.hcode.hoj.pojo.vo.ChatContactVO;
import top.hcode.hoj.pojo.vo.PrivateChatVO;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description: 私聊消息实体服务实现
 */
@Service
public class PrivateChatEntityServiceImpl extends ServiceImpl<PrivateChatMapper, PrivateChat> implements PrivateChatEntityService {

    @Resource
    private PrivateChatMapper privateChatMapper;

    @Override
    public List<ChatContactVO> getChatContacts(String uid) {
        return privateChatMapper.getChatContacts(uid);
    }

    @Override
    public IPage<PrivateChatVO> getChatMessages(Page<PrivateChatVO> page, String uid, String contactUid) {
        return privateChatMapper.getChatMessages(page, uid, contactUid);
    }

    @Override
    public void updateMsgRead(String senderId, String recipientId) {
        UpdateWrapper<PrivateChat> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .eq("sender_id", senderId)
                .eq("recipient_id", recipientId)
                .eq("state", false)
                .set("state", true);
        this.update(null, updateWrapper);
    }
}
