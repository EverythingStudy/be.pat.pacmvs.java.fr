package cn.staitech.fr.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.constant.Container;
import cn.staitech.fr.domain.Category;
import cn.staitech.fr.domain.PageDataResponse;
import cn.staitech.fr.domain.SingleOrganNumber;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.in.AiDownloadIn;
import cn.staitech.fr.domain.in.AlgorithmIn;
import cn.staitech.fr.domain.in.MatrixReviewEditIn;
import cn.staitech.fr.domain.in.MatrixReviewListIn;
import cn.staitech.fr.domain.in.SingleSlideAdjacent;
import cn.staitech.fr.domain.out.*;
import cn.staitech.fr.feign.PythonOrganRecognitionService;
import cn.staitech.fr.mapper.DiagnosisMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SpecialMapper;
import cn.staitech.fr.service.MatrixReviewService;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.utils.DateUtils;
import cn.staitech.fr.utils.ExportPdfUtils;
import cn.staitech.fr.utils.LanguageUtils;
import cn.staitech.fr.vo.annotation.AiAlgorithm;
import cn.staitech.fr.vo.annotation.StartRecognition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deepoove.poi.data.PictureRenderData;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
    
    @Resource
	private PythonOrganRecognitionService pythonService;


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
        Map<Long, Map<Long, Long>> map = singleOrganNumbers.stream().collect(Collectors.groupingBy(SingleOrganNumber::getSlideId, Collectors.toMap(SingleOrganNumber::getCategoryId, SingleOrganNumber::getOrganNumber)));
        waxList = waxList.stream().peek(p -> p.setOrganNumber(map.get(p.getSlideId()).get(p.getCategoryId()))).collect(Collectors.toList());
        resp.setTotal(page.getTotal());
        resp.setList(waxList);
        resp.setPages(page.getPages());
        return resp;
    }


    @Override
    public HashMap<String, SingleSlideSelectBy> SingleSlideAdjacent(SingleSlideAdjacent req) {
        List<MatrixReviewListOut> waxList = slideMapper.SingleSlideAdjacent(req);
        // 根据下标查询出附近的数据
        AtomicInteger index = new AtomicInteger(0);
        waxList.stream()
                //指定匹配逻辑
                .filter(s -> {
                    //每比对一个元素，数值加1
                    index.getAndIncrement();
                    return s.getSingleId().equals(req.getSingleSlideId());
                }).findFirst();
        HashMap<String, SingleSlideSelectBy> map = new HashMap<>();

        int indexsx = index.get() - 1;
        if (waxList.size() > 0) {
            if (indexsx == 0) {
                if (waxList.size() > 1) {
                    map.put("prev", null);
                    Long singleSlideId = waxList.get(indexsx + 1).getSingleId();
                    SingleSlideSelectBy slideSelectBy = singleSlideMapper.singleSlideBy(singleSlideId);
                    map.put("next", slideSelectBy);
                } else {
                    map.put("prev", null);
                    map.put("next", null);
                }
            } else if (waxList.size() - 1 == indexsx) {
                Long singleSlideId = waxList.get(indexsx - 1).getSingleId();
                SingleSlideSelectBy slideSelectBy = singleSlideMapper.singleSlideBy(singleSlideId);
                map.put("prev", slideSelectBy);
                map.put("next", null);
            } else {
                Long singleSlideIdPrev = waxList.get(indexsx - 1).getSingleId();
                SingleSlideSelectBy slideSelectByPrev = singleSlideMapper.singleSlideBy(singleSlideIdPrev);
                map.put("prev", slideSelectByPrev);
                Long singleSlideIdNext = waxList.get(indexsx + 1).getSingleId();
                SingleSlideSelectBy slideSelectByNext = singleSlideMapper.singleSlideBy(singleSlideIdNext);
                map.put("next", slideSelectByNext);
            }
        } else {
            map.put("prev", null);
            map.put("next", null);
        }
        return map;
    }

    @Override
    public List<SingleSlideSelectBy> specialSlideList(SingleSlideAdjacent req){
        return singleSlideMapper.singleSlideList(req);
    }

    @Override
    public PageResponse<SelectImageSlideOut> selectSlideList(MatrixReviewListIn req) {
        PageResponse resp = new PageResponse();
        Page<SelectImageSlideOut> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<SelectImageSlideOut> waxList = slideMapper.selectSlideList(req);
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
        wrapper.like(StringUtils.isNotEmpty(req.getAnimalCode()),Slide::getAnimalCode,req.getAnimalCode());
        wrapper.like(StringUtils.isNotEmpty(req.getGroupCode()),Slide::getGroupCode,req.getGroupCode());
        Page<Slide> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<Slide> slideList = slideMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(slideList)) {
            resp.setTotal(page.getTotal());
            resp.setPageNum(req.getPageNum());
            resp.setPageSize(req.getPageSize());
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
        resp.setPageNum(req.getPageNum());
        resp.setPageSize(req.getPageSize());
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
            if (LanguageUtils.isEn()) {
                exportVO.setColorType(Container.COLOR_TYPE_EN.get(Integer.valueOf(exportVO.getColorType())));
            } else {
                exportVO.setColorType(Container.COLOR_TYPE.get(Integer.valueOf(exportVO.getColorType())));
            }
            List<ExportListVO> collect = diagnosisMapper.getExportListVO(id);
            exportVO.setList(collect);
            exportVO.setTable(collect);
            //会报错{"msg":"TemplateRenderPolicy render error","code":500}
            //exportVO.setImg(new PictureRenderData(800, 200, "D:/image/liangz.png"));
            exportVO.setImg(new PictureRenderData(800, 200, exportVO.getThumbUrl().replace("/file/statics", "/home/pat_saas")));
            String s = waxPath + exportVO.getFileName() + "+" + exportVO.getOrganName() + CommonConstant.WROD_FILE;
            //生成word
            ExportPdfUtils.exportFile(s, exportVO);
            //生成pdf
            //ExportPdfUtils.convertDocx2Pdf(s, s.replace(CommonConstant.WROD_FILE, CommonConstant.PDF_FILE));
            ExportPdfUtils.wordToPdf(s, s.replace(CommonConstant.WROD_FILE, CommonConstant.PDF_FILE));
            pdfName.add(s.replace(CommonConstant.WROD_FILE, CommonConstant.PDF_FILE));
            topicName = exportVO.getTopicName();
        }
        if (ids.size() > 1) {
            log.info("走的压缩包");
            ExportPdfUtils.writePdfZip(pdfName, response, topicName + DateUtils.getCurrentHHmmssString("yyyy-MM-dd HH:mm:ss") + CommonConstant.ZIP_FILE);

        } else {

            ExportPdfUtils.downloadLocal(pdfName.get(0), response);

        }
        for (String s1 : pdfName) {
            if (new File(s1).exists()) {
                FileUtils.delete(new File(s1));
                //FileUtils.delete(new File(s1.replace(CommonConstant.PDF_FILE, CommonConstant.WROD_FILE)));
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

	@Override
	public R algorithm(AlgorithmIn req) {
		Long organizationId  = SecurityUtils.getLoginUser().getSysUser().getOrganizationId();
//		Long organizationId  = 1L;
		Long specialId = req.getSpecialId();
		MatrixReviewListIn mrl = new MatrixReviewListIn();
		mrl.setSpecialId(specialId);
		//0未预测、1预测成功、2预测失败、3预测中
		mrl.setForecastStatus("0");
		List<MatrixReviewListOut> singleSlideList = slideMapper.getMatrixReview(mrl);
		if (CollectionUtils.isEmpty(singleSlideList)) {
			return R.ok();
		}

		//请求算法处理
		if(CollectionUtils.isNotEmpty(singleSlideList)){
			for(MatrixReviewListOut matrixReviewListOut:singleSlideList){
				Long singleId = matrixReviewListOut.getSingleId();
				Long slideId = matrixReviewListOut.getSlideId();
				String imageUrl = matrixReviewListOut.getImageUrl();
				Long categoryId = matrixReviewListOut.getCategoryId();
				String organName = matrixReviewListOut.getOrganName();
				String aiImageUrl = matrixReviewListOut.getImageUrl();
				Long imageId = matrixReviewListOut.getImageId();
				String organizatinName = geNumber(organizationId);
				if(null != slideId && StringUtils.isNotEmpty(imageUrl)){
					if(imageUrl.endsWith("svs")||imageUrl.endsWith("SVS") && !organName.equals("盲肠-回肠-直肠-结肠")){
						//请求算法接口
						try {
							log.info("AI算法请求内容是singleId:{},slideId:{},organizationId:{},imageUrl:{},algorithm_name:{}", singleId,slideId,organizationId,imageUrl,CommonConstant.ALGORITHM_MODEL_NAME);
							AiAlgorithm aiAlgorithm = new AiAlgorithm(singleId, slideId, categoryId, aiImageUrl, imageId);
							//BeanUtils.copyProperties(matrixReviewListOut, aiAlgorithm);
							aiAlgorithm.setAlgorithm_name(CommonConstant.ALGORITHM_MODEL_NAME);
							aiAlgorithm.setOrganizationName(organizatinName);
							aiAlgorithm.setOrganizationId(organizationId);
							log.info("AI算法请求完整数据{}", JSONUtil.toJsonStr(aiAlgorithm));
							String body = pythonService.algorithm(aiAlgorithm);
							log.info("AI算法请求返回数据{}", JSONUtil.toJsonStr(body));
							JSONObject jsonObject = new JSONObject(body);
							Integer code = jsonObject.getInt("code");
							if (code.equals(200)) {
								//修改当前slide分析状态为进行中
								SingleSlide slide = new SingleSlide();
								slide.setSingleId(singleId);
								//0未预测、1预测成功、2预测失败、3预测中
								slide.setForecastStatus("3");
								singleSlideMapper.updateById(slide);
							}
						} catch (Exception e) {
							e.printStackTrace();
							log.error("AI算法请求失败,切片id是{}",slideId);
						}finally {

						}
					}else{
						log.error("AI算法请求失败,切片id是{},图片非svs格式",slideId);
					}
				}
			}
		}
		return R.ok();
	}
	
	public  String geNumber(Long organizationId) {
		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumIntegerDigits(3);
		formatter.setGroupingUsed(false);
		return "C" + formatter.format(organizationId);
	}
}
