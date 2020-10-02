package ch.so.agi.gb2av;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.rometools.rome.feed.rss.Channel;

@RestController
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.awsBucketName}")
    private String awsBucketName;

    private String awsBaseUrl = "https://s3.eu-central-1.amazonaws.com/";

    @RequestMapping(value="/ping", method=RequestMethod.GET)
    public String ping() {
    	return "gb2av-web-service";
    }
    
    @RequestMapping(value="/gb2av/rss.xml", method=RequestMethod.GET)
    public Channel rss() {

    	Channel channel = new Channel();
        channel.setFeedType("rss_2.0");
        channel.setTitle("GB2AV RSS Feed");
        channel.setDescription("Vollzugsmeldungen Grundbuch - Amtliche Vermessung");
        channel.setLink(ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("gb2av","rss.xml").toUriString());
        
//        Date lastDate = messages.get(0).getImportdate();
//        channel.setPubDate(lastDate);

        return channel;

    }
}
