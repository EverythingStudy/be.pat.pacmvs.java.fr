package cn.staitech.fr.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.date.DateUtil;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.SysDictData;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.SpecialMapper;
import cn.staitech.fr.mapper.SysDictDataMapper;
import cn.staitech.fr.service.SpecialService;
import cn.staitech.fr.service.SysDictDataService;
import cn.staitech.fr.vo.diagnosis.SysDictDataVo;

/**
 * <p>
 * 专题选片表 服务实现类
 * </p>
 *
 * @author wanglibei
 * @since 2023-06-02
 */
@Service
public class SysDictDataServiceImpl implements SysDictDataService {


	@Resource
	private SpecialService specialService;


	@Resource
	private ImageMapper imageMapper;

	@Resource
	private SpecialMapper specialMapper;

	@Resource
	private SysDictDataMapper sysDictDataMapper;


	@Override
	public List<SysDictData> getSysDictDataListByParm(Map<String, Object> map) {
		List<SysDictData> list = sysDictDataMapper.getSysDictDataListByParm(map);
		return list;
	}
	
	@Override
	public List<SysDictDataVo> getSysDictDataVoListByParm(Map<String, Object> map) {
		List<SysDictDataVo> list =  new ArrayList<SysDictDataVo>();
			list = sysDictDataMapper.getSysDictDataVoListByParm(map);
		return list;
	}

