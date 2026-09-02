package top.hcode.hoj.dao.assignment;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.assignment.StudentGroup;
import top.hcode.hoj.pojo.vo.StudentGroupVO;

import java.util.List;

/**
 * 学生组 EntityService
 */
public interface StudentGroupEntityService extends IService<StudentGroup> {

    List<StudentGroupVO> getGroupList(String ownerUid);
}
