package org.fantasizer.theblog.xo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.fantasizer.theblog.common.vo.BaseVO;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:40
 */
@Data
public class AdministratorVO extends BaseVO<AdministratorVO> {
    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 性别(1:男2:女)
     */
    private String gender;

    /**
     * 个人头像
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 出生年月日
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date birthday;

    /**
     * 手机
     */
    private String mobile;

    /**
     * QQ号
     */
    private String qqNumber;

    /**
     * 微信号
     */
    private String weChat;

    /**
     * 职业
     */
    private String occupation;

    /**
     * 自我简介最多150字
     */
    private String summary;

    /**
     * github地址
     */
    private String github;

    /**
     * gitee地址
     */
    private String gitee;
}
