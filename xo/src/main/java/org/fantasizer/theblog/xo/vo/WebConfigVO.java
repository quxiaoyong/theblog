package org.fantasizer.theblog.xo.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.fantasizer.theblog.common.vo.BaseVO;

import java.util.List;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:47
 */
@Data
public class WebConfigVO extends BaseVO<WebConfigVO> {

    /**
     * 网站Logo
     */
    private String logo;

    /**
     * 网站名称
     */
    private String name;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String summary;

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 作者
     */
    private String author;

    /**
     * 备案号
     */
    private String recordNum;

    /**
     * 支付宝收款码FileId
     */
    private String aliPay;

    /**
     * 微信收款码FileId
     */
    private String weixinPay;

    /**
     * 是否开启评论(0:否， 1:是)
     */
    private String startComment;


    // 以下字段不存入数据库，封装为了方便使用

    /**
     * 标题图
     */
    @TableField(exist = false)
    private List<String> photoList;

    /**
     * 支付宝付款码
     */
    @TableField(exist = false)
    private String aliPayPhoto;

    /**
     * 微信付款码
     */
    @TableField(exist = false)
    private String weixinPayPhoto;

}
