package cn.staitech.fr.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.constant.Container;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.Project;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.AiDownloadIn;
import cn.staitech.fr.domain.out.AipreAirepostOut;
import cn.staitech.fr.domain.out.ExportAiListVO;
import cn.staitech.fr.domain.out.ExportAiVO;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.MatrixReviewService;
import cn.staitech.fr.utils.DateUtils;
import cn.staitech.fr.utils.ExportPdfUtils;
import cn.staitech.fr.utils.LanguageUtils;
import cn.staitech.fr.utils.MathUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
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
	private ProjectMapper projectMapper;

	@Resource
	private SingleSlideMapper singleSlideMapper;

	@Resource
	private SysOrganizationMapper organizationMapper;

	@Resource
	private HttpServletResponse response;


	@Resource
	private AiForecastMapper aiForecastMapper;



	@Value("${waxPath}")
	private String waxPath;


	@Override
	public void algorithmDownload(AiDownloadIn req) throws Exception {
		log.info("ai预测报告导出接口开始：");
		List<Long> ids = req.getIds();
		List<String> pdfName = new ArrayList<>();
		String topicName = "";
		//存放单脏器切片id和脏器id
		Map<Long,Long> categorys=new HashMap<>();
		//判断是不是存在对照组
		Project special = projectMapper.selectById(req.getSpecialId());
		if(StringUtils.isNotEmpty(special.getControlGroup())){
			LambdaQueryWrapper<SingleSlide> wrapper = new LambdaQueryWrapper<>();
			wrapper.in(SingleSlide::getSingleId,ids);
			List<SingleSlide> singleSlides = singleSlideMapper.selectList(wrapper);
			categorys =  singleSlides.stream().collect(Collectors.toMap(SingleSlide::getSingleId,SingleSlide::getCategoryId));
		}
		for (Long id : ids) {
			ExportAiVO exportVO = singleSlideMapper.getExportAiVO(id);

			if (LanguageUtils.isEn()) {
				exportVO.setColorType(Container.COLOR_TYPE_EN.get(Integer.valueOf(exportVO.getColorType())));
			} else {
				exportVO.setColorType(Container.COLOR_TYPE.get(Integer.valueOf(exportVO.getColorType())));
			}
			//算法结果数据填充
			LambdaQueryWrapper<AiForecast> wrapper = new LambdaQueryWrapper<>();
			wrapper.eq(AiForecast::getSingleSlideId, id);
			wrapper.eq(AiForecast::getStructType,CommonConstant.NUMBER_0);
			List<AiForecast> aiForecasts = aiForecastMapper.selectList(wrapper);
			List<ExportAiListVO> collect = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(aiForecasts)) {
				for (AiForecast aiForecast : aiForecasts) {
					ExportAiListVO exportAiListVO = new ExportAiListVO();
					BeanUtils.copyProperties(aiForecast, exportAiListVO);

					if(!CommonConstant.SINGLE_RESULT.equals(aiForecast.getResults())
							&&! (aiForecast.getResults().contains("±"))
							&& new BigDecimal(aiForecast.getResults()).compareTo(BigDecimal.ZERO)<0){
						exportAiListVO.setResults("?");
					}
					//范围数据
					if(StringUtils.isNotEmpty(special.getControlGroup())){
						String genderFlag=singleSlideMapper.getGender(id);
						//setRang(aiForecast.getQuantitativeIndicators(),special,id,exportAiListVO,categorys);
						if(StringUtils.isNotEmpty(genderFlag)){
							setReferenceScope(special, id, exportAiListVO, categorys,genderFlag);
						}

					}
					collect.add( exportAiListVO);
				}
			}
			exportVO.setList(collect);
			exportVO.setTable(collect);
			//算法详情
			AipreAirepostOut aiForecastBySingle = aiForecastMapper.getAiForecastBySingle(id);
			if(ObjectUtils.isNotEmpty(aiForecastBySingle)){
				exportVO.setAlgorithmName(aiForecastBySingle.getAlgorithmName());
				exportVO.setModelVersion(aiForecastBySingle.getModelVersion());
				exportVO.setStartTime(aiForecastBySingle.getStartTime());
				exportVO.setWasteTime(aiForecastBySingle.getWasteTime());
			}

			exportVO.setOrganizationName(organizationMapper.getOrganizationName(SecurityUtils.getLoginUser().getSysUser().getOrganizationId()));
			String s = waxPath + "AI" + File.separator + exportVO.getFileName() + "+" + exportVO.getOrganName() + CommonConstant.WROD_FILE;
			File file = new File(waxPath + "AI" + File.separator);
			if(!file.exists()&&!file.isDirectory()){
				file.mkdirs();
			}
			//生成word
			ExportPdfUtils.exportAiFile(s, exportVO);
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



	/**
	 * 设置参考范围
	 * @param special
	 * @param singleId
	 * @param exportAiListVO
	 * @param categorys
	 * @param genderFlag
	 */
	private void setReferenceScope(Project special, Long singleId, ExportAiListVO exportAiListVO, Map<Long, Long> categorys, String genderFlag) {
		List<BigDecimal> dataList= singleSlideMapper.getReferenceScope(exportAiListVO.getQuantitativeIndicators(),categorys.get(singleId), special.getProjectId(),
				special.getControlGroup(),genderFlag,CommonConstant.NUMBER_0);
		if(CollectionUtils.isNotEmpty(dataList)) {
			if (CollectionUtil.isNotEmpty(dataList)) {
				List<BigDecimal> objects = new ArrayList<>(dataList);
				objects.forEach(e -> {
					if (e.compareTo(BigDecimal.ZERO) < 0) {
						dataList.remove(e);
					}
				});
			}
			if (CollectionUtil.isNotEmpty(dataList)) {
				BigDecimal bigDecimal = MathUtils.calculateAve(dataList.toArray(new BigDecimal[dataList.size()]), 3);
				log.info("平均值" + bigDecimal);
				BigDecimal variance = MathUtils.variance(dataList.toArray(new BigDecimal[dataList.size()]), 3);
				log.info("总体方差" + variance);
				BigDecimal sqrt = MathUtils.sqrt(variance, 3);
				log.info("总体标准差" + sqrt);
				//平均值-标准差
				//BigDecimal subtract = bigDecimal.subtract(sqrt).setScale(3, RoundingMode.UP);
				//平均值+标准差
				//BigDecimal add = bigDecimal.add(sqrt).setScale(3, RoundingMode.UP);
				exportAiListVO.setAverageValue(bigDecimal + "±" + sqrt);

				//正态分布(下限)
				BigDecimal subtract2 = bigDecimal.subtract(new BigDecimal(1.96).multiply(sqrt)).setScale(3, RoundingMode.UP);
			/*if(subtract2.compareTo(BigDecimal.ZERO)<0){
				subtract2=BigDecimal.ZERO;
			}*/
				//正态分布(上限)
				BigDecimal add2 = bigDecimal.add(new BigDecimal(1.96).multiply(sqrt)).setScale(3, RoundingMode.UP);
				if (subtract2.compareTo(BigDecimal.ZERO) >= 0) {
					exportAiListVO.setNormalDistribution(subtract2 + "-" + add2);
				}

			}

		}

	}

}
