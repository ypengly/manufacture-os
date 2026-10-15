package com.manufactureos;

import com.manufactureos.config.CorsProperties;
import com.manufactureos.config.JwtProperties;
import com.manufactureos.config.ScrapProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class, ScrapProperties.class})
@EnableScheduling
public class ManufactureOsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManufactureOsApplication.class, args);
    }
}
