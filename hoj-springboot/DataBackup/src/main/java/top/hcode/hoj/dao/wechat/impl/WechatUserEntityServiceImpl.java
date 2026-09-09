package top.hcode.hoj.dao.wechat.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.wechat.WechatUserEntityService;
import top.hcode.hoj.mapper.WechatUserMapper;
import top.hcode.hoj.pojo.entity.assignment.WechatUser;

/**
 * 微信身份映射 EntityService 实现
 */
@Service
public class WechatUserEntityServiceImpl extends ServiceImpl<WechatUserMapper, WechatUser> implements WechatUserEntityService {
}
