package cn.staitech.fr.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.out.ContourFileVo;
import cn.staitech.fr.domain.out.JsonFileVo;
import cn.staitech.fr.service.ContourJsonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
@Api(value = "查询数据整体大小", tags = {"V2.6.1"})
@Slf4j
@RestController
@RequestMapping("/contourJson")
public class ContourJsonController {

	@Resource
	private ContourJsonService contourJsonService;

	/**
	 * 
	 * @Title: list
	 * @Description: 根据专题id(必须)+切片id(必须)+脏器id(非必填)用于单脏器下载使用
	 * @param @param slideId
	 * @param @param projectId
	 * @param @param organTagId
	 * @param @return
	 * @return R<JsonFileVo>
	 * @throws
	 */
	@ApiOperation(value = "单脏器json下载", notes = "单脏器json下载")
	@GetMapping("/selectList")
	public R<JsonFileVo> list(@RequestParam(value = "slideId") @ApiParam(name = "slideId", value = "切片ID", required = true) Long slideId,
			@RequestParam(value = "projectId") @ApiParam(name = "projectId", value = "专题id", required = true) Long projectId,
			@RequestParam(value = "organTagId") @ApiParam(name = "organTagId", value = "脏器id", required = true) Long organTagId) {
		return contourJsonService.selectList(slideId, projectId, organTagId);
	}

	/**
	 * 
	 * @Title: getContourJsonSize
	 * @Description: 根据专题id(必须)+切片id(必须)+脏器id(多个脏器)用于获取脏器文件大小使用
	 * @param @param slideId
	 * @param @param projectId
	 * @param @param organTagIds
	 * @param @return
	 * @return R<ContourFileVo>
	 * @throws
	 */
	@ApiOperation(value = "多脏器获取文件大小", notes = "多脏器获取文件大小")
	@GetMapping("/getContourJsonSize")
	public R<ContourFileVo> getContourJsonSize(@RequestParam(value = "slideId") @ApiParam(name = "slideId", value = "切片ID", required = true) Long slideId,
			@RequestParam(value = "projectId") @ApiParam(name = "projectId", value = "专题id", required = true) Long projectId,
			@RequestParam(value = "organTagIds") @ApiParam(name = "organTagIds", value = "脏器id", required = true) List<Long> organTagIds) {
		return contourJsonService.getContourJsonSize(slideId, projectId, organTagIds);
	}
}
