package cn.staitech.fr.domain.out;

import lombok.Data;

import java.util.Date;

/**
 * @Author wudi
 * @Date 2024/5/10 16:27
 * @desc
 */
@Data
public class AipreAirepostOut {

    private String algorithmName;

    private String modelVersion;

    private  String startTime;

    private  Integer wasteTime;

}
