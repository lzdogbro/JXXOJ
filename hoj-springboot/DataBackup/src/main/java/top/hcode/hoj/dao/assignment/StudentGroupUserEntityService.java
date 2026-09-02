package top.hcode.hoj.dao.assignment;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.assignment.StudentGroupUser;
import top.hcode.hoj.pojo.vo.StudentGroupUserVO;

import java.util.List;

/**
 * 学生组成员 EntityService
 */
public interface StudentGroupUserEntityService extends IService<StudentGroupUser> {

    List<StudentGroupUserVO> getGroupUserList(Long gid);
}
