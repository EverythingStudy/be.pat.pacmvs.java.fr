package cn.staitech.fr.service;

import cn.staitech.fr.domain.Species;
import cn.staitech.fr.domain.out.SpeciesOut;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 种属表 服务类
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
public interface SpeciesService extends IService<Species> {

    List<Species> getSpeciesList();
}
