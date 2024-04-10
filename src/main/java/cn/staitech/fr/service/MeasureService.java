package cn.staitech.fr.service;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.fr.domain.Measure;
import cn.staitech.fr.vo.annotation.AnnotationById;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.in.ViewAddIn;
import cn.staitech.fr.vo.measure.MarkingSelectListVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author admin
* @description 针对表【fr_measure】的数据库操作Service
* @createDate 2024-04-09 14:42:38
*/
public interface MeasureService extends IService<Measure> {

    PageResponse<MarkingSelectListVO> list(Long slideId, Integer pageNum, Integer pageSize, String measureFullName) throws Exception;

    List<Features> selectListBy(Long slideId) throws Exception;

    Long insert(ViewAddIn req) throws Exception;

    int delete(Long markingId) throws Exception;


}
