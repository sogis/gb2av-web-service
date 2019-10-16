package ch.so.agi.gb2av;

import java.net.URI;
import java.nio.ByteBuffer;
import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.s3.S3Constants;
import org.apache.camel.dataformat.zipfile.ZipSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ch.so.agi.camel.predicates.IlivalidatorPredicate;

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
    
    @Value("${app.awsAccessKey}")
    private String awsAccessKey;

    @Value("${app.awsSecretKey}")
    private String awsSecretKey;
    
    @Value("${app.awsBucketName}")
    private String awsBucketName;

    @Value("${app.downloadDelay}")
    private String downloadDelay;

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
        
        // Download form Infogrips FTP.
        from("ftp://"+ftpUserInfogrips+"@"+ftpUrlInfogrips+"/\\gb2av\\?password="+ftpPwdInfogrips+"&antInclude=VOLLZUG*.zip&autoCreate=false&noop=true&readLock=changed&stepwise=false&separator=Windows&passiveMode=true&binary=true&delay="+downloadDelay+"&initialDelay="+initialDownloadDelay+"&idempotentRepository=#jdbcConsumerRepo&idempotentKey=ftp-${file:name}")
        .routeId("_download_")
        .to("file://"+pathToDownloadFolder)
        .split(new ZipSplitter())
        .streaming().convertBodyTo(ByteBuffer.class)
            .choice()
                .when(body().isNotNull())
                    .to("file://"+pathToUnzipFolder) 
            .end()
        .end();        

        // Upload file to S3.
        from("file://"+pathToUnzipFolder+"/?noop=true&delay="+uploadDelay+"&initialDelay="+initialUploadDelay+"&readLock=changed&idempotentRepository=#jdbcConsumerRepo&idempotentKey=s3-${file:name}")
        .routeId("_upload_")
        .convertBodyTo(byte[].class)
        .setHeader(S3Constants.CONTENT_LENGTH, simple("${in.header.CamelFileLength}"))
        .setHeader(S3Constants.KEY,simple("${in.header.CamelFileNameOnly}"))
        .setHeader(S3Constants.CANNED_ACL,simple("PublicRead")) 
        .to("aws-s3://" + awsBucketName
                + "?deleteAfterWrite=false&region=EU_CENTRAL_1" 
                + "&accessKey={{awsAccessKey}}"
                + "&secretKey=RAW({{awsSecretKey}})")
        .log(LoggingLevel.INFO, "File uploaded: ${in.header.CamelFileNameOnly}");
        
        // Import to database.
        // This does not work for all jdbc drivers:
        String url = dataSource.getConnection().getMetaData().getURL();
        String cleanUrl = url.substring(5);
        URI uri = URI.create(cleanUrl);
        String dbHost = uri.getHost();
        String dbPort = String.valueOf(uri.getPort());
        String dbDatabase = uri.getPath().substring(1);
        
        IlivalidatorPredicate isValid = new IlivalidatorPredicate();
        from("file://"+pathToUnzipFolder+"/?noop=true&include=.*\\.xml&delay="+importDelay+"&initialDelay="+initialImportDelay+"&readLock=changed&idempotentRepository=#fileConsumerRepo&idempotentKey=ili2pg-${file:name}")
        .routeId("_import_")
        .choice()
            .when(isValid).toD("ili2pg:import?dbhost="+dbHost+"&dbport="+dbPort+"&dbdatabase="+dbDatabase+"&dbschema="+dbSchema+"&dbusr="+dbUser+"&dbpwd="+dbPwd+"&dataset=${file:onlyname.noext}")
            .otherwise().to("file://"+pathToErrorFolder)
        .end();        
    }
}