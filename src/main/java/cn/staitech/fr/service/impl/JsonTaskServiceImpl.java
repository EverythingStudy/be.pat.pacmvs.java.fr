package cn.staitech.fr.service.impl;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.enums.ForecastStatusEnum;
import cn.staitech.fr.enums.JsonTaskStatusEnum;
import cn.staitech.fr.mapper.JsonTaskMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.JsonTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * (JsonTask)表服务实现类
 *
 * @author makejava
 * @since 2024-05-10 14:57:01
 */
@Service("jsonTaskService")
@Slf4j
public class JsonTaskServiceImpl extends ServiceImpl<JsonTaskMapper, JsonTask> implements JsonTaskService {
    @Resource
    SingleSlideMapper singleSlideMapper;

    @Override
    public Boolean checkTask(JsonTask jsonTask) {
        jsonTask = this.getOne(new LambdaQueryWrapper<JsonTask>().eq(JsonTask::getSingleId, jsonTask.getSingleId()));
        if (jsonTask == null) {
            return Boolean.FALSE;
        }
        if (JsonTaskStatusEnum.PARSE_SUCCESS.getCode().equals(jsonTask.getStatus())) {
            return Boolean.TRUE;
        }
        jsonTask.setStatus(JsonTaskStatusEnum.PARSE_FAIL.getCode());
        jsonTask.setEndTime(new Date());
        this.updateById(jsonTask);
        log.info("jsonTask id:[{}] singleSlide id:[{}] 修改状态：[{}]", jsonTask.getTaskId(), jsonTask.getSingleId(), JsonTaskStatusEnum.PARSE_FAIL.getCode());
        SingleSlide singleSlide = new SingleSlide();
        singleSlide.setSingleId(jsonTask.getSingleId());
        //0未预测、1预测成功、2预测失败、3预测中
        singleSlide.setForecastStatus(ForecastStatusEnum.FORECAST_FAIL.getCode());
        singleSlide.setStructureTime(jsonTask.getStructureTime());
        singleSlideMapper.updateById(singleSlide);
        log.info("jsonTask id:[{}] singleSlide id:[{}] 修改状态：[{}]", jsonTask.getTaskId(), jsonTask.getSingleId(), ForecastStatusEnum.FORECAST_FAIL.getCode());
        return Boolean.FALSE;
    }
}

