package ch.so.agi.gb2av;

import java.nio.ByteBuffer;

import javax.sql.DataSource;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.dataformat.zipfile.ZipSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Gb2avRoute extends RouteBuilder {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.ftpUserInfogrips}")
    private String ftpUserInfogrips;

    @Value("${app.ftpPwdInfogrips}")
    private String ftpPwdInfogrips;

    @Value("${app.ftpUrlInfogrips}")
    private String ftpUrlInfogrips;

    @Value("${app.pathToDownloadFolder}")
    private String pathToDownloadFolder;

    @Value("${app.pathToUnzipFolder}")
    private String pathToUnzipFolder;
    
    @Value("${app.pathToErrorFolder}")
    private String pathToErrorFolder;
    
    @Value("${app.awsBucketName}")
    private String awsBucketName;

    @Value("${app.downloadDelayGb2Av}")
    private String downloadDelayGb2Av;
    
    @Value("${app.downloadDelayAv2Gb}")
    private String downloadDelayAv2Gb;    

    @Value("${app.uploadDelay}")
    private String uploadDelay;
    
    @Value("${app.importDelay}")
    private String importDelay;    
    
    @Value("${app.initialDownloadDelay}")
    private String initialDownloadDelay;

    @Value("${app.initialUploadDelay}")
    private String initialUploadDelay;

    @Value("${app.initialImportDelay}")
    private String initialImportDelay;

    @Value("${app.dbSchema}")
    private String dbSchema;
    
    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPwd;

    @Autowired
    DataSource dataSource;

    @Override
    public void configure() throws Exception {
        // Download Vollzugsmeldungen from Infogrips FTP.
        from("ftp://"+ftpUserInfogrips+"@"+ftpUrlInfogrips+"/\\gb2av\\?password="+ftpPwdInfogrips+"&antInclude=VOLLZUG*.zip&autoCreate=false&noop=true&readLock=changed&stepwise=false&separator=Windows&passiveMode=true&binary=true&delay="+downloadDelayGb2Av+"&initialDelay="+initialDownloadDelay+"&idempotentRepository=#jdbcConsumerRepo&idempotentKey=ftp-${file:name}")
        .routeId("*downloadVollzugsmeldung*")
        .to("file://"+pathToDownloadFolder)
        .split(new ZipSplitter())
        .streaming().convertBodyTo(ByteBuffer.class)
            .choice()
                .when(body().isNotNull())
                    .to("file://"+pathToUnzipFolder) 
            .end()
        .end();   
        
        // Download Mutationstabellen from Infogrips FTP.
        from("ftp://"+ftpUserInfogrips+"@"+ftpUrlInfogrips+"/\\av2gb\\?password="+ftpPwdInfogrips+"&antInclude=*.zip&autoCreate=false&noop=true&readLock=changed&stepwise=false&separator=Windows&passiveMode=true&binary=true&delay="+downloadDelayAv2Gb+"&initialDelay="+initialDownloadDelay+"&idempotentRepository=#jdbcConsumerRepo&idempotentKey=ftp-${file:name}-${file:size}-${file:modified}")
        .routeId("*downloadMutationstabelle*")
        .to("file://"+pathToDownloadFolder)
        .split(new ZipSplitter())
        .streaming().convertBodyTo(ByteBuffer.class)
            .choice()
                .when(body().isNotNull())
                    .to("file://"+pathToUnzipFolder) 
            .end()
        .end();        

        // Upload files to S3.
        from("file://"+pathToUnzipFolder+"/?noop=true&delay="+uploadDelay+"&initialDelay="+initialUploadDelay+"&readLock=changed&idempotentRepository=#jdbcConsumerRepo&idempotentKey=s3-${file:name}")
        .routeId("*uploadS3*")
        .convertBodyTo(byte[].class)
        .setHeader(AWS2S3Constants.CONTENT_LENGTH, simple("${in.header.CamelFileLength}"))
        .setHeader(AWS2S3Constants.KEY,simple("${in.header.CamelFileNameOnly}"))
        .setHeader(AWS2S3Constants.ACL,simple("public-read")) 
        .to("aws2-s3://" + awsBucketName
                + "?deleteAfterWrite=false&region=eu-central-1" 
                + "&useDefaultCredentialsProvider=true"
//                + "&accessKey={{ftpUrlInfogrips}}"
//                + "&secretKey=RAW({{awsSecretKey}})"
                )
        .log(LoggingLevel.INFO, "File uploaded: ${in.header.CamelFileNameOnly}");
        
        // Import
        IlivalidatorPredicate isValid = new IlivalidatorPredicate(); 
        Ili2pgReplaceProcessor ili2pgProcessor = new Ili2pgReplaceProcessor();
        
        from("file://"+pathToUnzipFolder+"/?noop=true&include=.*\\.xml&delay="+importDelay+"&initialDelay="+initialImportDelay+"&readLock=changed&idempotentRepository=#jdbcConsumerRepo&idempotentKey=ili2pg-${file:name}")
        .routeId("*import*")
        .setProperty("datasource", constant(dataSource))
        .setProperty("dbschema", constant(dbSchema))
        .setProperty("dbusr", constant(dbUser))
        .setProperty("dbpwd", constant(dbPwd))
        .setProperty("dataset", simple("${file:onlyname.noext}"))
        .choice()
            .when(isValid).process(ili2pgProcessor)
            .otherwise().to("file://"+pathToErrorFolder)
        .end();  
    }
}
