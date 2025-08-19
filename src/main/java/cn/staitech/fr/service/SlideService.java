package cn.staitech.fr.service;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.out.AiInfoListRequest;
import cn.staitech.fr.vo.project.*;
import cn.staitech.fr.vo.project.slide.*;
import cn.staitech.system.api.domain.biz.AddSingleSlide;
import cn.staitech.system.api.domain.biz.DelSingleSlide;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.HashMap;
import java.util.List;

/**
* @author admin
* @description 针对表【fr_slide(项目选片表)】的数据库操作Service
* @createDate 2024-03-29 13:33:37
*/
public interface SlideService extends IService<Slide> {

    R<CustomPage<SlidePageVo>> page(SlidePageReq req, boolean isPageConfigSlide, boolean isAccessPermission);

    R<CustomPage<ImageVO>> choiceImageList(ChoiceImagePageReq image);

    R deleteSlide(Long projectId, List<Long> slideIds) throws  Exception;

    R checkDeleteSlide(Long projectId, List<Long> slideIds) throws  Exception;

    R choiceSave(ProjectImageVo choiceSaveInVo);

    R choiceAll(Long projectId) throws Exception;

    HashMap<String, SlidePageVo> slideAdjacent(SlidePageReq req);

    SlideDetailVo getSlideInfo(Long slideId);

    boolean checkAiExecuted(Long projectId);
    List<String> getAnimalCode(SlideSelectListReq req);

    List<String> getWaxCode(SlideSelectListReq req);

    List<String> getGroupCode(SlideSelectListReq req);

    List<SlideOrganTagVo>  getOrganCode(SlideSelectListReq req);

    /**
     * 查看Ai切片是否分析完成，没有完成返回false，完成返回true
     * @param projectId
     * @return
     */
    boolean isAiSlideFinished(Long projectId);

    R<String> aiAnalysis(AiAnalysisReq req);

    List<ExportAiInfoVo> exportAiInfo(ExportAiInfoReq req);

    OrganCheckVo organCheck(OrganCheckReq req);

    OrganCheckViewVo organCheckView(OrganCheckViewReq req);

    R<String> organCheckConfirm(OrganCheckViewReq req);

    List<OrganTagVO> organList(Long projectId);

    List<AiInfoListResp> getAiInfoList(AiInfoListRequest request);

    Boolean getAiInfoListCheck(Long projectId, Long singleSlideId);

    Long addSingleSlide(AddSingleSlide req);

    int delSingleSlide(DelSingleSlide req);


    R<CustomPage<SlidePageVo>> pageNew(SlidePageReq req);
}
