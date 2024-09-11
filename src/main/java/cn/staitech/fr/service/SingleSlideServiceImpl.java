package cn.staitech.fr.service;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.common.core.utils.bean.BeanUtils;
import cn.staitech.fr.config.MapConstant;
import cn.staitech.fr.constant.Container;
import cn.staitech.fr.domain.Image;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.out.ImageExportOut;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.utils.LanguageUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SingleSlideServiceImpl extends ServiceImpl<SingleSlideMapper, SingleSlide>
        implements SingleSlideService {

    @Resource
    private ImageMapper mapper;


    @Override
    public List<ImageExportOut> getExportList(List<Long> imageIds) {
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Image::getImageId, imageIds);
        List<Image> images = mapper.selectList(wrapper);
        List<ImageExportOut> list = images.stream().map(image -> {
            ImageExportOut exportOut = new ImageExportOut();
            BeanUtils.copyProperties(image, exportOut);
            exportOut.setOrganizationName(MapConstant.getOrganizationName(image.getOrganizationId()));
            String size = image.getSize();
            if (!ObjectUtil.isEmpty(size)) {
                exportOut.setSize(String.format("%.2f", Long.parseLong(size) / (1024.0 * 1024.0)) + "MB");
            }
            if (LanguageUtils.isEn()) {
                exportOut.setFileStatus(Container.IMAGE_STATUS_MAP_EN.get(image.getStatus()));
            } else {
                exportOut.setFileStatus(Container.IMAGE_STATUS_MAP.get(image.getStatus()));
            }
            return exportOut;
        }).collect(Collectors.toList());
        return list;
    }

}