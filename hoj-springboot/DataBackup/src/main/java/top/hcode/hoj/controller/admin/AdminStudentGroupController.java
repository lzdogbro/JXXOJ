package top.hcode.hoj.controller.admin;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.assignment.StudentGroup;
import top.hcode.hoj.pojo.vo.StudentGroupUserVO;
import top.hcode.hoj.pojo.vo.StudentGroupVO;
import top.hcode.hoj.service.admin.assignment.AdminStudentGroupService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 后台管理学生组 Controller
 */
@RestController
@RequestMapping("/api/admin/assignment/group")
public class AdminStudentGroupController {

    @Resource
    private AdminStudentGroupService adminStudentGroupService;

    @GetMapping("/get-group-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult<List<StudentGroupVO>> getGroupList() {
        return adminStudentGroupService.getGroupList();
    }

    @GetMapping("/get-group-user-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult<List<StudentGroupUserVO>> getGroupUserList(@RequestParam("gid") Long gid) {
        return adminStudentGroupService.getGroupUserList(gid);
    }

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult<Void> addGroup(@RequestBody StudentGroup group) {
        return adminStudentGroupService.addGroup(group);
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult<Void> updateGroup(@RequestBody StudentGroup group) {
        return adminStudentGroupService.updateGroup(group);
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteGroup(@RequestParam("gid") Long gid) {
        return adminStudentGroupService.deleteGroup(gid);
    }

    @PostMapping("/add-user")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult<Void> addGroupUser(@RequestParam("gid") Long gid,
                                           @RequestBody List<String> uidList) {
        return adminStudentGroupService.addGroupUser(gid, uidList);
    }

    @DeleteMapping("/remove-user")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult<Void> removeGroupUser(@RequestParam("gid") Long gid,
                                              @RequestParam("uid") String uid) {
        return adminStudentGroupService.removeGroupUser(gid, uid);
    }
}
