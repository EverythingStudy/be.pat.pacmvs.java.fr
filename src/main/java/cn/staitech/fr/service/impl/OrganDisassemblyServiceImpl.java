package cn.staitech.fr.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.utils.bean.BeanUtils;
import cn.staitech.fr.config.MapConstant;
import cn.staitech.fr.domain.Image;
import cn.staitech.fr.domain.in.ImageVagueQueryIn;
import cn.staitech.fr.domain.in.OrganDisassemblyQueryIn;
import cn.staitech.fr.domain.out.ImageExportOut;
import cn.staitech.fr.domain.out.ImageVagueListOutVO;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.service.OrganDisassemblyService;
import cn.staitech.fr.service.SingleSlideService;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.fr.utils.PageMaster;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author wmy
 * @version 1.0
 * @date 2024/4/2 11:36
 * @description
 */
@Slf4j
@Service
public class OrganDisassemblyServiceImpl implements OrganDisassemblyService {
    @Resource
    private SingleSlideService singleSlideService;
    @Resource
    private HttpServletResponse response;
    @Resource
    private ImageMapper mapper;

    @Override
    public PageResponse<OrganDisassemblyOut> getList(OrganDisassemblyQueryIn req) {
        PageResponse<OrganDisassemblyOut> pageResponse = new PageResponse<>();
        Page<OrganDisassemblyOut> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        // 根据查询条件得到脏器拆分后的数据
        List<OrganDisassemblyOut> disassembly = singleSlideService.getSingleList(req);
        pageResponse.setTotal(page.getTotal());
        pageResponse.setList(disassembly);
        pageResponse.setPages(page.getPages());
        return pageResponse;
    }

    @Override
    public void export(List<Long> imageIds) throws IOException {
        // 拿到原始切片列表
        List<ImageExportOut> disassembly = singleSlideService.getExportList(imageIds);
        Map<String, List<ImageExportOut>> map = disassembly.stream().collect(Collectors.groupingBy(ImageExportOut::getTopicName));
        Set<String> set = map.keySet();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + URLEncoder.encode(MessageSource.M("EXCEL_IMAGE_TITLE"), "utf-8"));
        ServletOutputStream outputStream = response.getOutputStream();
        ExcelWriter excelWriter = EasyExcel.write(outputStream).build();
        for (String code : set) {
            WriteSheet writeSheet = EasyExcel.writerSheet(code).registerConverter(new LongStringConverter())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).head(ImageExportOut.class).build();
            excelWriter.write(map.get(code), writeSheet);
        }
        if (excelWriter != null) {
            excelWriter.finish();
        }

    }

}
