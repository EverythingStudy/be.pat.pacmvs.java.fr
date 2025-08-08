package cn.staitech.fr.service;


import java.util.Map;

/**
 * @author staitech
 */

public interface OrganizationService {


    /**
     * 查询所有机构Map
     *
     * @return
     */
    Map<Long, String> selectMap();

}