package br.com.condo.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAutoConfiguration
public class CondoManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CondoManagerApplication.class, args);
	}
}
