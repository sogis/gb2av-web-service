package ch.so.agi.gb2av;

import java.sql.Timestamp;

import org.apache.camel.processor.idempotent.jdbc.AbstractJdbcMessageIdRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Gb2avJdbcMessageIdRepository extends AbstractJdbcMessageIdRepository {

    @Value("${app.dbSchema}")
    private String dbSchema;

    private String queryString = "SELECT COUNT(*) FROM "+dbSchema+".CAMEL_MESSAGEPROCESSED WHERE processorName = ? AND messageId = ?";
    private String insertString = "INSERT INTO "+dbSchema+".CAMEL_MESSAGEPROCESSED (processorName, messageId, createdAt) VALUES (?, ?, ?)";
    private String deleteString = "DELETE FROM "+dbSchema+".CAMEL_MESSAGEPROCESSED WHERE processorName = ? AND messageId = ?";
    private String clearString = "DELETE FROM "+dbSchema+".CAMEL_MESSAGEPROCESSED WHERE processorName = ?";

    @Override
    protected int queryForInt(String key) {
        return jdbcTemplate.queryForObject(getQueryString(), Integer.class, processorName, key);
    }

    @Override
    protected int insert(String key) {
        return jdbcTemplate.update(getInsertString(), processorName, key, new Timestamp(System.currentTimeMillis()));
    }

    @Override
    protected int delete(String key) {
        return jdbcTemplate.update(getDeleteString(), processorName, key);
    }
 
    @Override
    protected int delete() {
        return jdbcTemplate.update(getClearString(), processorName);
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
    
    public String getInsertString() {
        return insertString;
    }

    public void setInsertString(String insertString) {
        this.insertString = insertString;
    }
    
    public String getDeleteString() {
        return deleteString;
    }

    public void setDeleteString(String deleteString) {
        this.deleteString = deleteString;
    }    

    public String getClearString() {
        return clearString;
    }

    public void setClearString(String clearString) {
        this.clearString = clearString;
    }
}
