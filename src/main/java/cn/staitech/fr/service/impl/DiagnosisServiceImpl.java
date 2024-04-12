package cn.staitech.fr.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.date.DateUtil;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Diagnosis;
import cn.staitech.fr.domain.DiagnosisDetail;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.SysDictData;
import cn.staitech.fr.enums.SysDictTypeEnum;
import cn.staitech.fr.mapper.DiagnosisMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SysDictDataMapper;
import cn.staitech.fr.service.DiagnosisDetailService;
import cn.staitech.fr.service.DiagnosisService;
import cn.staitech.fr.service.SysDictDataService;
import cn.staitech.fr.utils.DictUtils;
import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisAddVo;
import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisVo;
import cn.staitech.fr.vo.diagnosis.SysDictDataVo;
import cn.staitech.fr.vo.diagnosis.SysDictResultVo;
import cn.staitech.fr.vo.diagnosis.SysDictTagVo;
import cn.staitech.fr.vo.diagnosis.VisceraVo;
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

	//	@Resource
	//	private ProjectExtMapper projectExtMapper;
	//
	//	@Resource
	//	private SpecialDiagnosisMapper specialDiagnosisMapper;
	//
	//	@Resource
	//	private SpecialDiagnosisDetailMapper specialDiagnosisDetailMapper;
	//

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

	@Resource
	private DiagnosisDetailService   diagnosisDetailService;


	@Override
	public List<SpecialDiagnosisVo> getSpecialDiagnosisVo(Long singleId, Long specialId) {

		List<SpecialDiagnosisVo> voList = new ArrayList<SpecialDiagnosisVo>();

		QueryWrapper<Diagnosis> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("special_id",specialId);
		queryWrapper.eq("single_id",singleId);
		queryWrapper.gt("delete_flag",1);
		queryWrapper.orderByAsc("create_by");

		List<Diagnosis> list = list(queryWrapper);

		if (CollectionUtils.isNotEmpty(list)) {
			for (Diagnosis diagn : list) {
				SpecialDiagnosisVo vo = new SpecialDiagnosisVo();
				BeanUtils.copyProperties(diagn, vo);

				Long diagnosisId = diagn.getDiagnosisId();
				Long createBy = diagn.getCreateBy();
				//根据创建人查询名称
				SysUser loginUser = diagnosisMapper.selectUserById(createBy);
				vo.setCreateUser(loginUser.getNickName());

				//				vo.setIndex(index);

				QueryWrapper<DiagnosisDetail> queryDetailWrapper = new QueryWrapper<>();
				queryDetailWrapper.eq("diagnosis_id",diagnosisId);
				// 查询所有的明细
				List<DiagnosisDetail> detailList = diagnosisDetailService.list(queryDetailWrapper);
				String visceraTag = "";
				for (DiagnosisDetail detail : detailList) {
					String dictType = detail.getDictType();
					String tags = detail.getTags();
					//String tagName = detail.getTagName();
					//根据不同的标签去查询value值
					if (StringUtils.isNotEmpty(tags)) {
						if (dictType.equals(SysDictTypeEnum.organization.label())) {
							vo.setViscera(tags);
						}else if (dictType.equals(SysDictTypeEnum.lesion.label())) {
							vo.setLesion(tags);
						}else if (dictType.equals(SysDictTypeEnum.grade.label())) {
							vo.setGrade(tags);
						}else if (dictType.equals(SysDictTypeEnum.position.label())) {
							vo.setPosition(tags);
						}else if (dictType.equals(SysDictTypeEnum.ddefinition.label())) {
							vo.setDdefinition(tags);
						}
					}
				}

				//根据脏器标签查询他对应的部位和dde的列表
				//				List<VisceraVo> relationshipList =  new ArrayList<>();
				//				relationshipList = getSelectRelationship(SysDictTypeEnum.organization.label(),visceraTag);
				//				vo.setVisceraList(relationshipList);

				if (createBy.equals(SecurityUtils.getLoginUser().getSysUser().getUserId())) {
					vo.setEditStatus(1);
				}
				voList.add(vo);
				//				index++;
			}

		}
		return voList;

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int saveOrUpdateSpecialDiagnosisVo(SpecialDiagnosisAddVo addVo) {
		int checkFlage = 0;
		Long specialDiagnosisId = addVo.getDiagnosisId();
		Long currentUserId = SecurityUtils.getLoginUser().getSysUser().getUserId();
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
		//operType 1：save  2:update
		int operType = 1;
		if (null == addVo.getDiagnosisId()) {
			//获取分组id
			SingleSlide singleSlide = singleSlideMapper.selectById(record.getSingleId());
			Long slideId = singleSlide.getSlideId();
			Slide slide = slideMapper.selectById(slideId);
			String groupCode = slide.getGroupCode();
			record.setGroupId(Long.valueOf(groupCode));
			record.setCreateBy(currentUserId);
			record.setCreateTime(DateUtil.date());
			diagnosisMapper.insert(record);
		} else {
			operType = 2;
			record.setUpdateBy(currentUserId);
			record.setUpdateTime(DateUtil.date());
			diagnosisMapper.updateById(record);
		}
		specialDiagnosisId = record.getDiagnosisId();
		saveOrUpdateDetail(addVo, specialDiagnosisId, operType);
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		log.info("人工诊断单条处理运行时间：{},毫秒 ",totalTime);
		//修改诊断状态
		Diagnosis diagnosis = getById(specialDiagnosisId);
		SingleSlide singleSlide = new SingleSlide();
		singleSlide.setSingleId(diagnosis.getSingleId());
		//人工诊断状态 0：未诊断；1：已诊断
		singleSlide.setDiagnosisStatus(CommonConstant.DIAGNOSIS_YES);
		singleSlideMapper.updateById(singleSlide);
		//是否需要刷新初始化字典的判断
		//		updateInitDictCache(addVoList);
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

		DiagnosisDetail detail = new DiagnosisDetail();
		detail.setDiagnosisId(specialDiagnosisId);
		detail.setDeleteFlag(0);
		diagnosisDetailService.updateById(detail);
		//查询下当前数据下是否还有数据，如果没有数据了，修改为未诊断
		QueryWrapper<Diagnosis> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("special_id",diagnosis.getSpecialId());
		queryWrapper.eq("single_id",diagnosis.getSingleId());
		queryWrapper.gt("delete_flag",1);
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

	/**
	 * 
	 * @Title: saveOrUpdateDetail
	 * @Description: 明细处理
	 * @param @param addVo
	 * @param @param specialDiagnosisId
	 * @param @param operType
	 * @return void
	 * @throws
	 */
	public void saveOrUpdateDetail(SpecialDiagnosisAddVo addVo, long specialDiagnosisId, int operType) {
		long startTime = System.currentTimeMillis();

		if (operType == 2) {
			//先删除所有明细数据
			diagnosisDetailService.removeById(specialDiagnosisId);
		}

		saveDetailInfo(addVo.getViscera(), specialDiagnosisId, SysDictTypeEnum.organization.label(), "");
		saveDetailInfo(addVo.getPosition(), specialDiagnosisId, SysDictTypeEnum.position.label(),String.valueOf(addVo.getViscera()));
		saveDetailInfo(addVo.getLesion(), specialDiagnosisId, SysDictTypeEnum.lesion.label(),String.valueOf(addVo.getViscera()));
		saveDetailInfo(addVo.getDdefinition(), specialDiagnosisId, SysDictTypeEnum.ddefinition.label(),"");
		saveDetailInfo(addVo.getGrade(), specialDiagnosisId, SysDictTypeEnum.grade.label(),"");

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		log.info("人工诊断明细程序运行时间： " + totalTime + " 毫秒");
	}

	/**
	 * 
	 * @Title: saveDetail
	 * @Description:  明细save到数据库
	 * @param @param labelList
	 * @param @param labelWord
	 * @param @param specialDiagnosisId
	 * @param @param dictType
	 * @param @param filter
	 * @param @return
	 * @return SpecialDiagnosisDetail
	 * @throws
	 */
	/*public DiagnosisDetail saveDetail(Long labelId,  Long specialDiagnosisId,String dictType, String filter,String viscera) {
		DiagnosisDetail detail = new DiagnosisDetail();
		detail.setDiagnosisId(specialDiagnosisId);
		detail.setDictType(dictType);
		detail.setTags(labelId+"");
		String labelName = getLabelNameByParm(dictType, labelIds,viscera);
		if(StringUtils.isNotEmpty(labelName) && labelName.contains("其它（自定义）")){
			labelName = labelName.replaceAll("其它（自定义）", labelWord);
		}
		if(StringUtils.isNotEmpty(labelName)){
			detail.setTagName(labelName);
		}
		detail.setCreateTime(DateUtil.date());
			diagnosisDetailService.save(detail);
		return detail;
	}*/

	public DiagnosisDetail saveDetailInfo(Long labelId,  Long specialDiagnosisId,String dictType, String filter) {
		DiagnosisDetail detail = new DiagnosisDetail();
		detail.setDiagnosisId(specialDiagnosisId);
		detail.setDictType(dictType);
		detail.setTags(labelId+"");
		String labelName = getLabelNameByParm(dictType, labelId,filter);
		if(StringUtils.isNotEmpty(labelName)){
			detail.setTagName(labelName);
		}
		detail.setCreateTime(DateUtil.date());
		diagnosisDetailService.save(detail);
		return detail;
	}

	public DiagnosisDetail updateDetailInfo(Long labelId,Long specialDiagnosisId,String dictType, String filter) {
		DiagnosisDetail detail = new DiagnosisDetail();
		detail.setDiagnosisId(specialDiagnosisId);
		detail.setDictType(dictType);

		detail.setTags(labelId+"");
		String labelName = getLabelNameByParm(dictType, labelId,filter);
		if(StringUtils.isNotEmpty(labelName)){
			detail.setTagName(labelName);
		}
		// 先查询当前切片之前的数据，有就修改、没有就添加
		QueryWrapper<DiagnosisDetail> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("dict_type",dictType);
		queryWrapper.eq("diagnosis_id",specialDiagnosisId);

		List<DiagnosisDetail> detailList = diagnosisDetailService.list(queryWrapper);

		if (CollectionUtils.isNotEmpty(detailList)) {
			DiagnosisDetail ail = detailList.get(0);
			DiagnosisDetail upAil = new DiagnosisDetail();
			upAil.setTags(null);
			upAil.setDiagnosisDetailId(ail.getDiagnosisDetailId());
			diagnosisDetailService.updateById(upAil);
		} else {
			diagnosisDetailService.save(detail);
		}
		return detail;
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
		/*if(StringUtils.isEmpty(labelName)){
			log.info("dictType："+dictType+" labelIds:"+labelIds+" 未查询到相关信息");
		}*/
		return labelName;
	}




	/**
	 * 
	 * @Title: updateDetail
	 * @Description: 明细update到数据库
	 * @param @param labelList
	 * @param @param labelWord
	 * @param @param specialDiagnosisId
	 * @param @param dictType
	 * @param @param filter
	 * @param @return
	 * @return SpecialDiagnosisDetail
	 * @throws
	 */
	/*	public DiagnosisDetail updateDetail(List<Object> labelList, String labelWord, Long specialDiagnosisId,
			String dictType, String filter,String viscera) {
		DiagnosisDetail detail = new DiagnosisDetail();
		detail.setDiagnosisId(specialDiagnosisId);
		detail.setDictType(dictType);
		String labelIds = "";

		if (CollectionUtils.isNotEmpty(labelList)) {
			labelIds = labelList.stream().map(String::valueOf).collect(Collectors.joining(","));
		}

		detail.setTags(labelIds);
		String labelName = getLabelNameByParm(dictType, labelIds,viscera);
		if(StringUtils.isNotEmpty(labelName) && labelName.contains("其它（自定义）")){
			labelName = labelName.replaceAll("其它（自定义）", labelWord);
		}
		if(StringUtils.isNotEmpty(labelName)){
			detail.setTagName(labelName);
		}
		detail.setCreateTime(DateUtil.date());
		// 先查询当前切片之前的数据，有就修改、没有就添加
		QueryWrapper<DiagnosisDetail> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("dict_type",dictType);
		queryWrapper.eq("diagnosis_id",specialDiagnosisId);

		List<DiagnosisDetail> detailList = diagnosisDetailService.list(queryWrapper);

		if (CollectionUtils.isNotEmpty(detailList)) {
			DiagnosisDetail ail = detailList.get(0);
			DiagnosisDetail upAil = new DiagnosisDetail();
			upAil.setTags(labelIds);
			upAil.setDiagnosisDetailId(ail.getDiagnosisDetailId());
			diagnosisDetailService.updateById(upAil);
		} else {
			diagnosisDetailService.save(detail);
		}
		return detail;
	}*/



}
