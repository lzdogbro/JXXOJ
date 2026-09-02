package top.hcode.hoj.manager.admin.assignment;

import org.apache.shiro.SecurityUtils;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.shiro.AccountProfile;

/**
 * 作业域后台管理权限校验工具
 */
public final class AssignmentAuthHelper {

    private AssignmentAuthHelper() {
    }

    /**
     * 校验当前用户是资源所有者或 root
     */
    public static void checkOwnerOrRoot(String ownerUid) throws StatusForbiddenException {
        if (SecurityUtils.getSubject().hasRole("root")) {
            return;
        }
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        if (userRolesVo == null || !userRolesVo.getUid().equals(ownerUid)) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }
    }
}
