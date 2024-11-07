package com.aischool.goodswap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class GoodswapApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoodswapApplication.class, args);
	}

}
