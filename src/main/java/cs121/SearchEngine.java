package cs121;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

//@ComponentScan
@SpringBootApplication(exclude = org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class)
public class SearchEngine {

    public static void main(String[] args) {
    	Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    	root.setLevel(Level.WARN);
    	
        SpringApplication.run(SearchEngine.class, args);
    }
}
