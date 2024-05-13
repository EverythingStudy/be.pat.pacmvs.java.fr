package cn.staitech.fr.service;

import cn.staitech.fr.domain.Algorithm;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * (Algorithm)表服务接口
 *
 * @author makejava
 * @since 2024-05-10 14:44:25
 */
public interface AlgorithmService extends IService<Algorithm> {

    /**
     * 通过ID查询单条数据
     *
     * @param algorithmUuid 主键
     * @return 实例对象
     */
    Algorithm queryById(Integer algorithmUuid);

    /**
     * 分页查询
     *
     * @param algorithm   筛选条件
     * @param pageRequest 分页对象
     * @return 查询结果
     */
    Page<Algorithm> queryByPage(Algorithm algorithm, PageRequest pageRequest);

    /**
     * 新增数据
     *
     * @param algorithm 实例对象
     * @return 实例对象
     */
    Algorithm insert(Algorithm algorithm);

    /**
     * 修改数据
     *
     * @param algorithm 实例对象
     * @return 实例对象
     */
    Algorithm update(Algorithm algorithm);

    /**
     * 通过主键删除数据
     *
     * @param algorithmUuid 主键
     * @return 是否成功
     */
    boolean deleteById(Integer algorithmUuid);

}
