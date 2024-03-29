package cn.staitech.fr.service;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.fr.domain.Measure;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.in.MarkingUpdateIn;
import cn.staitech.fr.vo.geojson.in.UpdateOperationIn;
import cn.staitech.fr.vo.geojson.in.ViewAddIn;
import cn.staitech.fr.vo.measure.MeasureSelectPageVo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
* @author admin
* @description 针对表【fr_measure】的数据库操作Service
* @createDate 2024-03-29 10:08:34
*/
public interface MeasureService extends IService<Measure> {

    PageResponse<MeasureSelectPageVo> list(Long slideId, Integer pageNum, Integer pageSize, String measureFullName) throws Exception;

    List<Features> selectListBy(Long slideId) throws Exception;

    int delete(Long markingId) throws Exception;

    /**
     * 添加标注
     *
     * @param req 标注数据
     * @return true || false
     */
    Long insert(ViewAddIn req) throws Exception;

    /**
     * 删除标注
     *
     * @param marking 标注数据
     * @return true || false
     */
    Long update(MarkingUpdateIn marking) throws Exception;

    JSONObject updateOperation(UpdateOperationIn req) throws Exception;

    void execlExport(Long slideId, HttpServletResponse response) throws Exception;

    double operationCheck(UpdateOperationIn req) throws Exception;

}
