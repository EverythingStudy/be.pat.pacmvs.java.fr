package cn.staitech.fr.mapper.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2025/6/16 16:37:48
 */
@Slf4j
public class ListLongTypeHandler extends BaseTypeHandler<Object> {

    CustomJsonTypeHandler delegate = new CustomJsonTypeHandler(new TypeReference<List<Long>>() {});

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        delegate.setNonNullParameter(ps, i, parameter, jdbcType);
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return delegate.getNullableResult(rs, columnName);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return delegate.getNullableResult(rs, columnIndex);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return delegate.getNullableResult(cs, columnIndex);
    }
}
