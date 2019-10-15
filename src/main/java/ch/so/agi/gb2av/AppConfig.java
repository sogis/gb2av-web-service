package ch.so.agi.gb2av;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.processor.idempotent.FileIdempotentRepository;
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

    @Value("${app.idempotentFileUrl}")
    private String idempotentFileUrl;

    @Bean
    public FileIdempotentRepository fileConsumerRepo() {
        FileIdempotentRepository fileConsumerRepo = null;
        try {
            fileConsumerRepo = new FileIdempotentRepository();
            fileConsumerRepo.setFileStore(new File(idempotentFileUrl));
            fileConsumerRepo.setCacheSize(5000);
            fileConsumerRepo.setMaxFileStoreSize(51200000);
        } catch (Exception e) {
            log.error("Caught exception inside Creating fileConsumerRepo ..." + e.getMessage());
        }
        if (fileConsumerRepo == null) {
            log.error("fileConsumerRepo == null ...");
        }
        return fileConsumerRepo;
    }
    
    // https://github.com/apache/camel/blob/master/components/camel-sql/src/main/java/org/apache/camel/processor/idempotent/jdbc/AbstractJdbcMessageIdRepository.java
    // https://github.com/apache/camel/blob/master/components/camel-sql/src/main/java/org/apache/camel/processor/idempotent/jdbc/JdbcMessageIdRepository.java
    @Bean
    public JdbcMessageIdRepository jdbcConsumerRepo() {
        JdbcMessageIdRepository jdbcConsumerRepo = null;
        jdbcConsumerRepo = new JdbcMessageIdRepository();
//        jdbcConsumerRepo.setJdbcTemplate();
//        JdbcTemplate asdaf;
//        jdbcConsumerRepo.setDataSource(dataSource);
        
        
        return null;
        
    }
}
