package top.hcode.hoj.controller.wechat;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.WechatBindDTO;
import top.hcode.hoj.pojo.dto.WechatLoginDTO;
import top.hcode.hoj.pojo.dto.WechatSubscribeDTO;
import top.hcode.hoj.pojo.dto.WechatUnbindDTO;
import top.hcode.hoj.pojo.vo.AssignmentVO;
import top.hcode.hoj.pojo.vo.ChildVO;
import top.hcode.hoj.pojo.vo.WechatAssignmentDetailVO;
import top.hcode.hoj.pojo.vo.WechatLoginVO;
import top.hcode.hoj.service.wechat.WechatService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 微信家长侧接口（挂在 /api/wechat/** 前缀下，认证见 ShiroConfig）
 */
@RestController
@RequestMapping("/api/wechat")
public class WechatController {

    @Resource
    private WechatService wechatService;

    /**
     * 微信登录（无需认证）
     */
    @PostMapping("/login")
    public CommonResult<WechatLoginVO> login(@RequestBody WechatLoginDTO dto) {
        return wechatService.login(dto);
    }

    /**
     * 生成一次性绑定码（平台学生登录态）
     */
    @PostMapping("/bind-code")
    @RequiresAuthentication
    public CommonResult<Map<String, Object>> bindCode() {
        return wechatService.bindCode();
    }

    /**
     * 家长绑定孩子（微信登录态）
     */
    @PostMapping("/bind")
    public CommonResult<ChildVO> bind(@RequestBody WechatBindDTO dto) {
        return wechatService.bind(dto);
    }

    /**
     * 解绑（微信登录态）
     */
    @PostMapping("/unbind")
    public CommonResult<Void> unbind(@RequestBody WechatUnbindDTO dto) {
        return wechatService.unbind(dto.getStudentUid());
    }

    /**
     * 我的孩子列表（微信登录态）
     */
    @GetMapping("/child/list")
    public CommonResult<List<ChildVO>> childList() {
        return wechatService.childList();
    }

    /**
     * 孩子作业列表（微信登录态）
     */
    @GetMapping("/child/{uid}/assignments")
    public CommonResult<IPage<AssignmentVO>> childAssignments(
            @PathVariable("uid") String uid,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage) {
        return wechatService.childAssignments(uid, limit, currentPage);
    }

    /**
     * 孩子作业详情（微信登录态）
     */
    @GetMapping("/child/{uid}/assignment/{aid}")
    public CommonResult<WechatAssignmentDetailVO> childAssignmentDetail(
            @PathVariable("uid") String uid,
            @PathVariable("aid") Long aid) {
        return wechatService.childAssignmentDetail(uid, aid);
    }

    /**
     * 订阅消息授权上报（微信登录态）
     */
    @PostMapping("/subscribe")
    public CommonResult<Void> subscribe(@RequestBody WechatSubscribeDTO dto) {
        return wechatService.subscribe(dto);
    }

    /**
     * 绑定码小程序码图片（无认证，供 Web 端 <img> 直接加载）
     */
    @GetMapping("/qrcode/{bindCode}")
    public ResponseEntity<byte[]> qrcode(@PathVariable("bindCode") String bindCode) {
        byte[] bytes = wechatService.getQrcode(bindCode);
        if (bytes == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=300")
                .body(bytes);
    }
}
