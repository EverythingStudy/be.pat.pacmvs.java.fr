package cn.staitech.fr.domain.in;

import lombok.Data;

import java.util.List;

/**
 * AI计算识别json消息
 */
@Data
public class AiMessageBO {
    //图片Id
    private Long slideId;
    private Long imageId;
    //机构ID
    private Long organizationId;
    //脏器ID
    private Long categoryId;
    //单切片ID
    private Long singleSlideId;
    //脏器策略码
    private String algorithmCode;
    private String algorithmName;
    private Integer code;
    private String msg;
    private List<JsonData> data;

    @Data
    public static class JsonData {
        //结构文件
        private String fileUrl;
        //结构编码
        private String structureCode;
        //结构预测状态 0:正常 1:异常
        private Integer code;
        private String msg;
    }
}
