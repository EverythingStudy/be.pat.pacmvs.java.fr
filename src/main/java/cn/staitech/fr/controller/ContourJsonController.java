package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.R;
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

@Slf4j
@RestController
@RequestMapping("/contourJson")
public class ContourJsonController {


//    @GetMapping("/selectList")
//    public R<JsonFileVo> list(@RequestParam(value = "singleSlideId") @ApiParam(name = "singleSlideId", value = "单切片ID", required = true) Long singleSlideId,
//                                @RequestParam(value = "specialId") @ApiParam(name = "specialId", value = "专题id", required = true) Long specialId) {
//        String filePath = File.separator + "home" + File.separator + "data" + File.separator + "aiJson" + File.separator + specialId + File.separator + singleSlideId;
//        // 检测filePath目录是否存在，如果存在，则返回该目录下的所有文件名称，如果不存在，则返回null
//        JsonFileVo jsonFileVo = getFilesInDirectory(filePath);
//        return R.ok(jsonFileVo);
//    }

	@GetMapping("/selectList")
    public R<JsonFileVo> list(@RequestParam(value = "slideId") @ApiParam(name = "slideId", value = "切片ID", required = true) Long slideId,
                                @RequestParam(value = "specialId") @ApiParam(name = "specialId", value = "专题id", required = true) Long specialId) {
        String filePath = File.separator + "home" + File.separator + "data" + File.separator + "aiJson" + File.separator + specialId + File.separator + slideId;
        //通过slideId查询imageId以及waxCode信息
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
            totalSize = Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> path.toFile().length())
                    .sum();
            files = Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JsonFileVo.builder().files(files).totalSize(totalSize).build();
    }

    @Data
    @Builder
    static class JsonFileVo{
        private Long totalSize;
        private List<String> files;
    }

}
