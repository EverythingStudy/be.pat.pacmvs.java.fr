package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.redis.service.RedisService;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.AccessViewRecords;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.mapper.AccessViewRecordsMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.service.AccessViewRecordsService;
import cn.staitech.fr.utils.DaysOfYear;
import cn.staitech.fr.vo.project.AccessViewStatisticsReq;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private RedisService redisService;

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

    private static final String ACCESS_VIEW_STATISTICS = "access_view_statistics";
    @Override
    public R accessViewStatistics(AccessViewStatisticsReq req) {
        List<String> allDaysOfCurrentYear = DaysOfYear.getAllDaysOfCurrentYear(req.getYear());

        //redisService.setCacheObject(ACCESS_VIEW_STATISTICS , allDaysOfCurrentYear);

        QueryWrapper<AccessViewRecords> accessViewRecordsWrapper = new QueryWrapper<>();
        accessViewRecordsWrapper.select("DATE(access_time) as mtime", "count(*) as mcount");
        accessViewRecordsWrapper.eq("user_id", SecurityUtils.getUserId());
        accessViewRecordsWrapper.apply(String.format("YEAR(access_time) = %S", req.getYear() == null ? "YEAR(NOW())" : req.getYear()));
        accessViewRecordsWrapper.groupBy("DATE(access_time)");

        List<AccessViewRecords> accessViewRecords = this.list(accessViewRecordsWrapper);

        //List<Map<String, Object>> accessViewRecords = this.listMaps(accessViewRecordsWrapper);
        Map<String, Long> rMap = accessViewRecords.stream()
                .collect(Collectors.toMap(
                        AccessViewRecords::getMtime,
                        AccessViewRecords::getMcount
                        ));

        // 创建一个动态二维数组
        List<List<Object>> dynamicArray = new ArrayList<>(allDaysOfCurrentYear.size());
        for (int i = 0; i < allDaysOfCurrentYear.size(); i++) {
            List<Object> obj = new ArrayList<>();
            obj.add(allDaysOfCurrentYear.get(i)); //日期
            if(rMap.containsKey(allDaysOfCurrentYear.get(i))){
                obj.add(rMap.get(allDaysOfCurrentYear.get(i))); //访问次数
            } else {
                obj.add(0); //访问次数
            }
            dynamicArray.add(obj);
        }
        return R.ok(dynamicArray);
    }
}
