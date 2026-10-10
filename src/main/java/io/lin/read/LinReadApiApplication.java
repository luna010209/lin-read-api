package io.lin.read;

import io.lin.read.config.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class LinReadApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LinReadApiApplication.class, args);
	}

}
