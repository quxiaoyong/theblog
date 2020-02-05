package org.fantasizer.theblog.common.vo;

import lombok.Data;
import org.fantasizer.theblog.common.validator.Messages;
import org.fantasizer.theblog.common.validator.annnotation.LongNotNull;
import org.fantasizer.theblog.common.validator.group.FetchList;

/**
 * 分页信息
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 21:14
 */
@Data
public class PageInfo<T> {

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 当前页
     */
    @LongNotNull(groups = {FetchList.class}, message = Messages.PAGE_NOT_NULL)
    private Long currentPage;

    /**
     * 页大小
     */
    @LongNotNull(groups = {FetchList.class}, message = Messages.SIZE_NOT_NULL)
    private Long pageSize;

}
