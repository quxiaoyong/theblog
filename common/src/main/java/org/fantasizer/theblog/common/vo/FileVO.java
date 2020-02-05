package org.fantasizer.theblog.common.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author Cruise Qu
 * @Date 2020-01-29 21:53
 */
@Data
public class FileVO extends BaseVO<FileVO> {
    /**
     * 如果是用户上传，则包含用户uid
     */
    private String userUid;

    /**
     * 如果是管理员上传，则包含管理员uid
     */
    private String adminUid;

    /**
     * 项目名
     */
    private String projectName;

    /**
     * 模块名
     */
    private String sortName;

    /**
     * 图片Url集合
     */
    private List<String> urlList;


}
