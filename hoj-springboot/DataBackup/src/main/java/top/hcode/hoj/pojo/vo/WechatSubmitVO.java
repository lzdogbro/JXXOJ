package top.hcode.hoj.pojo.vo;

import lombok.Data;

import java.util.Date;

/**
 * 家长侧作业题目「最近提交」快照（pid + 最近提交时间）
 */
@Data
public class WechatSubmitVO {

    private Long pid;

    private Date submitTime;
}
