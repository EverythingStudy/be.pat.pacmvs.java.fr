package cn.staitech.fr.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.fr.domain.SingleOrganNumber;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.OrganDisassemblyQueryIn;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.SingleSlideService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SingleSlideServiceImpl extends ServiceImpl<SingleSlideMapper, SingleSlide>
        implements SingleSlideService {

    @Override
    public List<OrganDisassemblyOut> getSingleList(OrganDisassemblyQueryIn req) {
        List<OrganDisassemblyOut> outList = getBaseMapper().selectSingleOrgan(req);
        if (CollectionUtil.isEmpty(outList)) return outList;
        List<Long> slideIds = outList.stream().map(OrganDisassemblyOut::getSlideId).distinct().collect(Collectors.toList());
        // 根据切片id和脏器id拿到脏器数量
        List<SingleOrganNumber> organNumbers = getBaseMapper().selectNumber(slideIds, req.getCategoryId());
        if (CollectionUtil.isEmpty(organNumbers)) return outList;
        Map<Long, Map<Long, Long>> map = organNumbers.stream()
                .collect(Collectors.groupingBy(
                        SingleOrganNumber::getSlideId,
                        Collectors.toMap(
                                SingleOrganNumber::getCategoryId, SingleOrganNumber::getOrganNumber
                        )
                ));
        outList = outList.stream().peek(p -> p.setOrganNumber(map.get(p.getSlideId()).get(p.getCategoryId())))
                .collect(Collectors.toList());
        return outList;
    }


}




