package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Api(value = "查询数据整体大小", tags = {"V2.6.1"})
@Slf4j
@RestController
@RequestMapping("/contourJson")
public class ContourJsonController {
	/**
	 * 
	* @Title: list
	* @Description: 根据专题id(必须)+切片id(必须)+脏器id(非必填)
	* @param @param slideId
	* @param @param projectId
	* @param @param organTagId
	* @param @return
	* @return R<JsonFileVo>
	* @throws
	 */
	@GetMapping("/selectList")
    public R<JsonFileVo> list(@RequestParam(value = "slideId") @ApiParam(name = "slideId", value = "切片ID", required = true) Long slideId,
    		@RequestParam(value = "projectId") @ApiParam(name = "projectId", value = "专题id", required = true) Long projectId,
    		@RequestParam(value = "organTagId") @ApiParam(name = "organTagId", value = "脏器id", required = true) Long organTagId) {
        String filePath = File.separator + "home" + File.separator + "data" + File.separator + "aiJson" + File.separator + projectId + File.separator + slideId;
        if(null != organTagId) {
        	//如果脏器id不为空，json目录需要拼接脏器id
        	filePath =  filePath + File.separator + organTagId;
        }
        // 检测filePath目录是否存在，如果存在，则返回该目录下的所有文件名称，如果不存在，则返回null
        JsonFileVo jsonFileVo = getFilesInDirectory(filePath);
        return R.ok(jsonFileVo);
    }
	
    public static JsonFileVo getFilesInDirectory(String directoryPath) {
        Path directory = Paths.get(directoryPath);
        List<String> files = new ArrayList<>();
        //获取文件夹下所有文件总size
        long totalSize = 0;
        try {
            totalSize = Files.walk(directory).filter(Files::isRegularFile).mapToLong(path -> path.toFile().length()).sum();
            files = Files.walk(directory).filter(Files::isRegularFile).map(path -> path.getFileName().toString()).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JsonFileVo.builder().files(files).totalSize(totalSize).build();
    }

    @Data
    @Builder
    static class JsonFileVo{
        @ApiModelProperty(value = "总大小")
        private Long totalSize;
        @ApiModelProperty(value = "文件列表")
        private List<String> files;
    }

}
