package com.arkady;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@SpringBootApplication
public class DiplomaApplication {
	public static void main(String[] args) {
		SpringApplication.run(DiplomaApplication.class, args);
	}
}
