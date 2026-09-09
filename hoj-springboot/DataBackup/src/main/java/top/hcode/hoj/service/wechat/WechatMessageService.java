package top.hcode.hoj.service.wechat;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.hcode.hoj.config.WechatProperties;
import top.hcode.hoj.dao.assignment.AssignmentEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.dao.wechat.ParentBindingEntityService;
import top.hcode.hoj.dao.wechat.WechatSubscribeQuotaEntityService;
import top.hcode.hoj.pojo.entity.assignment.Assignment;
import top.hcode.hoj.pojo.entity.user.UserInfo;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信订阅消息推送服务（一次性订阅配额 + 异步推送）
 *
 * 配额跨服务共享：DataBackup / JudgeServer 均直连同一张 wechat_subscribe_quota 表，
 * 授权时 +1、推送前 -1（受影响行=1 才允许发）。
 */
@Component
public class WechatMessageService {

    @Resource
    private WechatApiClient wechatApiClient;

    @Resource
    private WechatProperties wechatProperties;

    @Resource
    private ParentBindingEntityService parentBindingEntityService;

    @Resource
    private WechatSubscribeQuotaEntityService wechatSubscribeQuotaEntityService;

    @Resource
    private UserInfoEntityService userInfoEntityService;

    @Resource
    private AssignmentEntityService assignmentEntityService;

    /**
     * 授权上报：某 openid 对某模板的配额 +1
     */
    public void addQuota(String openid, String templateId) {
        wechatSubscribeQuotaEntityService.addQuota(openid, templateId);
    }

    /**
     * 作业发布通知（异步，逐学生 → 逐绑定家长）
     */
    @Async
    public void notifyAssignmentPublish(Assignment assignment, List<String> studentUids) {
        if (assignment == null || studentUids == null || studentUids.isEmpty()) {
            return;
        }
        String templateId = wechatProperties.getTemplatePublish();
        if (templateId == null || templateId.trim().isEmpty()) {
            return;
        }
        for (String studentUid : studentUids) {
            sendToParents(studentUid, templateId,
                    buildData(studentName(studentUid), assignment.getTitle(), assignment.getEndTime(), "请及时查看并完成作业"));
        }
    }

    /**
     * 作业完成通知（异步，孩子完成某作业时）
     */
    @Async
    public void notifyAssignmentDone(Long aid, String studentUid) {
        Assignment assignment = assignmentEntityService.getById(aid);
        if (assignment == null) {
            return;
        }
        String templateId = wechatProperties.getTemplateDone();
        if (templateId == null || templateId.trim().isEmpty()) {
            return;
        }
        sendToParents(studentUid, templateId,
                buildData(studentName(studentUid), assignment.getTitle(), new Date(), null));
    }

    private void sendToParents(String studentUid, String templateId, Map<String, Object> data) {
        List<String> openids = parentBindingEntityService.getBoundParentOpenids(studentUid);
        if (openids == null || openids.isEmpty()) {
            return;
        }
        for (String openid : openids) {
            int affected = wechatSubscribeQuotaEntityService.decrementQuota(openid, templateId);
            if (affected > 0) {
                wechatApiClient.sendSubscribeMessage(openid, templateId, data, null);
            }
        }
    }

    private String studentName(String studentUid) {
        UserInfo student = userInfoEntityService.getById(studentUid);
        if (student == null) {
            return studentUid;
        }
        return student.getRealname() != null && !student.getRealname().isEmpty()
                ? student.getRealname() : student.getUsername();
    }

    /**
     * 组装订阅消息 data 字段。字段 key（thing1/thing2/time3/thing4）需与微信后台模板关键词一致。
     * 发布模板：thing1 姓名 + thing2 标题 + time3 截止时间 + thing4 提示；
     * 完成模板：thing1 姓名 + thing2 标题 + time3 完成时间（无 thing4）。
     * 上线时按实际模板调整。
     */
    private Map<String, Object> buildData(String studentName, String title, Date time, String remark) {
        Map<String, Object> data = new HashMap<>();
        data.put("thing1", wrap(studentName));
        data.put("thing2", wrap(title));
        data.put("time3", wrap(time == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(time)));
        if (remark != null) {
            data.put("thing4", wrap(remark));
        }
        return data;
    }

    private Map<String, Object> wrap(String value) {
        Map<String, Object> m = new HashMap<>();
        m.put("value", value == null ? "" : value);
        return m;
    }
}
