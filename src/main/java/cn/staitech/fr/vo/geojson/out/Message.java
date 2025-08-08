package cn.staitech.fr.vo.geojson.out;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: wangfeng
 * @create: 2024-02-20 16:03:11
 * @Description: 批量操作返回值
 */
@AllArgsConstructor
@Data
public class Message {

    private String marking_id;
    private String message;
}
