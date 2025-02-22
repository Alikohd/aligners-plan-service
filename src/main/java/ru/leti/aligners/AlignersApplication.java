package ru.leti.aligners;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class AlignersApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlignersApplication.class, args);
	}

}
