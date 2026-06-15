package com.lalitha.sweets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;

@SpringBootApplication
@Lazy
public class LalithaSweetsApplication {

	public static void main(String[] args) {
		SpringApplication.run(LalithaSweetsApplication.class, args);
	}

}
