package cn.staitech.fr.service;


import cn.staitech.fr.domain.history.Cursor;
import cn.staitech.fr.domain.history.HistoryDTO;
import cn.staitech.fr.domain.history.Session;

/**
 * @author: wangfeng
 * @create: 2024-02-21 11:14:57
 * @Description: 历史记录服务
 */

public interface HistoryService {
    void put(Long userId, Long slideId);

    Session get(Long userId, Long slideId);

    void remove(String key);


    /**
     * 清空Session
     *
     * @param userId
     * @param slideId
     */
    void clearSessionList(Long userId, Long slideId);


    Cursor getCursor(HistoryDTO dto);

    void process(HistoryDTO dto);
}
