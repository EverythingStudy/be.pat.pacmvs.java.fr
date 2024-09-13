package cn.staitech.fr.service;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.fr.domain.Measure;
import cn.staitech.fr.vo.annotation.Features;
import cn.staitech.fr.vo.annotation.in.ViewAddIn;
import cn.staitech.fr.vo.measure.MarkingSelectListVO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
* @author admin
* @description 针对表【fr_measure】的数据库操作Service
* @createDate 2024-09-10 11:24:58
*/
public interface MeasureService extends IService<Measure> {


    PageResponse<MarkingSelectListVO> list(Long slideId, Integer pageNum, Integer pageSize, String measureFullName) throws Exception;

    List<Features> selectListBy(Long slideId) throws Exception;

    Long insert(ViewAddIn req) throws Exception;

    int delete(Long markingId) throws Exception;

    void execlExport(Long singleSlideId, HttpServletResponse response) throws Exception;


}
