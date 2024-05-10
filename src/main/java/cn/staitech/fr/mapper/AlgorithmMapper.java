package cn.staitech.fr.mapper;


import cn.staitech.fr.domain.Algorithm;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;


/**
 * (Algorithm)表数据库访问层
 *
 * @author makejava
 * @since 2024-05-10 14:42:47
 */
public interface AlgorithmMapper extends BaseMapper<Algorithm> {

    /**
     * 通过ID查询单条数据
     *
     * @param algorithmUuid 主键
     * @return 实例对象
     */
    Algorithm queryById(Integer algorithmUuid);

    /**
     * 查询指定行数据
     *
     * @param algorithm 查询条件
     * @param pageable  分页对象
     * @return 对象列表
     */
    List<Algorithm> queryAllByLimit(Algorithm algorithm, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param algorithm 查询条件
     * @return 总行数
     */
    long count(Algorithm algorithm);

    /**
     * 新增数据
     *
     * @param algorithm 实例对象
     * @return 影响行数
     */
    int insert(Algorithm algorithm);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<Algorithm> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<Algorithm> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<Algorithm> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<Algorithm> entities);

    /**
     * 修改数据
     *
     * @param algorithm 实例对象
     * @return 影响行数
     */
    int update(Algorithm algorithm);

    /**
     * 通过主键删除数据
     *
     * @param algorithmUuid 主键
     * @return 影响行数
     */
    int deleteById(Integer algorithmUuid);

}

