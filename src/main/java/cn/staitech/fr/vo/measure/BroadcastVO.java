package cn.staitech.fr.vo.measure;

import cn.staitech.fr.vo.annotation.Features;
import lombok.Data;

import java.util.List;

@Data
public class BroadcastVO {

    private String type;

    private Long slideId;

    private String annotation_type;

    private Features data;

    private List<Features> dataList;

    private List<PointCount> point_count_list;

}
