package cn.staitech.fr.service.impl;

import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.AccessViewRecords;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.mapper.AccessViewRecordsMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.service.AccessViewRecordsService;
import cn.staitech.fr.utils.DaysOfYear;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 访问view页面次数记录首页日活 服务实现类
 * </p>
 *
 * @author jiazx
 * @since 2025-08-19
 */
@Service
public class AccessViewRecordsServiceImpl extends ServiceImpl<AccessViewRecordsMapper, AccessViewRecords> implements AccessViewRecordsService {

    @Autowired
    private SlideMapper slideMapper;

    @Override
    public void saveAccessViewRecords(Long slideId) {
        Long userId = SecurityUtils.getUserId();

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String day = simpleDateFormat.format(date);

        LambdaQueryWrapper<AccessViewRecords> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessViewRecords::getUserId, userId);
        queryWrapper.eq(AccessViewRecords::getSlideId, slideId);
        queryWrapper.apply("date_format(access_time,'%Y-%m-%d') = '"+day+"'");

        List<AccessViewRecords> accessViewRecordsList = baseMapper.selectList(queryWrapper);
        if(accessViewRecordsList.isEmpty()) {
            Slide slide = slideMapper.selectById(slideId);
            if (null != slide) {
                AccessViewRecords accessViewRecords = new AccessViewRecords();
                accessViewRecords.setSlideId(slideId);
                accessViewRecords.setProjectId(slide.getProjectId());
                accessViewRecords.setAccessTime(new Date());
                accessViewRecords.setUserId(userId);
                baseMapper.insert(accessViewRecords);
            }
        }
    }
}
