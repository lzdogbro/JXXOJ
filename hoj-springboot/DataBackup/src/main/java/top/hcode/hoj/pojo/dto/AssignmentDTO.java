package top.hcode.hoj.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import top.hcode.hoj.pojo.entity.assignment.AssignmentProblem;

import java.util.Date;
import java.util.List;

/**
 * 后台管理作业的传输类
 */
@Data
@Accessors(chain = true)
public class AssignmentDTO {

    private Long id;

    private String title;

    private String description;

    private Integer isRequired;

    private Integer status;

    private Date startTime;

    private Date endTime;

    /**
     * 下发学生组id列表
     */
    private List<Long> groupIdList;

    /**
     * 临时手动追加的学生uid列表
     */
    private List<String> extraUidList;

    /**
     * 作业题目列表
     */
    private List<AssignmentProblem> problemList;
}
