package top.hcode.hoj.service.admin.assignment;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.assignment.StudentGroup;
import top.hcode.hoj.pojo.vo.StudentGroupUserVO;
import top.hcode.hoj.pojo.vo.StudentGroupVO;

import java.util.List;

/**
 * 后台管理学生组 Service
 */
public interface AdminStudentGroupService {

    CommonResult<List<StudentGroupVO>> getGroupList();

    CommonResult<List<StudentGroupUserVO>> getGroupUserList(Long gid);

    CommonResult<Void> addGroup(StudentGroup group);

    CommonResult<Void> updateGroup(StudentGroup group);

    CommonResult<Void> deleteGroup(Long gid);

    CommonResult<Void> addGroupUser(Long gid, List<String> uidList);

    CommonResult<Void> removeGroupUser(Long gid, String uid);
}
