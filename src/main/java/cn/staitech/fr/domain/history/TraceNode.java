package cn.staitech.fr.domain.history;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: wangfeng
 * @create: 2024-02-20 18:07:05
 * @Description:
 */
@Data
@AllArgsConstructor
public class TraceNode {

    /**
     * 标注ID
     */
    private String id;

    @ApiModelProperty(value = "要执行的操作(UNION:相交,DIFFERENCE:相差,UPDATE:修改,DELETE:删除,添加:INSERT,null)")
    private String operation;
}
