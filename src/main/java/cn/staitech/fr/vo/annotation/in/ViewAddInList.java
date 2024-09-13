package cn.staitech.fr.vo.annotation.in;

import lombok.Data;

import java.util.List;

/**
 * 轮廓合并批量操作
 *
 * @author wangfeng
 */
@Data
public class ViewAddInList {
    private List<ViewAddIn> list;
}
