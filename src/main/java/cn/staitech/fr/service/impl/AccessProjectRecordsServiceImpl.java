package cn.staitech.fr.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.DictData;
import cn.staitech.fr.domain.AccessProjectRecords;
import cn.staitech.fr.vo.AccessProjectRecordsVo;
import cn.staitech.fr.mapper.AccessProjectRecordsMapper;
import cn.staitech.fr.service.AccessProjectRecordsService;
import cn.staitech.fr.vo.project.AccessProjectRecordReq;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        List<AccessProjectRecords> accessProjectRecordsList = list(Wrappers.<AccessProjectRecords>lambdaQuery()
                .eq(AccessProjectRecords::getUserId, SecurityUtils.getUserId())
                .groupBy(AccessProjectRecords::getProjectId)
                .orderByDesc(AccessProjectRecords::getAccessTime)).stream().limit(5).collect(Collectors.toList());

        List<Long> projectIds = accessProjectRecordsList.stream().map(AccessProjectRecords::getProjectId).collect(Collectors.toList());
        Map<Long, Integer> statusMap = accessProjectRecordsList.stream().collect(Collectors.toMap(AccessProjectRecords::getProjectId, AccessProjectRecords::getStatus));
        Map<Long, Date> rmap = accessProjectRecordsList.stream().collect(Collectors.toMap(AccessProjectRecords::getProjectId, AccessProjectRecords::getAccessTime));
        AccessProjectRecordReq req = new AccessProjectRecordReq();
        req.setProjectIds(projectIds);
        List<AccessProjectRecordsVo> accessProjectRecordsVos = this.getBaseMapper().accessProjectStatistics(req);
        for (AccessProjectRecordsVo accessProjectRecordsVo : accessProjectRecordsVos) {
            if(rmap.containsKey(accessProjectRecordsVo.getSpecialId())) {
                Map<Integer, String> trialType = DictData.TRIAL_TYPE;
                accessProjectRecordsVo.setTrialType(trialType.get(accessProjectRecordsVo.getTrialId()));
                accessProjectRecordsVo.setAccessTime(DateUtil.formatDateTime(rmap.get(accessProjectRecordsVo.getSpecialId())));
                accessProjectRecordsVo.setAnalysisCount(accessProjectRecordsVo.getAnalysisCount() == null ? "0" : accessProjectRecordsVo.getAnalysisCount());
                accessProjectRecordsVo.setAnalysisSum(accessProjectRecordsVo.getAnalysisSum() == null ? "0" : accessProjectRecordsVo.getAnalysisSum());
                accessProjectRecordsVo.setStatus(statusMap.get(accessProjectRecordsVo.getSpecialId()));
            }
        }
        accessProjectRecordsVos.sort((o1, o2) -> o2.getAccessTime().compareTo(o1.getAccessTime()));
        return R.ok(accessProjectRecordsVos);
    }

    @Override
    public R saveAccessProjectRecords(AccessProjectRecords accessProjectRecordsVo){
        //LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        //LocalDateTime todayEnd   = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        //判断用户当天是否已经访问过该项目，如果已访问则不再添加记录
        List<AccessProjectRecords> accessProjectRecords = this.list(new LambdaQueryWrapper<AccessProjectRecords>()
                .eq(AccessProjectRecords::getUserId, accessProjectRecordsVo.getUserId())
                .eq(AccessProjectRecords::getProjectId, accessProjectRecordsVo.getProjectId()));
          //      .between(AccessProjectRecords::getAccessTime, todayStart, todayEnd));

        if(CollectionUtils.isEmpty(accessProjectRecords)) {
            this.save(accessProjectRecordsVo);
        } else {
            //如果存在，则更新访问时间
            accessProjectRecords.get(0).setAccessTime(new Date());
            this.updateById(accessProjectRecords.get(0));
        }
        return R.ok();
    }

}
