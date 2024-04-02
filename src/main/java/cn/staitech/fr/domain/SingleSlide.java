package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wmy
 * @version 1.0
 * @date 2024/4/2 13:05
 * @description
 */
@TableName(value = "fr_single_slide")
@Data
public class SingleSlide implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long singleId;

    private Long slideId;

    private String imageUrl;

    private Long categoryId;

}
