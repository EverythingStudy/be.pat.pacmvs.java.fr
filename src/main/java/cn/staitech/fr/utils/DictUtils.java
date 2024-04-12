package cn.staitech.fr.utils;

import java.util.List;

import cn.staitech.common.core.constant.Constants;
import cn.staitech.common.core.utils.SpringUtils;
import cn.staitech.common.core.utils.StringUtils;
import cn.staitech.common.redis.service.RedisService;
import cn.staitech.fr.vo.diagnosis.SysDictResultVo;
import cn.staitech.fr.vo.diagnosis.VisceraVo;

/**
 * 字典工具类
 * 
 * @author staitech
 */
public class DictUtils {
	
	/**
	 * 设置字典缓存
	 * 
	 * @param key
	 *            参数键
	 * @param dictDatas
	 *            字典数据列表
	 */
	public static void setSysDictResultVoCache(String key, SysDictResultVo sysDictResultVo) {
		SpringUtils.getBean(RedisService.class).setCacheObject(getCacheKey(key), sysDictResultVo);
	}

	/**
	 * 获取字典缓存
	 * 
	 * @param key
	 *            参数键
	 * @return dictDatas 字典数据列表
	 */
	public static SysDictResultVo getSysDictResultVoCache(String key) {
		SysDictResultVo vo = SpringUtils.getBean(RedisService.class).getCacheObject(getCacheKey(key));
		if (StringUtils.isNotNull(vo)) {
			return vo;
		}
		return null;
	}
	
	
	/**
	 * 删除指定字典缓存
	 * 
	 * @param key
	 *            字典键
	 */
	public static void removeDictCache(String key) {
		SpringUtils.getBean(RedisService.class).deleteObject(getCacheKey(key));
	}


	/**
	 * 设置cache key
	 * 
	 * @param configKey
	 *            参数键
	 * @return 缓存键key
	 */
	public static String getCacheKey(String configKey) {
		return Constants.SYS_DICT_KEY + configKey;
	}
	
	
	/**
	 * 设置字典缓存
	 * 
	 * @param key
	 *            参数键
	 * @param dictDatas
	 *            字典数据列表
	 */
	public static void setVisceraVoCache(String key, List<VisceraVo> list) {
		SpringUtils.getBean(RedisService.class).setCacheObject(getCacheKey(key), list);
	}

	/**
	 * 获取字典缓存
	 * 
	 * @param key
	 *            参数键
	 * @return dictDatas 字典数据列表
	 */
	public static List<VisceraVo> getVisceraVoCache(String key) {
		List<VisceraVo> list = SpringUtils.getBean(RedisService.class).getCacheObject(getCacheKey(key));
		if (StringUtils.isNotNull(list)) {
			return list;
		}
		return null;
	}
}
