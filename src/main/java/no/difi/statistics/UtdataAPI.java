package no.difi.statistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class UtdataAPI {
    public static void main(String[] args) {
        SpringApplication.run(UtdataAPI.class, args);
    }
}
