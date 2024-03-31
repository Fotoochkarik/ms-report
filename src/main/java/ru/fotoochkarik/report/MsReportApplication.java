package ru.fotoochkarik.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class MsReportApplication {

  public static void main(String[] args) {
    SpringApplication.run(MsReportApplication.class, args);
  }

}
