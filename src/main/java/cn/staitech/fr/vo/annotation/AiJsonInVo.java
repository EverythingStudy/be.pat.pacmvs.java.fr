package cn.staitech.fr.vo.annotation;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AiJsonInVo {

    private String inputGeoJsonFilePath;
    private Long specialId;
    private Long categoryId;
    private Long singleId;
    private Long createBy;
    private Long orgId;
}
