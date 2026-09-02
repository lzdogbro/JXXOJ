package top.hcode.hoj.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
import top.hcode.hoj.pojo.dto.AssignmentDTO;
import top.hcode.hoj.pojo.vo.AssignmentVO;
import top.hcode.hoj.service.admin.assignment.AdminAssignmentService;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * 后台管理作业 Controller
 */
@RestController
@RequestMapping("/api/admin/assignment")
public class AdminAssignmentController {

    @Resource
    private AdminAssignmentService adminAssignmentService;

    @GetMapping("/get-assignment-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<IPage<AssignmentVO>> getAssignmentList(@RequestParam(value = "limit", required = false) Integer limit,
                                                               @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                               @RequestParam(value = "keyword", required = false) String keyword) {
        return adminAssignmentService.getAssignmentList(limit, currentPage, keyword);
    }

    @GetMapping("/get-assignment")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<HashMap<String, Object>> getAssignment(@RequestParam("aid") Long aid) {
        return adminAssignmentService.getAssignment(aid);
    }

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult<Void> addAssignment(@RequestBody AssignmentDTO assignmentDto) {
        return adminAssignmentService.addAssignment(assignmentDto);
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult<Void> updateAssignment(@RequestBody AssignmentDTO assignmentDto) {
        return adminAssignmentService.updateAssignment(assignmentDto);
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteAssignment(@RequestParam("aid") Long aid) {
        return adminAssignmentService.deleteAssignment(aid);
    }

    @PostMapping("/publish")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult<Void> publishAssignment(@RequestBody AssignmentDTO assignmentDto) {
        return adminAssignmentService.publishAssignment(assignmentDto);
    }

    @PostMapping("/extend")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult<Void> extendAssignment(@RequestBody AssignmentDTO assignmentDto) {
        return adminAssignmentService.extendAssignment(assignmentDto.getId(), assignmentDto.getEndTime());
    }
}
