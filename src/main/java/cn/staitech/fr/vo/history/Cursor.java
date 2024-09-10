package cn.staitech.fr.vo.history;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.LinkedList;

/**
 * @author: wangfeng
 * @create: 2024-02-21 18:10:43
 * @Description:
 */
@Data
public class Cursor {
    @ApiModelProperty(value = "undo")
    private Boolean undo = false;

    @ApiModelProperty(value = "redo")
    private Boolean redo = false;

    private Integer undoSiz;

    private Integer redoSize;

    private LinkedList<Trace> drawList = new LinkedList<>();

    private LinkedList<Trace> undoList = new LinkedList<>();

}
