package top.hcode.hoj.dao.assignment;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.assignment.Assignment;
import top.hcode.hoj.pojo.vo.AssignmentVO;

/**
 * 作业 EntityService
 */
public interface AssignmentEntityService extends IService<Assignment> {

    IPage<AssignmentVO> getAssignmentList(int limit, int currentPage, String keyword, String creatorUid, Integer status);

    IPage<AssignmentVO> getMyAssignmentList(Page<AssignmentVO> page, String uid);
}
