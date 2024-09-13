package cn.staitech.fr.vo.history;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: wangfeng
 * @create: 2024-02-20 18:07:05
 * @Description:
 */
@Data
@AllArgsConstructor
public class Trace {

    /**
     * 用户ID、会话ID
     */
    private Long userId;

    /**
     * 请求ID-可UUID
     */
    private String traceId;

    /**
     * 是否批量接口
     */
    private Boolean isBatch;

    /**
     * 标注ID列表
     */
    private List<TraceNode> nodeList;


    public Trace(Long userId, String traceId, Boolean isBatch) {
        this.userId = userId;
        this.traceId = traceId;
        this.isBatch = isBatch;
        this.nodeList = new ArrayList<>();
    }
}
