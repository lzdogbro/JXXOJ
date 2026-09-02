package top.hcode.hoj.dao.assignment;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.assignment.AssignmentStudent;
import top.hcode.hoj.pojo.vo.AssignmentStudentVO;

import java.util.List;

/**
 * 作业下发快照 EntityService
 */
public interface AssignmentStudentEntityService extends IService<AssignmentStudent> {

    List<AssignmentStudentVO> getAssignmentStudentList(Long aid);
}
