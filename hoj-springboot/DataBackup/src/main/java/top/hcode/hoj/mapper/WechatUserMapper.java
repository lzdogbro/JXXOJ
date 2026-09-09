package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.assignment.WechatUser;

/**
 * 微信身份映射 Mapper
 */
@Mapper
@Repository
public interface WechatUserMapper extends BaseMapper<WechatUser> {
}
