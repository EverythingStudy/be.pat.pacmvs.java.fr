package cn.staitech.fr.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.staitech.common.core.utils.bean.BeanUtils;
import cn.staitech.fr.config.MapConstant;
import cn.staitech.fr.constant.Container;
import cn.staitech.fr.domain.Image;
import cn.staitech.fr.domain.SingleOrganNumber;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.OrganDisassemblyQueryIn;
import cn.staitech.fr.domain.out.ImageExportOut;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;
import cn.staitech.fr.domain.out.SingleSlideSelectBy;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.SingleSlideService;
import cn.staitech.fr.utils.LanguageUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SingleSlideServiceImpl extends ServiceImpl<SingleSlideMapper, SingleSlide>
        implements SingleSlideService {

    @Resource
    private ImageMapper mapper;

    @Resource
    private SingleSlideMapper singleSlideMapper;

    @Override
    public List<OrganDisassemblyOut> getSingleList(OrganDisassemblyQueryIn req) {
        List<OrganDisassemblyOut> outList = getBaseMapper().selectSingleOrgan(req);
        if (CollectionUtil.isEmpty(outList)) {
            return outList;
        }
        List<Long> slideIds = outList.stream().map(OrganDisassemblyOut::getSlideId).distinct().collect(Collectors.toList());
        // 根据切片id和脏器id拿到脏器数量
        List<SingleOrganNumber> organNumbers = getBaseMapper().selectNumber(slideIds, req.getCategoryId());
        if (CollectionUtil.isEmpty(organNumbers)) {
            return outList;
        }
        Map<Long, Map<Long, Long>> map = organNumbers.stream()
                .collect(Collectors.groupingBy(
                        SingleOrganNumber::getSlideId,
                        Collectors.toMap(
                                SingleOrganNumber::getCategoryId, SingleOrganNumber::getOrganNumber
                        )
                ));
        outList = outList.stream().peek(p -> p.setOrganNumber(ObjectUtil.isEmpty(map.get(p.getSlideId())) ? 0L : map.get(p.getSlideId()).get(p.getCategoryId())))
                .collect(Collectors.toList());
        return outList;
    }

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

    @Override
    public SingleSlideSelectBy singleSlideBy(Long singleId) {
        return singleSlideMapper.singleSlideBy(singleId);
    }


}