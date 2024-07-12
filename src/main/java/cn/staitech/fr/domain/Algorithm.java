package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * (Algorithm)实体类
 *
 * @author makejava
 * @since 2024-05-10 14:44:14
 */
@Data
@TableName(value = "aipre_algorithm")
public class Algorithm implements Serializable {

    private static final long serialVersionUID = -64526478340427455L;

    @TableId(type = IdType.AUTO)
    private Integer algorithmUuid;

    private String algorithmName;

    private String status;

    private Boolean whetherToCompare;

    private String compareGroup;

    private String code;

    private String abbreviation;

    private String createTime;

    private Double minMem;

    private Double videoMemory;

    private String algorithmLesion;

    private String fileType;

    private String modelVersion;

    private String direction;

    private Integer nividiaAmount;

    private String requestUrl;

    private String requestIp;

    private Integer imageSize;

    private Integer timeoutSeconds;
}

