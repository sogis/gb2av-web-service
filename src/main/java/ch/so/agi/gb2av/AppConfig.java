package ch.so.agi.gb2av;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.io.File;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.processor.idempotent.jdbc.JdbcMessageIdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class AppConfig {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DataSource dataSource;

    @Autowired
    CamelContext camelContext;
        
    @Value("${app.dbSchema}")
    private String dbSchema;
    
    @Bean
    public JdbcMessageIdRepository jdbcConsumerRepo() {
        String tableExistsString = "SELECT 1 FROM "+dbSchema+".CAMEL_MESSAGEPROCESSED WHERE 1 = 0";
        String queryString = "SELECT COUNT(*) FROM "+dbSchema+".CAMEL_MESSAGEPROCESSED WHERE processorName = ? AND messageId = ?";
        String insertString = "INSERT INTO "+dbSchema+".CAMEL_MESSAGEPROCESSED (processorName, messageId, createdAt) VALUES (?, ?, ?)";
        String deleteString = "DELETE FROM "+dbSchema+".CAMEL_MESSAGEPROCESSED WHERE processorName = ? AND messageId = ?";
        String clearString = "DELETE FROM "+dbSchema+".CAMEL_MESSAGEPROCESSED WHERE processorName = ?";
        
        JdbcMessageIdRepository jdbcConsumerRepo = null;
        jdbcConsumerRepo = new JdbcMessageIdRepository(dataSource, "gb2av");
        jdbcConsumerRepo.setCreateTableIfNotExists(false);
        jdbcConsumerRepo.setTableExistsString(tableExistsString);
        jdbcConsumerRepo.setQueryString(queryString);
        jdbcConsumerRepo.setInsertString(insertString);
        jdbcConsumerRepo.setDeleteString(deleteString);
        jdbcConsumerRepo.setClearString(clearString);
        
        return jdbcConsumerRepo;
    }
    
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}
