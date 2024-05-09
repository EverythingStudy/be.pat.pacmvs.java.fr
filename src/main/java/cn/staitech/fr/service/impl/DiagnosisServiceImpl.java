package cn.staitech.fr.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.date.DateUtil;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Diagnosis;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.SysDictData;
import cn.staitech.fr.enums.SysDictTypeEnum;
import cn.staitech.fr.mapper.DiagnosisMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SysDictDataMapper;
import cn.staitech.fr.service.DiagnosisService;
import cn.staitech.fr.service.SysDictDataService;
import cn.staitech.fr.utils.DictUtils;
import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisAbnormalVo;
import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisAddVo;
import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisVo;
import cn.staitech.fr.vo.diagnosis.SysDictDataVo;
import cn.staitech.fr.vo.diagnosis.SysDictResultVo;
import cn.staitech.fr.vo.diagnosis.SysDictTagVo;
import cn.staitech.fr.vo.diagnosis.VisceraVo;
import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisAddVo.DdefinitionChild;
import cn.staitech.system.api.domain.SysUser;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 人工诊断表 服务实现类
 * </p>
 *
 * @author wanglibei
 * @since 2024-04-11
 */
@Service
@Slf4j
public class DiagnosisServiceImpl extends ServiceImpl<DiagnosisMapper, Diagnosis> implements DiagnosisService {


	@Resource
	private SysDictDataMapper sysDictDataMapper;

	@Resource
	private SlideMapper slideMapper;

	@Resource
	private SingleSlideMapper singleSlideMapper;

	@Resource
	private SysDictDataService sysDictDataService;

	@Resource
	private DiagnosisMapper diagnosisMapper;


