package ch.so.agi.gb2av;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.ForwardedHeaderFilter;

@SpringBootApplication
public class Gb2avApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gb2avApplication.class, args);
	}

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    } 
}
