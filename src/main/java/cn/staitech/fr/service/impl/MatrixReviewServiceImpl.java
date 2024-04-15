package cn.staitech.fr.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.utils.bean.BeanUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Category;
import cn.staitech.fr.domain.Diagnosis;
import cn.staitech.fr.domain.PageDataResponse;
import cn.staitech.fr.domain.SingleOrganNumber;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.in.AiDownloadIn;
import cn.staitech.fr.domain.in.MatrixReviewEditIn;
import cn.staitech.fr.domain.in.MatrixReviewListIn;
import cn.staitech.fr.domain.out.AnimalDimensionData;
import cn.staitech.fr.domain.out.AnimalDimensionOut;
import cn.staitech.fr.domain.out.ExportListVO;
import cn.staitech.fr.domain.out.ExportVO;
import cn.staitech.fr.domain.out.MatrixReviewListOut;
import cn.staitech.fr.domain.out.MatrixReviewOut;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;
import cn.staitech.fr.domain.out.OrgansData;
import cn.staitech.fr.domain.out.WaxBlockNumberListOut;
import cn.staitech.fr.mapper.DiagnosisMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SpecialMapper;
import cn.staitech.fr.service.MatrixReviewService;
import cn.staitech.fr.utils.DateUtils;
import cn.staitech.fr.utils.ExportPdfUtils;
import cn.staitech.system.api.domain.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deepoove.poi.data.PictureRenderData;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author wudi
 * @Date 2024/4/10 15:54
 * @desc
 */
@Service
@Slf4j
public class MatrixReviewServiceImpl implements MatrixReviewService {
    @Resource
    private SlideMapper slideMapper;
    @Autowired
    private SpecialMapper specialMapper;

    @Resource
    private SingleSlideMapper singleSlideMapper;

    @Resource
    private DiagnosisMapper diagnosisMapper;

    @Resource
    private HttpServletResponse response;


    @Value("${waxPath}")
    private String waxPath;