	@Override
	public List<SpecialDiagnosisVo> getSpecialDiagnosisVo(Long singleId) {

		SingleSlide singleSlide = singleSlideMapper.selectById(singleId);
		Slide slide = slideMapper.selectById(singleSlide.getSlideId());
		List<SpecialDiagnosisVo> voList = new ArrayList<SpecialDiagnosisVo>();

		Diagnosis diagnosisParm = new Diagnosis();
		diagnosisParm.setSpecialId(slide.getSpecialId());
		diagnosisParm.setSingleId(singleId);
		diagnosisParm.setDeleteFlag(1);
		List<Diagnosis> list = diagnosisMapper.selectListByParm(diagnosisParm);

		if (CollectionUtils.isNotEmpty(list)) {
			for (Diagnosis diagn : list) {
				SpecialDiagnosisVo vo = new SpecialDiagnosisVo();
				BeanUtils.copyProperties(diagn, vo);
				Long createBy = diagn.getCreateBy();
				//根据创建人查询名称
				SysUser loginUser = diagnosisMapper.selectUserById(createBy);
				vo.setCreateUser(loginUser.getNickName());
				if (createBy.equals(SecurityUtils.getLoginUser().getSysUser().getUserId())) {
//														if (createBy.equals(1L)) {
					vo.setEditStatus(1);
				}
				voList.add(vo);
			}

		}
		return voList;

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int saveOrUpdateSpecialDiagnosisVo(SpecialDiagnosisAddVo addVo) {
		Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
//		Long currentUserId = 10L;
		
		int checkFlage = 0;
		Long specialDiagnosisId = addVo.getDiagnosisId();
		//根据singleId查询专题id
		SingleSlide singleSlide = singleSlideMapper.selectById(addVo.getSingleId());
		Slide slide = slideMapper.selectById(singleSlide.getSlideId());
		Long specialId = slide.getSpecialId();
		addVo.setSpecialId(specialId);

		if(null != specialDiagnosisId){
			//判断当前人和编辑人是否是同一个人
			//查下当前数据创建人是谁
			Diagnosis specialDiagnosis = diagnosisMapper.selectById(specialDiagnosisId);
			Long createBy = specialDiagnosis.getCreateBy();
			if(!currentUserId.equals(createBy)){
				checkFlage = -2;
			}
		}
		if(checkFlage != 0){
			return  checkFlage;
		}

		long startTime = System.currentTimeMillis();
		Diagnosis record = new Diagnosis();
		//copy
		BeanUtils.copyProperties(addVo, record);
		
//		List<DdefinitionChild> dcList = addVo.getDdefinition();
//		JSONObject jsonObject = new JSONObject();
//        jsonObject.put("ddefinition", dcList);
//		record.setDdefinition(jsonObject);
		
		String viscera =  addVo.getViscera();
		if(null != viscera){
			String visceraName = getLabelNameByParm(SysDictTypeEnum.organization.label(), Long.valueOf(viscera),"");
			record.setViscreaName(visceraName);
		}
		String position =  addVo.getPosition();
		int positionSource =  addVo.getPositionSource();
		if(StringUtils.isNotEmpty(position)) {
			if(positionSource == 0){
				String positionName = getLabelNameByParm(SysDictTypeEnum.position.label(), Long.valueOf(position),"");
				record.setPositionName(positionName);
			}else{
				record.setPositionName(position);
			}
		}
		String lesion =  addVo.getLesion();
		int lesionSource =  addVo.getLesionSource();
		if(StringUtils.isNotEmpty(lesion)) {
			if(lesionSource == 0){
				String lesionName = getLabelNameByParm(SysDictTypeEnum.lesion.label(), Long.valueOf(lesion),"");
				record.setLesionName(lesionName);
			}else{
				record.setLesionName(lesion);
			}
		}
		List<DdefinitionChild> ddefinitionList =  addVo.getDdefinition();
		List<String> nameList = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(ddefinitionList)){
			for(DdefinitionChild child:ddefinitionList){
				String ddefinition = child.getDdefinition();
				int ddefinitionSource = child.getDdefinitionSource();
				if(ddefinitionSource == 0){
					String ddefinitionName = getLabelNameByParm(SysDictTypeEnum.ddefinition.label(), Long.valueOf(ddefinition),"");
					if(StringUtils.isNotEmpty(ddefinitionName)){
						nameList.add(ddefinitionName);
					}
				}else{
					nameList.add(ddefinition);
				}
			}
			
			if(CollectionUtils.isNotEmpty(nameList)){
				String ddefinitionFullName =  String.join("|", nameList);
				record.setDdefinitionName(ddefinitionFullName);
			}
		}

		String grade =  addVo.getGrade();
		int gradeSource =  addVo.getGradeSource();
		if(StringUtils.isNotEmpty(grade)) {
			if(gradeSource == 0){
				String gradeName = getLabelNameByParm(SysDictTypeEnum.grade.label(), Long.valueOf(grade),"");
				record.setGradeName(gradeName);
			}else{
				record.setGradeName(grade);
			}
		}

		if (null == addVo.getDiagnosisId()) {
			//获取分组id
			//			Long slideId = singleSlide.getSlideId();
			if(null != slide.getGroupCode()){
				String groupCode = slide.getGroupCode();
				record.setGroupId(Long.valueOf(groupCode));
			}
			record.setCreateBy(currentUserId);
			record.setCreateTime(DateUtil.date());
			diagnosisMapper.insert(record);
		} else {
			record.setUpdateBy(currentUserId);
			record.setUpdateTime(DateUtil.date());
			diagnosisMapper.updateById(record);
		}
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		log.info("人工诊断单条处理运行时间：{},毫秒 ",totalTime);
		//修改诊断状态
		Diagnosis diagnosis = getById(record.getDiagnosisId());
		SingleSlide singleSlideInfo = new SingleSlide();
		singleSlideInfo.setSingleId(diagnosis.getSingleId());
		//人工诊断状态 0：未诊断；1：已诊断
		singleSlideInfo.setDiagnosisStatus(CommonConstant.DIAGNOSIS_YES);
		singleSlideMapper.updateById(singleSlideInfo);
		return checkFlage;
	}

	@Override
	public List<SysDictDataVo> getCommonTag(String dictType) {
		Map<String, Object> dictMap = new HashMap<>();
		dictMap.put("dictType", dictType);
		List<SysDictDataVo>	list = sysDictDataService.getSysDictDataVoListByParm(dictMap);
		if(CollectionUtils.isNotEmpty(list)){
			for(SysDictDataVo dict : list){
				dict.setDictValueInt(Integer.valueOf(dict.getDictValue()));
			}
		}
		return list;

	}

