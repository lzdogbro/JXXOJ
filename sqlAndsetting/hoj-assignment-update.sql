USE `hoj`;

-- =====================================================================
-- 「作业」功能 数据模型迁移
-- 对齐 docs/作业功能设计.md §3：6 张新表 + judge 加 aid 列（幂等写法）
-- =====================================================================

-- 3.1 作业
CREATE TABLE IF NOT EXISTS `assignment` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '作业ID',
  `title` varchar(200) NOT NULL COMMENT '作业标题',
  `description` mediumtext COMMENT '作业说明',
  `creator_uid` varchar(32) NOT NULL COMMENT '创建者uid(管理员)',
  `is_required` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否必做 1必做 0选做',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0草稿 1已发布',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '截止时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除 0正常 1已删除',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_creator` (`creator_uid`),
  KEY `idx_status_end` (`status`, `end_time`),
  CONSTRAINT `assignment_ibfk_1` FOREIGN KEY (`creator_uid`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='作业';

-- 3.2 作业题目
CREATE TABLE IF NOT EXISTS `assignment_problem` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `aid` bigint(20) unsigned NOT NULL COMMENT '作业id',
  `pid` bigint(20) unsigned NOT NULL COMMENT '题目id',
  `display_id` varchar(20) NOT NULL COMMENT '作业内展示编号 A/B/C',
  `score` int(11) NOT NULL DEFAULT '0' COMMENT '分值(预留,AC制暂用)',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_aid_pid` (`aid`, `pid`),
  KEY `idx_pid` (`pid`),
  CONSTRAINT `assignment_problem_ibfk_1` FOREIGN KEY (`aid`) REFERENCES `assignment` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `assignment_problem_ibfk_2` FOREIGN KEY (`pid`) REFERENCES `problem` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='作业题目';

-- 3.3 学生组 + 成员
CREATE TABLE IF NOT EXISTS `student_group` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '组名',
  `owner_uid` varchar(32) NOT NULL COMMENT '组主uid',
  `description` varchar(500) DEFAULT NULL COMMENT '组描述',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_owner` (`owner_uid`),
  CONSTRAINT `student_group_ibfk_1` FOREIGN KEY (`owner_uid`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='学生组';

CREATE TABLE IF NOT EXISTS `student_group_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `gid` bigint(20) unsigned NOT NULL COMMENT '学生组id',
  `uid` varchar(32) NOT NULL COMMENT '学生uid',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_gid_uid` (`gid`, `uid`),
  KEY `idx_uid` (`uid`),
  CONSTRAINT `student_group_user_ibfk_1` FOREIGN KEY (`gid`) REFERENCES `student_group` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `student_group_user_ibfk_2` FOREIGN KEY (`uid`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='学生组成员';

-- 3.4 作业下发快照 + 完成情况
CREATE TABLE IF NOT EXISTS `assignment_student` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `aid` bigint(20) unsigned NOT NULL COMMENT '作业id',
  `uid` varchar(32) NOT NULL COMMENT '学生uid',
  `is_required` tinyint(1) NOT NULL DEFAULT '0' COMMENT '冗余:是否必做,便于查"必做未完成"',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0未完成 1已完成',
  `accepted_count` int(11) NOT NULL DEFAULT '0' COMMENT '已AC题数',
  `score` int(11) NOT NULL DEFAULT '0' COMMENT '得分(预留)',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '下发时间',
  `gmt_finish` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_aid_uid` (`aid`, `uid`),
  KEY `idx_uid_status` (`uid`, `status`),
  KEY `idx_aid_status` (`aid`, `status`),
  CONSTRAINT `assignment_student_ibfk_1` FOREIGN KEY (`aid`) REFERENCES `assignment` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `assignment_student_ibfk_2` FOREIGN KEY (`uid`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='作业下发快照与完成情况';

-- 3.5 微信家长（本轮仅建表 + 预留接口，小程序另排期）
CREATE TABLE IF NOT EXISTS `wechat_user` (
  `openid` varchar(64) NOT NULL COMMENT '微信openid',
  `uid` varchar(32) DEFAULT NULL COMMENT '平台账号uid,可空',
  `nickname` varchar(100) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`openid`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='微信身份映射';

CREATE TABLE IF NOT EXISTS `parent_binding` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `parent_openid` varchar(64) NOT NULL COMMENT '家长openid',
  `student_uid` varchar(32) NOT NULL COMMENT '孩子平台uid',
  `bind_code` varchar(32) NOT NULL COMMENT '一次性绑定码',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0待确认 1已绑定 2已解绑',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bind_code` (`bind_code`),
  KEY `idx_parent` (`parent_openid`),
  KEY `idx_student` (`student_uid`),
  CONSTRAINT `parent_binding_ibfk_1` FOREIGN KEY (`student_uid`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='家长-孩子绑定';

-- 3.6 judge 加 aid 列（幂等）
DROP PROCEDURE IF EXISTS `add_judge_aid`;
DELIMITER $$
CREATE PROCEDURE `add_judge_aid`()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE table_schema = 'hoj' AND table_name = 'judge' AND column_name = 'aid'
  ) THEN
    ALTER TABLE `judge` ADD COLUMN `aid` bigint(20) unsigned DEFAULT '0' COMMENT '作业id,非作业提交默认为0';
  END IF;
END$$
DELIMITER ;
CALL `add_judge_aid`();
DROP PROCEDURE `add_judge_aid`;
