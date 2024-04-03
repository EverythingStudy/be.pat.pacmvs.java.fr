package cn.staitech.fr.service.impl;


import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.history.Cursor;
import cn.staitech.fr.domain.history.HistoryDTO;
import cn.staitech.fr.domain.history.Session;
import cn.staitech.fr.service.AnnotationService;
import cn.staitech.fr.service.HistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: wangfeng
 * @create: 2024-02-20 18:11:25
 * @Description: 会话
 */
@Slf4j
@Service
public class HistoryServiceImpl implements HistoryService {

    public static final ConcurrentHashMap<String, Session> USER_SESSION_MAP = new ConcurrentHashMap<>();

    @Resource
    AnnotationService annotationService;

//    @Resource
//    MarkingExamineService markingExamineService;

    public static Session refreshSession(Long userId, Long slideId) {
        // 撤消,恢复历史记录 用HistoryService会引起循环依赖！ -> 后续在线程池中处理 判断是批处理，还是单独处理
        // 1、创建Session,并存入ConcurrentHashMap<Long, Session>
        Session session = new Session(userId, slideId);
        String key = userId + "_" + slideId;
        if (!HistoryServiceImpl.USER_SESSION_MAP.containsKey(key)) {
            HistoryServiceImpl.USER_SESSION_MAP.put(key, session);
        }
        session = HistoryServiceImpl.USER_SESSION_MAP.get(key);
        return session;
    }

    @Override
    public void put(Long userId, Long slideId) {
        String key = userId + "_" + slideId;
        if (!USER_SESSION_MAP.containsKey(key)) {
            USER_SESSION_MAP.put(key, new Session(userId, slideId));
        }
    }

    @Override
    public Session get(Long userId, Long slideId) {
        String key = userId + "_" + slideId;
        if (USER_SESSION_MAP.containsKey(key)) {
            return USER_SESSION_MAP.get(key);
        }
        return null;
    }

    /**
     * 删除Session
     *
     * @param key
     */
    @Override
    public void remove(String key) {
        if (USER_SESSION_MAP.containsKey(key)) {
            USER_SESSION_MAP.remove(key);
        }
    }

    /**
     * 清空Session中的列表、游标置零、清空rocksDB中的数据
     *
     * @param userId
     */
    @Override
    public void clearSessionList(Long userId, Long slideId) {
        String key = userId + "_" + slideId;
        if (USER_SESSION_MAP.containsKey(key)) {
            USER_SESSION_MAP.get(key).cleanAllList();
        }
    }

    /**
     * 获取撤消、恢复是否可用的状态
     *
     * @param dto
     */
    @Override
    public Cursor getCursor(HistoryDTO dto) {
        String key = dto.getUserId() + "_" + dto.getSlideId();
        Cursor cursor = new Cursor();
        if (USER_SESSION_MAP.containsKey(key)) {
            Session session = USER_SESSION_MAP.get(key);
            cursor = session.getStatus();
        }
        return cursor;
    }

    /**
     * 撤消
     *
     * @param dto
     */
    @Override
    public void process(HistoryDTO dto) {
        switch (dto.getBizType()) {
            case 1:
                switch (dto.getEnvType()) {
                    case 1:
                        annotationService.undo(dto);
                        break;
                    case 2:
                        annotationService.redo(dto);
                        break;
                    default:
                }
                break;
            case 2:
                switch (dto.getEnvType()) {
//                    case 1:
//                        markingExamineService.undo(dto);
//                        break;
//                    case 2:
//                        markingExamineService.redo(dto);
//                        break;
                    default:
                }
                break;
            default:
        }
    }
}