	@Override
	public List<VisceraVo> getRelationshipTag(String dictType) {
		List<VisceraVo> list = new ArrayList<>();
		Map<String, Object> dictMap = new HashMap<>();
		dictMap.put("dictType", dictType);
		//dictMap.put("dictValue", 1);
		List<SysDictDataVo>	orgainlist = sysDictDataService.getSysDictDataVoListByParm(dictMap);
		if(CollectionUtils.isNotEmpty(orgainlist)){
			for(SysDictDataVo vo : orgainlist){
				VisceraVo visVo = new VisceraVo();
				BeanUtils.copyProperties(vo, visVo);
				String dictValue = vo.getDictValue();
				visVo.setDictValueInt(Integer.valueOf(dictValue));
				//根据sortvalue 查询部位
				Map<String, Object> positionMap = new HashMap<>();
				positionMap.put("dictType", SysDictTypeEnum.position.label());
				if(StringUtils.isNoneEmpty(dictValue)){
					positionMap.put("filter", dictValue);
				}
				List<SysDictDataVo>	positionlist =  new ArrayList<>();
				positionlist = sysDictDataService.getSysDictDataVoListByParm(positionMap);
				if(CollectionUtils.isNotEmpty(positionlist)){
					for(SysDictDataVo pvo : positionlist){
						pvo.setDictValueInt(Integer.valueOf(pvo.getDictValue()));
					}
				}
				visVo.setPositionList(positionlist);
				//根据sortvalue  病理改变
				Map<String, Object> lesionMap = new HashMap<>();
				lesionMap.put("dictType", SysDictTypeEnum.lesion.label());
				if(StringUtils.isNoneEmpty(dictValue)){
					lesionMap.put("filter", dictValue);
				}
				List<SysDictDataVo>	lesionList =  new ArrayList<>();
				lesionList = sysDictDataService.getSysDictDataVoListByParm(lesionMap);
				if(CollectionUtils.isNotEmpty(lesionList)){
					for(SysDictDataVo lvo : lesionList){
						lvo.setDictValueInt(Integer.valueOf(lvo.getDictValue()));
					}
				}
				visVo.setLesionList(lesionList);
				list.add(visVo);
			}
		}
		return list;
	}

	@Override
	public void deleteSpecialDiagnosisVo(Long specialDiagnosisId) {
		Diagnosis diagnosis = diagnosisMapper.selectById(specialDiagnosisId);
		Diagnosis record = new Diagnosis();
		record.setDiagnosisId(specialDiagnosisId);
		record.setDeleteFlag(0);
		diagnosisMapper.updateById(record);

		//查询下当前数据下是否还有数据，如果没有数据了，修改为未诊断
		QueryWrapper<Diagnosis> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("special_id",diagnosis.getSpecialId());
		queryWrapper.eq("single_id",diagnosis.getSingleId());
		//逻辑删除状态（0:删除 1:未删除）
		queryWrapper.eq("delete_flag",1);
		List<Diagnosis> list = list(queryWrapper);
		if (CollectionUtils.isEmpty(list)) {
			//修改未诊断的状态
			SingleSlide singleSlide = new SingleSlide();
			singleSlide.setSingleId(diagnosis.getSingleId());
			//人工诊断状态 0：未诊断；1：已诊断
			singleSlide.setDiagnosisStatus(CommonConstant.DIAGNOSIS_NO);
			singleSlideMapper.updateById(singleSlide);
		}
	}

	@Override
	public Diagnosis getSpecialDiagnosis(Long specialDiagnosisId) {
		Diagnosis diagnosis = getById(specialDiagnosisId);
		return diagnosis;
	}

	@Override
	public SysDictResultVo getSysDictResultVo() {
		//先从缓存获取
		String cacheKeyi = "special_viscera_organization_1";
		SysDictResultVo vo = DictUtils.getSysDictResultVoCache(cacheKeyi);
		//		SysDictResultVo vo = null;
		if(null == vo){
			vo  = new SysDictResultVo();
			Map<Integer,String> labelMap = new HashMap<>();
			labelMap.put(SysDictTypeEnum.ddefinition.value(), SysDictTypeEnum.ddefinition.label());
			labelMap.put(SysDictTypeEnum.grade.value(), SysDictTypeEnum.grade.label());

			for(Map.Entry<Integer,String> entry : labelMap.entrySet()){
				Integer dictTypeKey = entry.getKey();
				String dictTypeStr = entry.getValue();
				List<SysDictDataVo> tagList =  new ArrayList<>();
				tagList = getCommonTag(dictTypeStr);
				if(dictTypeKey == SysDictTypeEnum.ddefinition.value()){
					//2:病理改变 sys_lesion
					vo.setDdefinitionList(tagList);
				}else if(dictTypeKey == SysDictTypeEnum.grade.value()){
					//4:病变级别 sys_grade
					vo.setGradeList(tagList);
				}

			}

			List<VisceraVo> relationshipList =  new ArrayList<>();
			relationshipList = getRelationshipTag(SysDictTypeEnum.organization.label());
			vo.setVisceraList(relationshipList);
			DictUtils.setSysDictResultVoCache(cacheKeyi, vo);
		}
		return vo;
	}

