package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableConfigServer
@SpringBootApplication
@RestController
public class ConfigServerApplication {
	
	  String name = "World";

	  @RequestMapping("/")
	  public String home() {
	    return "Hello " + name;
	  }

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}
}
