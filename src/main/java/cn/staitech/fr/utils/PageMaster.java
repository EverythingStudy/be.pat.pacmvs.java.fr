package cn.staitech.fr.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageSerializable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 分页类
 *
 * @param <T>
 */
@Api(value = "分页类", tags = "分页类")
public class PageMaster<T> extends PageSerializable<T> {

    public static final PageMaster EMPTY = new PageMaster(Collections.emptyList(), 0);

    @ApiModelProperty(value = "页码")
    private int pageNum;

    @ApiModelProperty(value = "每页记录数")
    private int pageSize;

    @ApiModelProperty(value = "总页数")
    private int pages;

    public PageMaster(List<T> list) {
        this(list, 8);
    }

    public PageMaster(List<T> list, int navigatePages) {
        super(list);

        if (list instanceof Page) {
            Page page = (Page) list;
            this.pageNum = page.getPageNum();
            this.pageSize = page.getPageSize();
            this.pages = page.getPages();

        } else if (list instanceof Collection) {
            this.pageNum = 1;
            this.pageSize = list.size();
            this.pages = this.pageSize > 0 ? 1 : 0;
        }

    }

    public static <T> PageMaster<T> of(List<T> list) {
        return new PageMaster(list);
    }

    public static <T> PageMaster<T> of(List<T> list, int navigatePages) {
        return new PageMaster(list, navigatePages);
    }

    public static <T> PageMaster<T> emptyPageMaster() {
        return EMPTY;
    }


    public int getPageNum() {
        return this.pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }


    public int getPages() {
        return this.pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }


    public String toString() {
        String sb = "PageMaster{" + "pageNum=" + this.pageNum +
                ", pageSize=" + this.pageSize +
                ", total=" + this.total +
                ", pages=" + this.pages +
                ", rows=" + this.list;

        return sb;
    }

}
