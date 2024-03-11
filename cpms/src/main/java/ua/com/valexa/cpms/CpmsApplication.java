package ua.com.valexa.cpms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"ua.com.valexa.db.repository"})
@EntityScan(basePackages = {"ua.com.valexa.db.model"})
@ComponentScan(basePackages = {"ua.com.valexa.db", "ua.com.valexa.cpms"})
public class CpmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CpmsApplication.class, args);
    }

}
