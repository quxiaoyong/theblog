package org.fantasizer.theblog.xo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.fantasizer.theblog.common.validator.annnotation.NotBlank;
import org.fantasizer.theblog.common.validator.group.FetchOne;
import org.fantasizer.theblog.common.validator.group.Insert;
import org.fantasizer.theblog.common.vo.BaseVO;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:47
 */
@Data
public class UserVO extends BaseVO<UserVO> {
    /**
     * 用户名
     */
    @NotBlank(groups = {Insert.class, FetchOne.class})
    private String userName;

    /**
     * 密码
     */
    @NotBlank(groups = {Insert.class, FetchOne.class})
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
    @NotBlank(groups = {Insert.class})
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
     * 资料来源
     */
    private String source;

    /**
     * 平台uuid
     */
    private String uuid;

}