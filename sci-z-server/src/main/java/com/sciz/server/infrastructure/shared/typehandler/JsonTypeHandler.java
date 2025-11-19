package com.sciz.server.infrastructure.shared.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JSON 类型处理器
 * 用于处理 PostgreSQL 的 JSON/JSONB 类型与 Java String 之间的转换
 *
 * @author shihangshang
 * @className JsonTypeHandler
 * @date 2025-11-18 10:00
 */
@MappedTypes({String.class})
@MappedJdbcTypes({JdbcType.OTHER})
public class JsonTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("json");
        pgObject.setValue(parameter);
        ps.setObject(i, pgObject);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        if (value == null) {
            return null;
        }
        if (value instanceof PGobject) {
            return ((PGobject) value).getValue();
        }
        return value.toString();
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object value = rs.getObject(columnIndex);
        if (value == null) {
            return null;
        }
        if (value instanceof PGobject) {
            return ((PGobject) value).getValue();
        }
        return value.toString();
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object value = cs.getObject(columnIndex);
        if (value == null) {
            return null;
        }
        if (value instanceof PGobject) {
            return ((PGobject) value).getValue();
        }
        return value.toString();
    }
}



