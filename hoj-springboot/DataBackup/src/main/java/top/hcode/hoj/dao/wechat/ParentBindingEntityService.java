package top.hcode.hoj.dao.wechat;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.assignment.ParentBinding;

import java.util.List;

/**
 * 家长-孩子绑定 EntityService
 */
public interface ParentBindingEntityService extends IService<ParentBinding> {

    /**
     * 查询某孩子绑定的家长 openid 列表（status=1）
     */
    List<String> getBoundParentOpenids(String studentUid);
}
