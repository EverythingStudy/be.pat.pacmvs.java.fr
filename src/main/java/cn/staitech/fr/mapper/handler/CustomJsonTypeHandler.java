package cn.staitech.fr.mapper.handler;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * @author mugw
 * @version 1.0
 * @description 支持泛型的自定义 JSON TypeHandler
 * @date 2025/6/16 15:57:13
 */
@Slf4j
public class CustomJsonTypeHandler extends AbstractJsonTypeHandler<Object> {

    private final TypeReference<?> typeReference;

    public CustomJsonTypeHandler(TypeReference<?> typeReference) {
        Assert.notNull(typeReference, "TypeReference 不能为空");
        this.typeReference = typeReference;
        if (log.isTraceEnabled()) {
            log.trace("CustomJsonTypeHandler initialized with type: {}", typeReference.getType());
        }
    }

    @Override
    protected Object parse(String json) {
        try {
            return getObjectMapper().readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("JSON 反序列化失败: " + json, e);
        }
    }

    @Override
    protected String toJson(Object obj) {
        try {
            return getObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象序列化为 JSON 失败", e);
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, toJson(parameter));
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        if (json == null || json.isEmpty()) {
            return null;
        }
        return parse(json);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        if (json == null || json.isEmpty()) {
            return null;
        }
        return parse(json);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        if (json == null || json.isEmpty()) {
            return null;
        }
        return parse(json);
    }

    // 静态方法获取 ObjectMapper 实例
    private static ObjectMapper OBJECT_MAPPER;

    public static ObjectMapper getObjectMapper() {
        if (OBJECT_MAPPER == null) {
            OBJECT_MAPPER = new ObjectMapper();
        }
        return OBJECT_MAPPER;
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "ObjectMapper should not be null");
        OBJECT_MAPPER = objectMapper;
    }
}