    @Override
    public R<List<MatrixReviewOut>> groupList(Long specialId) {
        log.info("对照组数据查询接口开始：");
        List<MatrixReviewOut> resp = new ArrayList<>();
        LambdaQueryWrapper<Slide> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Slide::getGroupCode);
        wrapper.eq(Slide::getSpecialId, specialId);
        wrapper.eq(Slide::getDelFlag, CommonConstant.NUMBER_0);
        wrapper.isNotNull(Slide::getGroupCode);
        wrapper.groupBy(Slide::getGroupCode);
        List<Slide> slideList = slideMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(slideList)) {
            resp = slideList.stream().map(e -> {
                MatrixReviewOut matrixReviewOut = new MatrixReviewOut();
                matrixReviewOut.setGroupId(e.getGroupCode());
                matrixReviewOut.setGroupCode(e.getGroupCode());
                return matrixReviewOut;
            }).collect(Collectors.toList());
        }
        return R.ok(resp);
    }

    @Override
    public R edit(MatrixReviewEditIn req) {
        log.info("对照组数据编辑接口开始：");
        Special special = new Special();
        special.setSpecialId(req.getSpecialId());
        special.setControlGroup(req.getGroupId());
        specialMapper.updateById(special);
        return R.ok();
    }

    @Override
    public PageResponse<MatrixReviewListOut> getMatrixReview(MatrixReviewListIn req) {
        log.info("阅片列表单切片维度接口查询开始：");
        //创建响应
        PageResponse resp = new PageResponse();
        //分页查询
        Page<MatrixReviewListOut> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<MatrixReviewListOut> waxList = slideMapper.getMatrixReview(req);
        if (CollectionUtils.isEmpty(waxList)) {
            resp.setTotal(page.getTotal());
            resp.setList(waxList);
            resp.setPages(page.getPages());
            return resp;
        }
        List<Long> slideIds = waxList.stream().map(MatrixReviewListOut::getSlideId).distinct().collect(Collectors.toList());
        List<SingleOrganNumber> singleOrganNumbers = singleSlideMapper.selectNumber(slideIds, req.getCategoryId());
        if (CollectionUtil.isEmpty(singleOrganNumbers)) {
            resp.setTotal(page.getTotal());
            resp.setList(waxList);
            resp.setPages(page.getPages());
            return resp;
        }
        Map<Long, Map<Long, Long>> map = singleOrganNumbers.stream()
                .collect(Collectors.groupingBy(
                        SingleOrganNumber::getSlideId,
                        Collectors.toMap(
                                SingleOrganNumber::getCategoryId, SingleOrganNumber::getOrganNumber
                        )
                ));
        waxList = waxList.stream().peek(p -> p.setOrganNumber(map.get(p.getSlideId()).get(p.getCategoryId())))
                .collect(Collectors.toList());
        resp.setTotal(page.getTotal());
        resp.setList(waxList);
        resp.setPages(page.getPages());
        return resp;
    }

    @Override
    public PageDataResponse<AnimalDimensionOut> animalList(MatrixReviewListIn req) {
        log.info("阅片列表单动物维度接口查询开始：");

        PageDataResponse<AnimalDimensionOut> resp = new PageDataResponse<>();
        AnimalDimensionOut ret = new AnimalDimensionOut();
        //查询基础数据
        LambdaQueryWrapper<Slide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Slide::getDelFlag, CommonConstant.NUMBER_0);
        wrapper.eq(Slide::getSpecialId, req.getSpecialId());
        wrapper.isNotNull(Slide::getAnimalCode);
        Page<AnimalDimensionOut> page = new Page<>(req.getPageNum(), req.getPageSize());
        List<Slide> slideList = slideMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(slideList)) {
            resp.setTotal(page.getTotal());
            resp.setData(ret);
            resp.setPages(page.getPages());
        }
        List<AnimalDimensionData> retData = new ArrayList<>();
        for (Slide slide : slideList) {
            //数据
            AnimalDimensionData out = new AnimalDimensionData();
            out.setGroupCode(slide.getGroupCode());
            out.setAnimalCode(slide.getAnimalCode());
            out.setGenderFlag(slide.getGenderFlag());
            List<OrgansData> organsData = slideMapper.selectRespData(slide.getSlideId());
            out.setOrgans(organsData);
            retData.add(out);
        }
        //表头
        List<Category> headList = slideMapper.selectHeadList(req.getSpecialId());
        ret.setDataList(retData);
        ret.setCategoryList(headList);
        resp.setTotal(page.getTotal());
        resp.setData(ret);
        resp.setPages(page.getPages());
        return resp;
    }

    @Override
    public void diagnosisDownload(AiDownloadIn req) throws Exception {
        log.info("诊断报告下载接口开始：");
        List<Long> ids = req.getIds();
        List<String> pdfName = new ArrayList<>();
        String topicName = "";
        for (Long id : ids) {
            ExportVO exportVO = singleSlideMapper.getExportVO(id);
            List<ExportListVO> collect = diagnosisMapper.getExportListVO(id);
            exportVO.setList(collect);
            exportVO.setTable(collect);
            //exportVO.setImg(new PictureRenderData(800, 200, "D:/image/liangz.png"));
            exportVO.setImg(new PictureRenderData(800, 200, exportVO.getThumbUrl().replace("/file/statics","/home/pat_saas")));
            String s = waxPath + exportVO.getFileName() + "+" + exportVO.getOrganName() +CommonConstant.WROD_FILE;
            //生成word
            ExportPdfUtils.exportFile(s, exportVO);
            //生成pdf
            ExportPdfUtils.convertDocx2Pdf(s, s.replace(CommonConstant.WROD_FILE, CommonConstant.PDF_FILE));
            pdfName.add(s.replace(CommonConstant.WROD_FILE, CommonConstant.PDF_FILE));
            topicName = exportVO.getTopicName();
        }
        if (ids.size() > 1) {
            log.info("走的压缩包");
            ExportPdfUtils.writePdfZip(pdfName, response, topicName +
                    DateUtils.getCurrentHHmmssString("yyyy-MM-dd HH:mm:ss") + CommonConstant.ZIP_FILE);

        } else {

            ExportPdfUtils.downloadLocal(pdfName.get(0), response);

        }
        for (String s1 : pdfName) {
            if (new File(s1).exists()) {
                FileUtils.delete(new File(s1));
            }

        }
        log.info("结束");
    }

    @Override
    public R<String> getControlGroup(Long specialId) {
        log.info("对照组获得接口开始：");
        Special special = specialMapper.selectById(specialId);
        return R.ok(special.getControlGroup());
    }

    @Override
    public void algorithmDownload(AiDownloadIn req) {

    }
}
