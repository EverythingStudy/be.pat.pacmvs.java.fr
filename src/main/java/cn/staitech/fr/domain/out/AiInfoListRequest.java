package cn.staitech.fr.domain.out;

import lombok.Data;

@Data
public class AiInfoListRequest {

    private Long projectId;
    private Long singleId;
    private String controlGroup;
}
