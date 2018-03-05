package com.github.pmoerenhout.rssmixer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

@RestController
public class RssController {

  final private static Logger LOG = LoggerFactory.getLogger(RssController.class);

  final private static String ROME_FEED_TYPE_RSS_2_0 = "rss_2.0";
  final private static String ROME_FEED_TYPE_ATOM_1_0 = "atom_1.0";

  @Autowired
  private RssService rssService;

  @RequestMapping(value = "/rss", method = RequestMethod.GET, produces = "application/rss+xml")
  public void getRss(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException, FeedException {
    final Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      final String headerName = headerNames.nextElement();
      LOG.debug("Header {}: {}", headerName, request.getHeader(headerName));
    }
    LOG.info("Handle request URL {} from {}", request.getRequestURL(), request.getHeader("X-Real-IP"));
    final List<SyndFeed> feeds = rssService.getAllFeeds();
    final SyndFeed mergedFeed = rssService.merge(ROME_FEED_TYPE_RSS_2_0, feeds);

    for (final SyndEntry entry : mergedFeed.getEntries()) {
      LOG.info("title:'{}' link:'{}'", entry.getTitle(), entry.getLink());
    }

    response.setContentType("application/rss+xml");

    final SyndFeedOutput output = new SyndFeedOutput();
    output.output(mergedFeed, response.getWriter());
  }

  @RequestMapping(value = "/atom", method = RequestMethod.GET, produces = "application/atom+xml")
  public void getAtom(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException, FeedException {
    LOG.info("Handle request URL {} from {}", request.getRequestURL(), request.getHeader("X-Real-IP"));
    final List<SyndFeed> feeds = rssService.getAllFeeds();
    final SyndFeed mergedFeed = rssService.merge(ROME_FEED_TYPE_ATOM_1_0, feeds);

    for (final SyndEntry entry : mergedFeed.getEntries()) {
      LOG.info("title:'{}' link:'{}'", entry.getTitle(), entry.getLink());
    }
    response.setContentType("application/atom+xml");

    final SyndFeedOutput output = new SyndFeedOutput();
    output.output(mergedFeed, response.getWriter());
  }


  @RequestMapping(value = "/text", method = RequestMethod.GET, produces = "text/html")
  @ResponseBody
  public void getText(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException, FeedException {
//    final Enumeration<String> headerNames = request.getHeaderNames();
//    while (headerNames.hasMoreElements()) {
//      final String headerName = headerNames.nextElement();
//      LOG.debug("Header {}: {}", headerName, request.getHeader(headerName));
//    }
    LOG.info("Handle request URL {} from {}", request.getRequestURL(), request.getHeader("X-Real-IP"));
    final List<SyndFeed> feeds = rssService.getAllFeeds();
    final SyndFeed mergedFeed = rssService.merge(ROME_FEED_TYPE_RSS_2_0, feeds);

    response.setContentType("text/html");

    final PrintWriter printWriter = response.getWriter();
    printWriter.println("<html>");
    printWriter.println("<head>");
    printWriter.println("<title>ZOOOM RSS als text</title>");
    printWriter.println("</head>");
    printWriter.println("<body>");
    printWriter.println("<table>");
    printWriter.println("<tr><th>Title</th><th>Link</th><th>Author</th><th>Published</th><th>Updated</th></tr>");
    for (final SyndEntry entry : mergedFeed.getEntries()) {
      LOG.info("title:'{}' link:'{}' author:'{}' published:{} updated:{}", entry.getTitle(), entry.getLink(), entry.getAuthor(), entry.getPublishedDate(),
          entry.getUpdatedDate());
      printWriter.println("<tr>");
      printWriter.println(htmlTableData(entry.getTitle()));
      printWriter.println(htmlTableDataLink(entry.getLink()));
      printWriter.println(htmlTableData(entry.getAuthor()));
      printWriter.println(htmlTableData(entry.getPublishedDate()));
      printWriter.println(htmlTableData(entry.getUpdatedDate()));
      printWriter.println("</tr>");
    }
    printWriter.println("</table>");
    printWriter.println("</body>");
    printWriter.println("</html>");
  }

  private String htmlTableData(final String text) {
    if (text == null) {
      return "<td>&nbsp;</td>";
    }
    return "<td>" + StringEscapeUtils.escapeHtml4(text) + "</td>";
  }

  private String htmlTableDataLink(final String link) {
    if (link == null) {
      return "<td></td>";
    }
    return "<td><a href=\"" + link + "\" target=\"_blank\">" + link + "</a></td>";
  }

  private String htmlTableData(final Date date) {
    if (date == null) {
      return "<td>&nbsp;</td>";
    }
    return "<td>" + StringEscapeUtils.escapeHtml4(date.toString()) + "</td>";
  }
}