	public List<Object> transArray(String tags) {
		String[] tagArray = tags.split(",");
		List<Object> cdids = new ArrayList<>();
		for (String value: tagArray) {
			cdids.add(Long.valueOf(value));
		}
		return cdids;
	}

	public List<Object> transArray2(String tagName) {
		String[] tagArray = tagName.split(";");
		List<Object> cdids = new ArrayList<>();
		for (String value: tagArray) {
			cdids.add(value);
		}
		return cdids;
	}

	//获取下拉数据
	public List<VisceraVo> getSelectRelationship(String dictType,String filter) {
		//先从缓存读取，如果没有在从库里查询
		String cacheKey = "special_getSelectRelationship_"+dictType+"_"+filter;
		List<VisceraVo>	list = DictUtils.getVisceraVoCache(cacheKey);
		if(CollectionUtils.isEmpty(list)){
			list = new ArrayList<>();
			Map<String, Object> dictMap = new HashMap<>();
			dictMap.put("dictType", dictType);
			dictMap.put("dictValue", filter);

			List<SysDictDataVo>	orgainlist = sysDictDataService.getSysDictDataVoListByParm(dictMap);
			if(CollectionUtils.isNotEmpty(orgainlist)){
				for(SysDictDataVo vo : orgainlist){
					VisceraVo visVo = new VisceraVo();
					BeanUtils.copyProperties(vo, visVo);
					String dictValue = vo.getDictValue();
					visVo.setDictValueInt(Integer.valueOf(dictValue));
					//根据sortvalue 查询部位
					Map<String, Object> positionMap = new HashMap<>();
					positionMap.put("dictType", SysDictTypeEnum.position.label());
					if(StringUtils.isNoneEmpty(dictValue)){
						positionMap.put("filter", dictValue);
					}
					List<SysDictDataVo>	positionlist =  new ArrayList<>();
					positionlist = sysDictDataService.getSysDictDataVoListByParm(positionMap);
					if(CollectionUtils.isNotEmpty(positionlist)){
						for(SysDictDataVo pvo : positionlist){
							pvo.setDictValueInt(Integer.valueOf(pvo.getDictValue()));
						}
					}
					visVo.setPositionList(positionlist);
					//根据sortvalue  病理改变
					Map<String, Object> lesionMap = new HashMap<>();
					lesionMap.put("dictType", SysDictTypeEnum.lesion.label());
					if(StringUtils.isNoneEmpty(dictValue)){
						lesionMap.put("filter", dictValue);
					}
					List<SysDictDataVo>	lesionList =  new ArrayList<>();
					lesionList = sysDictDataService.getSysDictDataVoListByParm(lesionMap);
					if(CollectionUtils.isNotEmpty(lesionList)){
						for(SysDictDataVo lvo : lesionList){
							lvo.setDictValueInt(Integer.valueOf(lvo.getDictValue()));
						}
					}
					visVo.setLesionList(lesionList);
					list.add(visVo);
				}
			}
			//add 缓存
			DictUtils.setVisceraVoCache(cacheKey, list);

		}
		return list;
	}


	public List<Object> transferList(List<Object> list){
		List<Object> retList = new ArrayList<>();
		List<Integer> newList = new ArrayList<>();
		for(int i=0;i<list.size();i++){
			Integer value = (Integer) list.get(i);
			newList.add(value);
		}
		Collections.sort(newList);;//按从小到大排序，只能对基本数据类型的包装对象
		retList.addAll(newList);
		return retList;
	}

	public String getLabelNameByParm(String dictType,Long labelId,String filter){
		String labelName = "";
		SysDictTagVo sysDictTagVo = new SysDictTagVo();
		sysDictTagVo.setDictType(dictType);
		sysDictTagVo.setTagId(String.valueOf(labelId));
		if(StringUtils.isNotEmpty(filter)){
			sysDictTagVo.setFilter(filter);
		}
		SysDictData sysDictData = sysDictDataMapper.getLabelNameByParm(sysDictTagVo);
		if(null != sysDictData){
			labelName = sysDictData.getDictLabel();
		}
		return labelName;
	}

	@Override
	public void abnormalOperation(SpecialDiagnosisAbnormalVo specialDiagnosisAbnormalVo) {
		Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
		SingleSlide singleSlide = new SingleSlide();
		BeanUtils.copyProperties(specialDiagnosisAbnormalVo, singleSlide);
		singleSlide.setAbnormalCreateBy(currentUserId);
		singleSlide.setAbnormalCreateTime(new Date());
		singleSlideMapper.updateById(singleSlide);
	}







}
