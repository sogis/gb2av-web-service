package ch.so.agi.gb2av;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Item;

@RestController
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.awsBucketName}")
    private String awsBucketName;

//    @Autowired
//    private JdbcTemplate jdbcTemplate;

    private String awsBaseUrl = "https://s3.eu-central-1.amazonaws.com/";

    @RequestMapping(value="/ping", method=RequestMethod.GET)
    public String ping() {
    	return "gb2av-web-service";
    }
    
//    @RequestMapping(value="/gb2av/rss.xml", method=RequestMethod.GET)
//    public Channel rss() {
//
//    	Channel channel = new Channel();
//        channel.setFeedType("rss_2.0");
//        channel.setTitle("GB2AV Vollzugsmeldungen");
//        channel.setDescription("Vollzugsmeldungen Grundbuch - Amtliche Vermessung");
//        channel.setLink(ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("gb2av","rss.xml").toUriString());
//        
//        String sql = "SELECT \n" + 
//        		"    vollzugsgegenstand.t_id,\n" + 
//        		"    vollzugsgegenstand.t_datasetname,\n" + 
//        		"    mutationsnummer.nummer,\n" + 
//        		"    mutationsnummer.nbident,\n" + 
//        		"    vollzugsgegenstand.astatus AS status,\n" + 
//        		"    vollzugsgegenstand.bemerkungen,\n" + 
//        		"    TO_DATE(vollzugsgegenstand.grundbucheintrag, 'YYYY-MM-DD') AS grundbucheintrag,\n" + 
//        		"    TO_DATE(vollzugsgegenstand.tagebucheintrag, 'YYYY-MM-DD') AS tagebucheintrag, \n" + 
//        		"    vollzugsgegenstand.tagebuchbeleg,\n" + 
//        		"    messageprocessed.createdat AS importdatum\n" + 
//        		"FROM \n" + 
//        		"    agi_gb2av.vollzugsgegnstnde_vollzugsgegenstand AS vollzugsgegenstand\n" + 
//        		"    LEFT JOIN agi_gb2av.mutationsnummer AS mutationsnummer\n" + 
//        		"    ON mutationsnummer.vollzgsggnszgsggnstand_mutationsnummer = vollzugsgegenstand.t_id \n" + 
//        		"    LEFT JOIN agi_gb2av.camel_messageprocessed AS messageprocessed\n" + 
//        		"    ON messageprocessed.messageid = 'ili2pg-' || vollzugsgegenstand.t_datasetname || '.xml'\n" + 
//        		"ORDER BY \n" + 
//        		"    importdatum DESC\n" +
//        		"LIMIT 100"
//        		;
//        
//        RowMapper<Gb2avMessage> rowMapper = new BeanPropertyRowMapper<Gb2avMessage>(Gb2avMessage.class);
//        List<Gb2avMessage> messages = jdbcTemplate.query(sql, rowMapper);
//
//		Date lastDate = messages.get(0).getImportdatum();
//		channel.setPubDate(lastDate);
//
//        List<Item> items = new ArrayList<Item>();
//        for (Gb2avMessage message: messages) {
//            Item item = new Item();
//
//            item.setLink(awsBaseUrl + awsBucketName + "/" + message.getT_datasetname() + ".xml");
//            item.setTitle(message.getNummer() + " / " + message.getNbident() + ": " + message.getStatus());
//            item.setUri(awsBaseUrl + awsBucketName + "/" + message.getT_datasetname() + ".xml");
//            
//            Description descr = new Description();
//            descr.setValue("<table>"
//                    + "<tr><td>Mutationsnummer:</td><td>"+message.getNummer()+" / "+message.getNbident()+"</td></tr>"
//                    + "<tr><td>Status:</td><td>"+message.getStatus()+"</td></tr>"                    
//                    + "<tr><td>Grundbucheintrag:</td><td>"+message.getGrundbucheintrag()+"</td></tr>"                    
//                    + "<tr><td>Tagebucheintrag:</td><td>"+message.getTagebucheintrag()+"</td></tr>"                    
//                    + "<tr><td>Vollzugsmeldung:</td><td><a href='"+awsBaseUrl + awsBucketName + "/" + message.getT_datasetname() + ".xml"+"'>XML</a></td></tr>"
//                    + "</table>");
//            item.setDescription(descr);
//            item.setPubDate(message.getImportdatum());
//            
//            items.add(item);
//        }
//        
//        channel.setItems(items);
//        return channel;
//    }
}
