package top.hcode.hoj.dao.assignment;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.assignment.AssignmentProblem;
import top.hcode.hoj.pojo.vo.AssignmentProblemVO;

import java.util.List;

/**
 * 作业题目 EntityService
 */
public interface AssignmentProblemEntityService extends IService<AssignmentProblem> {

    List<AssignmentProblemVO> getAssignmentProblemList(Long aid);
}
