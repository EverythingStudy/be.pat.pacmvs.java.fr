package cn.staitech.fr.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.AccessProjectRecords;
import cn.staitech.fr.vo.AccessProjectRecordsVo;
import cn.staitech.fr.mapper.AccessProjectRecordsMapper;
import cn.staitech.fr.service.AccessProjectRecordsService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mugw
 * @version 2.6.0
 * @description 访问记录
 * @date 2025/5/14 13:44:14
 */
@Slf4j
@Service
public class AccessProjectRecordsServiceImpl extends ServiceImpl<AccessProjectRecordsMapper, AccessProjectRecords> implements AccessProjectRecordsService {

    @Scheduled(cron = "10 0 0 * * ?")
    private void delAccessRecords() {
        Date date = DateUtil.offsetMonth(new Date(), -1);
        remove(Wrappers.<AccessProjectRecords>lambdaQuery().ge(AccessProjectRecords::getAccessTime, date));
    }

    @Override
    public R<List<AccessProjectRecordsVo>> accessProjectStatistics() throws Exception {
        Date endDate = new Date();
        Date startDate = DateUtil.offsetMonth(endDate, -1);
        List<String> dateStrings = DateUtil.rangeToList(startDate, endDate, DateField.DAY_OF_MONTH).stream().map(DateUtil::formatDate).collect(Collectors.toList());
        List<AccessProjectRecords> accessProjectRecordsList = list(Wrappers.<AccessProjectRecords>lambdaQuery().between(AccessProjectRecords::getAccessTime, startDate, endDate)
                .eq(AccessProjectRecords::getUserId, SecurityUtils.getUserId()));
        Map<String, Integer> accessProjectRecordsMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(accessProjectRecordsList)){
            //accessProjectRecordsList将访问时间转换为日期，并按日期分组，计算访问数量
            for (AccessProjectRecords accessProjectRecords : accessProjectRecordsList) {
                String accessDate = DateUtil.formatDate(accessProjectRecords.getAccessTime());
                if (accessProjectRecordsMap.containsKey(accessDate)) {
                    accessProjectRecordsMap.put(accessDate, accessProjectRecordsMap.get(accessDate) + 1);
                } else {
                    accessProjectRecordsMap.put(accessDate, 1);
                }
            }
        }
        List<AccessProjectRecordsVo> accessProjectRecordsOuts = new ArrayList<>();
        for (String dateString : dateStrings) {
            AccessProjectRecordsVo accessProjectRecordsOut = new AccessProjectRecordsVo(0,dateString);
            if (accessProjectRecordsMap.containsKey(dateString)) {
                accessProjectRecordsOut.setNum(accessProjectRecordsMap.get(dateString));
            }
            accessProjectRecordsOuts.add(accessProjectRecordsOut);
        }
        return R.ok(accessProjectRecordsOuts);
    }

}
