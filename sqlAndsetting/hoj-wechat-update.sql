USE `hoj`;

-- =====================================================================
-- 微信家长侧 增量迁移（对齐 docs/微信后端接口-更新设计.md §2）
-- 幂等：可重复执行
-- =====================================================================

-- 1) parent_openid 允许 NULL（生成绑定码阶段还没有家长 openid，MODIFY 幂等）
ALTER TABLE `parent_binding`
  MODIFY COLUMN `parent_openid` varchar(64) DEFAULT NULL COMMENT '家长openid,生成码阶段为NULL';

-- 2) 补绑定码生命周期两列（幂等加列）
DROP PROCEDURE IF EXISTS `add_wechat_binding_cols`;
DELIMITER $$
CREATE PROCEDURE `add_wechat_binding_cols`()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE table_schema = 'hoj' AND table_name = 'parent_binding' AND column_name = 'gmt_expire'
  ) THEN
    ALTER TABLE `parent_binding` ADD COLUMN `gmt_expire` datetime DEFAULT NULL COMMENT '绑定码过期时间' AFTER `status`;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE table_schema = 'hoj' AND table_name = 'parent_binding' AND column_name = 'attempt_count'
  ) THEN
    ALTER TABLE `parent_binding` ADD COLUMN `attempt_count` int(11) NOT NULL DEFAULT '0' COMMENT '校验失败次数' AFTER `gmt_expire`;
  END IF;
END$$
DELIMITER ;
CALL `add_wechat_binding_cols`();
DROP PROCEDURE `add_wechat_binding_cols`;

-- 3) 订阅消息推送配额表（DataBackup/JudgeServer 跨服务共享，故用 DB 而非 Redis）
CREATE TABLE IF NOT EXISTS `wechat_subscribe_quota` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `openid` varchar(64) NOT NULL COMMENT '家长openid',
  `template_id` varchar(100) NOT NULL COMMENT '订阅消息模板id',
  `quota` int(11) NOT NULL DEFAULT '0' COMMENT '剩余推送配额(一次性订阅)',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid_template` (`openid`, `template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='微信订阅消息推送配额';
