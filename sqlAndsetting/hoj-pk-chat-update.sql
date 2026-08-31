USE `hoj`;

CREATE TABLE IF NOT EXISTS `private_chat` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `sender_id` varchar(32) NOT NULL,
  `recipient_id` varchar(32) NOT NULL,
  `content` text NOT NULL,
  `state` tinyint(1) DEFAULT '0' COMMENT '0未读，1已读',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sender_recipient` (`sender_id`, `recipient_id`),
  KEY `idx_recipient_sender_state` (`recipient_id`, `sender_id`, `state`),
  CONSTRAINT `private_chat_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `private_chat_ibfk_2` FOREIGN KEY (`recipient_id`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `pk_match` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'PK对战ID',
  `problem_id` bigint(20) unsigned NOT NULL COMMENT '题目ID',
  `initiator_uid` varchar(32) NOT NULL COMMENT '发起者用户ID',
  `opponent_uid` varchar(32) NOT NULL COMMENT '对手用户ID',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '对战状态: 0=等待接受, 1=进行中, 2=发起者胜, 3=对手胜, 4=平局, 5=已拒绝, 6=已取消',
  `winner_uid` varchar(32) DEFAULT NULL COMMENT '胜者用户ID',
  `initiator_submit_id` bigint(20) unsigned DEFAULT NULL COMMENT '发起者AC的提交ID',
  `opponent_submit_id` bigint(20) unsigned DEFAULT NULL COMMENT '对手AC的提交ID',
  `start_time` datetime DEFAULT NULL COMMENT '对战开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '对战结束时间',
  `surrender_uid` varchar(32) DEFAULT NULL COMMENT '投降者用户ID',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_initiator` (`initiator_uid`),
  KEY `idx_opponent` (`opponent_uid`),
  KEY `idx_status` (`status`),
  KEY `idx_problem` (`problem_id`),
  CONSTRAINT `pk_match_ibfk_1` FOREIGN KEY (`initiator_uid`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `pk_match_ibfk_2` FOREIGN KEY (`opponent_uid`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `pk_match_ibfk_3` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP PROCEDURE IF EXISTS `add_user_record_pk_score`;
DELIMITER $$
CREATE PROCEDURE `add_user_record_pk_score`()
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE table_schema = 'hoj'
      AND table_name = 'user_record'
      AND column_name = 'pk_score'
  ) THEN
    ALTER TABLE `user_record` ADD COLUMN `pk_score` int(11) DEFAULT 0 COMMENT 'PK对战积分';
  END IF;
END$$
DELIMITER ;
CALL `add_user_record_pk_score`();
DROP PROCEDURE `add_user_record_pk_score`;
