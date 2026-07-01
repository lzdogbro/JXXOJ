package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.PkMatchVO;
import top.hcode.hoj.pojo.vo.PkScoreVO;
import top.hcode.hoj.service.oj.PkService;

import java.util.List;
import java.util.Map;

/**
 * @Description: PK对战接口
 */
@RestController
@RequestMapping("/api")
public class PkController {

    @Autowired
    private PkService pkService;

    @PostMapping("/pk/invite")
    @RequiresAuthentication
    public CommonResult<Long> createPkInvite(@RequestBody Map<String, Object> body) {
        String problemId = body.get("problemId").toString();
        String opponentUid = (String) body.get("opponentUid");
        return pkService.createPkInvite(problemId, opponentUid);
    }

    @PostMapping("/pk/respond")
    @RequiresAuthentication
    public CommonResult<PkMatchVO> respondToPkInvite(@RequestBody Map<String, Object> body) {
        Long matchId = Long.valueOf(body.get("matchId").toString());
        Boolean accept = (Boolean) body.get("accept");
        return pkService.respondToPkInvite(matchId, accept);
    }

    @PostMapping("/pk/cancel")
    @RequiresAuthentication
    public CommonResult<Void> cancelPkInvite(@RequestBody Map<String, Object> body) {
        Long matchId = Long.valueOf(body.get("matchId").toString());
        return pkService.cancelPkInvite(matchId);
    }

    @PostMapping("/pk/surrender")
    @RequiresAuthentication
    public CommonResult<PkMatchVO> surrenderPk(@RequestBody Map<String, Object> body) {
        Long matchId = Long.valueOf(body.get("matchId").toString());
        return pkService.surrenderPk(matchId);
    }

    @GetMapping("/pk/status")
    @RequiresAuthentication
    public CommonResult<PkMatchVO> getPkMatchStatus(@RequestParam Long matchId) {
        return pkService.getPkMatchStatus(matchId);
    }

    @GetMapping("/pk/my-active")
    @RequiresAuthentication
    public CommonResult<PkMatchVO> getMyActivePkMatch() {
        return pkService.getMyActivePkMatch();
    }

    @GetMapping("/pk/my-invites")
    @RequiresAuthentication
    public CommonResult<List<PkMatchVO>> getMyPendingInvites() {
        return pkService.getMyPendingInvites();
    }

    @GetMapping("/pk/score")
    @RequiresAuthentication
    public CommonResult<PkScoreVO> getPkScore(@RequestParam(required = false) String uid) {
        return pkService.getPkScore(uid);
    }

    @GetMapping("/pk/search-users")
    @RequiresAuthentication
    public CommonResult<List<Map<String, Object>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") Integer limit) {
        return pkService.searchUsers(keyword, limit);
    }

    @GetMapping("/pk/history")
    @RequiresAuthentication
    public CommonResult<IPage<PkMatchVO>> getPkHistory(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "1") Integer currentPage) {
        return pkService.getPkHistory(limit, currentPage);
    }
}
