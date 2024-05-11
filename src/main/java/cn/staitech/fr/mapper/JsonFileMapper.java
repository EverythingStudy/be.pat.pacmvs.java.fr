package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.JsonFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (JsonFile)表数据库访问层
 *
 * @author makejava
 * @since 2024-05-11 13:56:31
 */
public interface JsonFileMapper extends BaseMapper<JsonFile> {

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<JsonFile> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<JsonFile> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<JsonFile> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<JsonFile> entities);

}

