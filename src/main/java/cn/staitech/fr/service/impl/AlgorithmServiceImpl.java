package cn.staitech.fr.service.impl;

import cn.staitech.fr.domain.Algorithm;
import cn.staitech.fr.mapper.AlgorithmMapper;
import cn.staitech.fr.service.AlgorithmService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * (Algorithm)表服务实现类
 *
 * @author makejava
 * @since 2024-05-10 14:44:26
 */
@Service("algorithmService")
public class AlgorithmServiceImpl extends ServiceImpl<AlgorithmMapper, Algorithm> implements AlgorithmService {
    @Resource
    private AlgorithmMapper algorithmMapper;

    /**
     * 通过ID查询单条数据
     *
     * @param algorithmUuid 主键
     * @return 实例对象
     */
    @Override
    public Algorithm queryById(Integer algorithmUuid) {
        return this.algorithmMapper.queryById(algorithmUuid);
    }

    /**
     * 分页查询
     *
     * @param algorithm   筛选条件
     * @param pageRequest 分页对象
     * @return 查询结果
     */
    @Override
    public Page<Algorithm> queryByPage(Algorithm algorithm, PageRequest pageRequest) {
        long total = this.algorithmMapper.count(algorithm);
        return new PageImpl<>(this.algorithmMapper.queryAllByLimit(algorithm, pageRequest), pageRequest, total);
    }

    /**
     * 新增数据
     *
     * @param algorithm 实例对象
     * @return 实例对象
     */
    @Override
    public Algorithm insert(Algorithm algorithm) {
        this.algorithmMapper.insert(algorithm);
        return algorithm;
    }

    /**
     * 修改数据
     *
     * @param algorithm 实例对象
     * @return 实例对象
     */
    @Override
    public Algorithm update(Algorithm algorithm) {
        this.algorithmMapper.update(algorithm);
        return this.queryById(algorithm.getAlgorithmUuid());
    }

    /**
     * 通过主键删除数据
     *
     * @param algorithmUuid 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer algorithmUuid) {
        return this.algorithmMapper.deleteById(algorithmUuid) > 0;
    }
}
