package com.parkez;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
public class ParkEzApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkEzApplication.class, args);
    }

}
