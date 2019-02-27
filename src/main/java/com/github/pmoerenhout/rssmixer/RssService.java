package com.github.pmoerenhout.rssmixer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

@Service
public class RssService {

  final private static Logger LOG = LoggerFactory.getLogger(RssService.class);

  @Autowired
  private RssConfiguration rssConfiguration;

  public List<SyndFeed> getAllFeeds() throws IOException, FeedException {

    final List<SyndFeed> feeds = new ArrayList<>();
    for (final String url : rssConfiguration.getUrls()) {
      LOG.info("Get feed for URL '{}'", url);
      feeds.add(getSortedSyndFeed(url));
    }
    return feeds;
  }

  public SyndFeed getSortedSyndFeed(final String url) throws IOException, FeedException {
    try (final CloseableHttpClient client = HttpClients.createMinimal()) {
      final HttpUriRequest request = new HttpGet(url);
      try (final CloseableHttpResponse response = client.execute(request);
           final InputStream stream = response.getEntity().getContent()) {
        final SyndFeedInput input = new SyndFeedInput();
        final SyndFeed feed = input.build(new XmlReader(stream));
        Collections.sort(feed.getEntries(), new Comparator<SyndEntry>() {
          @Override
          public int compare(SyndEntry o1, SyndEntry o2) {
            return o1.getPublishedDate().compareTo(o2.getPublishedDate());
          }
        });
        feed.setEntries(removeFutureEntries(feed.getEntries(), DateUtils.addDays(new Date(), 182)));
        return feed;
      }
    }
  }

  public SyndFeed merge(final String feedType, final List<SyndFeed> feeds) {

    final SyndFeed mergedFeed = new SyndFeedImpl();
    mergedFeed.setFeedType(feedType);

    mergedFeed.setTitle("ZZP Woerden");
    mergedFeed.setDescription("Aggregated Feed");
    mergedFeed.setAuthor("Pim Moerenhout");
    mergedFeed.setLink("http://www.zzpwoerden.nl");
    mergedFeed.setGenerator("http://www.wyless.ns1.name/rss-mixer");

    final List entries = new ArrayList();
    mergedFeed.setEntries(entries);
    for (SyndFeed inFeed : feeds) {
      entries.addAll(inFeed.getEntries());
    }
    return mergedFeed;
  }

  public List<SyndEntry> removeFutureEntries(final List<SyndEntry> entries, final Date maxDate){
    return entries.stream().filter(new Predicate<SyndEntry>() {
      @Override
      public boolean test(final SyndEntry syndEntry) {
        if (StringUtils.contains(syndEntry.getTitle(), "12 april")) {
          return false;
        }
        if (StringUtils.contains(syndEntry.getTitle(), "Laat je stem horen")) {
          return false;
        }
        return syndEntry.getPublishedDate().compareTo(maxDate) < 0;
      }
    }).collect(Collectors.toList());
  }
}
