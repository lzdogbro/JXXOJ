package top.hcode.hoj.dao.wechat.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.wechat.ParentBindingEntityService;
import top.hcode.hoj.mapper.ParentBindingMapper;
import top.hcode.hoj.pojo.entity.assignment.ParentBinding;

import javax.annotation.Resource;
import java.util.List;

/**
 * 家长-孩子绑定 EntityService 实现
 */
@Service
public class ParentBindingEntityServiceImpl extends ServiceImpl<ParentBindingMapper, ParentBinding> implements ParentBindingEntityService {

    @Resource
    private ParentBindingMapper parentBindingMapper;

    @Override
    public List<String> getBoundParentOpenids(String studentUid) {
        return parentBindingMapper.getBoundParentOpenids(studentUid);
    }
}