	@Override
	public SysDictData getMaxDictSortByParm(Map<String, Object> map) {
		SysDictData sysDictData = sysDictDataMapper.getMaxDictSortByParm(map);
		return sysDictData;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String saveSysDictDataByParm(String dictValueCn, String dictValueEn, String dictType, String filter) {
		SysDictData sysDictData = new SysDictData();
		sysDictData.setDictLabel(dictValueCn);
		sysDictData.setDictLabelEn(dictValueEn);
		sysDictData.setDictType(dictType);
		sysDictData.setCreateBy(SecurityUtils.getUserId());
		sysDictData.setCreateTime(DateUtil.date());
		if(StringUtils.isNoneEmpty(filter)){
			sysDictData.setFilter(filter);
		}
		//获取最大dictSort
		/*Map<String,Object> map = new HashMap<>();
		map.put("dictType", dictType);
		SysDictData maxSysDictData = sysDictDataMapper.getMaxDictSortByParm(map);
		int currentDictSort = 1;
		String dictValue = "0";
		if(null != maxSysDictData){
			currentDictSort = maxSysDictData.getDictSort();
			dictValue = maxSysDictData.getDictValue();
		}
		sysDictData.setDictSort(currentDictSort+1);
		sysDictData.setDictValue(Integer.valueOf(dictValue)+1+"");
		sysDictDataMapper.insertSelective(sysDictData);*/
		String dictValue = insertSysDictData(sysDictData, dictType);
		return dictValue;
	}

	private synchronized  String  insertSysDictData(SysDictData sysDictData,String dictType){
		//获取最大dictSort
		Map<String,Object> map = new HashMap<>();
		map.put("dictType", dictType);
		SysDictData maxSysDictData = sysDictDataMapper.getMaxDictSortByParm(map);
		int currentDictSort = 1;
		String dictValue = "0";
		if(null != maxSysDictData){
			currentDictSort = maxSysDictData.getDictSort();
			dictValue = maxSysDictData.getDictValue();
		}
		sysDictData.setDictSort(currentDictSort+1);
		sysDictData.setDictValue(Integer.valueOf(dictValue)+1+"");
		sysDictDataMapper.insertSelective(sysDictData);
		return sysDictData.getDictValue();
	}
	

	/*@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@Transactional(rollbackFor = Exception.class)
	public  R<String> updateSpecialImageList(AuditSpecialImageVO vo) {
		Long[]  imageIds = vo.getSpecialImageIds();
		if (null == imageIds) {
			return R.fail(SpecialImageConstant.Data_NULL);
		}
		//参数校验
		//审核状态 0：待审核 1：审核通过 2：审核不通过
		int auditStatus = vo.getAuditStatus();
		Map paramMap = new HashMap<>();
		if(auditStatus == 1){
			//确保所选切片全部是待审核或者审核通过的数据
			paramMap.put("auditSucess", auditStatus);
		}else if(auditStatus == 2){
			//确保所选切片全部是待审核或者审核通过的数据
			paramMap.put("auditFail", auditStatus);
		}
		paramMap.put("auditStatus", 0);
		paramMap.put("specialImageIds", imageIds);
		paramMap.put("sliceImageStatus", 2);
		paramMap.put("specialId", vo.getSpecialId());
		List<SpecialImage> list = specialImageMapper.selectSpecialImageListByParm(paramMap);
		if(CollectionUtils.isNotEmpty(list)){
			if (list.size() !=  imageIds.length) {
				if(auditStatus == 1){
					return R.fail(SpecialImageConstant.PASS_ERROR);
				}else{
					return R.fail(SpecialImageConstant.NO_PASS_ERROR);
				}
			}
		}else{
			return R.fail(SpecialImageConstant.Data_NULL);
		}

		SpecialImage record = new SpecialImage();
		BeanUtils.copyProperties(vo,record);
		record.setAuditTime(DateUtil.date());
		//审核状态 2：审核不通过
		if(auditStatus == 2){
			//切图状态改为未切图 record.setEditBy(-1l);
			record.setSliceImageStatus(0);
			record.setEditBy(-SecurityUtils.getUserId());
		}
		record.setUpdateTime(DateUtil.date());
		record.setUpdateBy(SecurityUtils.getUserId());
		//主图修改状态
		specialImageMapper.updateByPrimaryKeySelective(record);

		//针对审核通过的数据，需要根据主图imageid、专题id、批次id查询对应的tb_sub_image所有小的切图，修改审核状态为通过
		//审核状态 1：审核通过
		if(auditStatus == 1){
			List<Long> allSubIds = new ArrayList<>();
			for (SpecialImage sImage : list){	
				//根据主图imageid、专题id、批次id查询对应的tb_sub_image所有小的切图，修改审核状态为通过更新小图审核状态
				//查询小图列表
				Map<String,Object> columnMap = new HashMap<>();
				columnMap.put("parent_image_id", sImage.getImageId());
				columnMap.put("special_id", sImage.getSpecialId());
				columnMap.put("slice_batch_number", sImage.getSliceBatchNumber());
				List<SubImage> subList = subImageMapper.selectByMap(columnMap);
				if(CollectionUtils.isNotEmpty(subList)){
					for (SubImage simage : subList){	
						allSubIds.add(simage.getImageId());
					}
				}
			}
			if(CollectionUtils.isNotEmpty(allSubIds)){
				//批量修改小图状态
				SubImageVo simage = new SubImageVo();
				simage.setImageIds(allSubIds);
				simage.setAuditStatus(auditStatus);
				simage.setUpdateTime(DateUtil.date());
				simage.setUpdateBy(SecurityUtils.getUserId());
				subImageMapper.updateByPrimaryKeySelective(simage);
			}
		}
		return R.ok(OPERATE_SUCCEED);	
	}*/



	/*@SuppressWarnings({"unchecked", "rawtypes" })
	@Override
	public R<String> updateDeliveryBySpecialId(AuditSpecialImageVO vo) {
		//参数校验
		Map paramMap = new HashMap<>();
		paramMap.put("auditFail", 1);
		paramMap.put("specialId", vo.getSpecialId());
		List<SpecialImage> list = specialImageMapper.selectSpecialImageListByParm(paramMap);
		if(CollectionUtils.isEmpty(list)){
			SpecialImage record = new SpecialImage();
			BeanUtils.copyProperties(vo,record);
			record.setAuditStatus(1);
			record.setUpdateTime(DateUtil.date());
			record.setUpdateBy(SecurityUtils.getUserId());
			record.setDeliveryStatus(1);
			specialImageMapper.updateByPrimaryKeySelective(record);
			//修改当前专题交付状态为已交付
			Special special = new Special();
			special.setSpecialId(vo.getSpecialId());
			//交付状态 0：未交付 1：已交付 
			special.setDeliveryStatus(1l);
			special.setUpdateBy(SecurityUtils.getUserId());
			specialService.updateDeliveryStatus(special);
		}else{
			return R.fail(SpecialImageConstant.DELIVERY_FAIL);
		}
		return R.ok(OPERATE_SUCCEED);
	}*/



}
