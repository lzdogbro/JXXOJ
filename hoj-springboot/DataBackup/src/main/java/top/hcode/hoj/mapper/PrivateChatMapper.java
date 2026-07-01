package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.chat.PrivateChat;
import top.hcode.hoj.pojo.vo.ChatContactVO;
import top.hcode.hoj.pojo.vo.PrivateChatVO;

import java.util.List;

@Mapper
@Repository
public interface PrivateChatMapper extends BaseMapper<PrivateChat> {

    List<ChatContactVO> getChatContacts(@Param("uid") String uid);

    IPage<PrivateChatVO> getChatMessages(Page<PrivateChatVO> page,
                                          @Param("uid") String uid,
                                          @Param("contactUid") String contactUid);
}
