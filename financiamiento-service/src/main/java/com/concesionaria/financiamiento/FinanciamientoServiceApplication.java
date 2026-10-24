package com.concesionaria.financiamiento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FinanciamientoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanciamientoServiceApplication.class, args);
    }
}
