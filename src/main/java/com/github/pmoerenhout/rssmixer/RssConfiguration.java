package com.github.pmoerenhout.rssmixer;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rss")
public class RssConfiguration {

  private List<String> urls;

  public List<String> getUrls() {
    return urls;
  }

  public void setUrls(final List<String> urls) {
    this.urls = urls;
  }
}
