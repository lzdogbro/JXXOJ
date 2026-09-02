package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.AssignmentProblemVO;
import top.hcode.hoj.pojo.vo.AssignmentVO;
import top.hcode.hoj.pojo.vo.ProblemInfoVO;
import top.hcode.hoj.service.oj.AssignmentService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * 学生视图作业接口
 */
@RestController
@RequestMapping("/api")
public class AssignmentController {

    @Resource
    private AssignmentService assignmentService;

    /**
     * @param limit
     * @param currentPage
     * @MethodName getMyAssignmentList
     * @Description 获取布置给我的作业（分页：未完成在前、必做未完成置顶）
     */
    @GetMapping("/get-assignment-list")
    @RequiresAuthentication
    public CommonResult<IPage<AssignmentVO>> getMyAssignmentList(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage) {
        return assignmentService.getMyAssignmentList(limit, currentPage);
    }

    /**
     * @param aid
     * @MethodName getAssignmentDetail
     * @Description 获取单份作业 + 我的完成进度
     */
    @GetMapping("/get-assignment-detail")
    @RequiresAuthentication
    public CommonResult<HashMap<String, Object>> getAssignmentDetail(@RequestParam(value = "aid") Long aid) {
        return assignmentService.getAssignmentDetail(aid);
    }

    /**
     * @param aid
     * @MethodName getAssignmentProblemList
     * @Description 获取作业题目列表（提交入口用）
     */
    @GetMapping("/get-assignment-problem-list")
    @RequiresAuthentication
    public CommonResult<List<AssignmentProblemVO>> getAssignmentProblemList(@RequestParam(value = "aid") Long aid) {
        return assignmentService.getAssignmentProblemList(aid);
    }

    /**
     * @param aid
     * @param displayId
     * @MethodName getAssignmentProblemDetails
     * @Description 获取作业题目详情（提交页用）
     */
    @GetMapping("/get-assignment-problem-detail")
    @RequiresAuthentication
    public CommonResult<ProblemInfoVO> getAssignmentProblemDetails(@RequestParam(value = "aid") Long aid,
                                                                   @RequestParam(value = "displayId") String displayId) {
        return assignmentService.getAssignmentProblemDetails(aid, displayId);
    }
}
