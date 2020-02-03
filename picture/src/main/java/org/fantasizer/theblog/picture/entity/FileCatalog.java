package org.fantasizer.theblog.picture.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.fantasizer.common.entity.BaseEntity;

@TableName("t_file_catalog")
@Data
public class FileCatalog extends BaseEntity<FileCatalog> {

    private static final long serialVersionUID = 1L;

    private String projectName;

    private String catalogName;

    private String url;



}
