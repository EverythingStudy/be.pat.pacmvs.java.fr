package cn.staitech.fr.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.Diagnosis;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.out.DiagnosisInfo;
import cn.staitech.fr.mapper.DiagnosisMapper;
import cn.staitech.fr.service.DiagnosisService;
import cn.staitech.fr.service.SingleSlideService;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisAbnormalVo;
import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisAddVo;
import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisDeleteVo;
import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisVo;
import cn.staitech.fr.vo.diagnosis.SysDictResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * 
* @ClassName: DiagnosisController
* @Description:人工诊断
* @author wanglibei
* @date 2024年4月11日
* @version V1.0
 */
@Slf4j
@Api(value = "人工诊断接口", tags = "人工诊断接口")
@RestController
@Validated
@RequestMapping("/diagnosis")
public class DiagnosisController {

	@Resource
	private DiagnosisService diagnosisService;
	
	@Resource
	private SingleSlideService singleSlideService;
	
	@Resource
	private SlideService slideService;
	
	@Resource
	private DiagnosisMapper diagnosisMapper;
	
	
	/**
	 * 
	* @Title: add
	* @Description: 人工诊断添加
	* @param @param addVo
	* @param @return
	* @param @throws Exception
	* @return R
	* @throws
	 */
	@ApiOperation(value = "添加诊断接口")
//	@Log(title = "人工诊断添加", menu = "人工诊断添加", subMenu = "保存诊断结果", businessType = BusinessType.INSERT)
	@PostMapping("/saveOrUpdate")
	public R saveOrUpdate(@Validated @RequestBody SpecialDiagnosisAddVo addVoList){
		long startTime = System.currentTimeMillis();
		if (null != addVoList) {
			int  checkFlage = diagnosisService.saveOrUpdateSpecialDiagnosisVo(addVoList);
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			log.info("人工诊断程序总运行时间： " + totalTime + " 毫秒");
			if(checkFlage == -1){
				return R.fail(MessageSource.M("PARAMETER_ABNORMALITY"));
			}else if(checkFlage == -2){
				return R.fail(MessageSource.M("DATA_CANNOT_EDITED"));
			}else{
				return R.ok();
			}
			
		}else {
			return R.fail(MessageSource.M("DATA_IS_EMPTY"));
		}
	}

	
	
	@ApiOperation(value = "删除诊断接口")
//	@Log(title = "人工诊断删除", menu = "人工诊断删除", subMenu = "修改诊断结果", businessType = BusinessType.UPDATE)
	@PostMapping("/delete")
	public R delete(@Validated @RequestBody SpecialDiagnosisDeleteVo specialDiagnosisDeleteVo) throws Exception {
		if (null != specialDiagnosisDeleteVo) {
			Diagnosis sid = diagnosisService.getSpecialDiagnosis(specialDiagnosisDeleteVo.getDiagnosisId());
			if(null == sid){
				return R.fail(MessageSource.M("DATA_DOES_NOT_EXIST"));
			}
			//判断当前人和编辑人是否是同一个人
			Long currentUserId = SecurityUtils.getUserId();
			if(!currentUserId.equals(sid.getCreateBy())){
				return R.fail(MessageSource.M("DATA_CANNOT_DELETED"));
			}
			diagnosisService.deleteSpecialDiagnosisVo(specialDiagnosisDeleteVo.getDiagnosisId());
			return R.ok();
		} else {
			return R.fail(MessageSource.M("DATA_DOES_NOT_EXIST"));
		}
	}
	

	/**
	 * 
	* @Title: info
	* @Description: 查询人工诊断列表
	* @param @param specialImageId
	* @param @param projctId
	* @param @param specialId
	* @param @return
	* @return R
	* @throws
	 */
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "查询人工诊断列表")
//	@Log(title = "查询人工诊断列表", menu = "专题阅片", subMenu = "项目列表", businessType = BusinessType.QUERY)
	@GetMapping("/info")
	public R<DiagnosisInfo> info( 
			@RequestParam @ApiParam(name = "singleId", value = "切片id", required = true) Long singleId
			) {
		SingleSlide singleSlide = singleSlideService.getById(singleId);
		//通过项目ID 专题id 切片id 查询所有的诊断结果，返回列表（添加是否可以修改）
		List<SpecialDiagnosisVo> list = diagnosisService.getSpecialDiagnosisVo(singleId);
		DiagnosisInfo diagnosisInfo = new DiagnosisInfo();
		diagnosisInfo.setList(list);
		diagnosisInfo.setAbnormalStatus(singleSlide.getAbnormalStatus());
		return R.ok(diagnosisInfo);
	}

	
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "搜索标签列表")
//	@Log(title = "搜索标签列表",menu = "专题阅片",subMenu = "项目列表-标签搜索",businessType = BusinessType.QUERY)
	@GetMapping("/getAllTag")
	public R getAllTag() {
		SysDictResultVo vo = diagnosisService.getSysDictResultVo();
		return R.ok(vo);
	}
	
	@ApiOperation(value = "未见异常病理改变结果保存/取消")
	@PostMapping("/abnormalOperation")
	public R abnormalOperation(@Validated @RequestBody SpecialDiagnosisAbnormalVo specialDiagnosisAbnormalVo) throws Exception {
		Long singleId = specialDiagnosisAbnormalVo.getSingleId();
		if (null != specialDiagnosisAbnormalVo) {
			SingleSlide singleSlide = singleSlideService.getById(singleId);
			if(null == singleSlide){
				return R.fail(MessageSource.M("DATA_DOES_NOT_EXIST"));
			}
			
			Slide slide = slideService.getById(singleSlide.getSlideId());
			Long specialId = slide.getSpecialId();
			
			Diagnosis diagnosisParm = new Diagnosis();
			diagnosisParm.setSpecialId(specialId);
			diagnosisParm.setSingleId(singleId);
			diagnosisParm.setDeleteFlag(1);
			List<Diagnosis> list = diagnosisMapper.selectListByParm(diagnosisParm);
			//异常状态 0：取消 ；1：未见异常
			if(CollectionUtils.isNotEmpty(list) && specialDiagnosisAbnormalVo.getAbnormalStatus().equals("1")){
				//禁止设置
				throw new Exception(MessageSource.M("EXISTS_DIAGNOSIS_DATA"));
			}
			diagnosisService.abnormalOperation(specialDiagnosisAbnormalVo);
			return R.ok();
		} else {
			return R.fail(MessageSource.M("DATA_DOES_NOT_EXIST"));
		}
	}
}