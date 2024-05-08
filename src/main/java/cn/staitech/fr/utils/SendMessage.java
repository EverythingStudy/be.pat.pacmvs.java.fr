package cn.staitech.fr.utils;

import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.measure.BroadcastVO;
import cn.staitech.fr.vo.measure.PointCount;

import java.util.ArrayList;
import java.util.List;

public class SendMessage {


    public static BroadcastVO sendOneMessages(String status, Features features, List<PointCount> pointCountRes) {
        BroadcastVO broadcast = new BroadcastVO();
        broadcast.setData(features);
        broadcast.setType(status);
        broadcast.setPoint_count_list(pointCountRes);
        return broadcast;

    }

    public static BroadcastVO sendListMessages(String annoType, String status, Features features, List<PointCount> pointCountRes) {
        BroadcastVO broadcast = new BroadcastVO();
        broadcast.setData(features);
        broadcast.setType(status);
        broadcast.setAnnotation_type(annoType);
        broadcast.setPoint_count_list(pointCountRes);
        return broadcast;

    }

    public static BroadcastVO sendOneMessages(String status, Features features) {
        BroadcastVO broadcast = new BroadcastVO();
        broadcast.setData(features);
        broadcast.setType(status);
        broadcast.setPoint_count_list(new ArrayList<>());
        return broadcast;
    }

    public static BroadcastVO sendOneMessagesByAnnoType(String annoType, String status, Features features) {
        BroadcastVO broadcast = new BroadcastVO();
        broadcast.setData(features);
        broadcast.setType(status);
        broadcast.setAnnotation_type(annoType);
        broadcast.setPoint_count_list(new ArrayList<>());
        return broadcast;
    }

    public static BroadcastVO sendPointCountMessages(String status, List<PointCount> pointCount) {
        BroadcastVO broadcast = new BroadcastVO();
        broadcast.setPoint_count_list(pointCount);
        broadcast.setType(status);
        return broadcast;

    }

}
