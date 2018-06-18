package com.github.pmoerenhout.rssmixer;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

  final static Logger LOG = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public CommandLineRunner commandLineRunner(final ApplicationContext ctx) {
    return args -> {
      LOG.debug("Let's inspect the beans provided by Spring Boot:");
      Arrays.asList(ctx.getBeanDefinitionNames()).stream().sorted().forEach(b -> LOG.debug("Bean name: {}", b));
    };
  }

  @Bean
  public CharacterEncodingFilter characterEncodingFilter() {
    final CharacterEncodingFilter filter = new CharacterEncodingFilter();
    filter.setEncoding("UTF-8");
    filter.setForceEncoding(true);
    return filter;
  }

}