package io.collective.articles;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.collective.restsupport.BasicHandler;
import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ArticlesController extends BasicHandler {
    private final ArticleDataGateway gateway;

    public ArticlesController(ObjectMapper mapper, ArticleDataGateway gateway) {
        super(mapper);
        this.gateway = gateway;
    }

    private String generateHtml(List<ArticleInfo> articleInfos) {
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<html><head><title>Articles</title></head><body>");
        htmlBuilder.append("<h1>Articles</h1>");

        htmlBuilder.append("<ul>");
        for (ArticleInfo article : articleInfos) {
            htmlBuilder.append("<li>");
            htmlBuilder.append("<h2>").append(article.getTitle()).append("</h2>");
            htmlBuilder.append("</li>");
        }
        htmlBuilder.append("</ul>");

        htmlBuilder.append("</body></html>");

        return htmlBuilder.toString();
    }


    @Override
    public void handle(String target, Request request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        get("/articles", List.of("application/json", "text/html"), request, servletResponse, () -> {

            List<ArticleRecord> articles = gateway.findAll();

            List<ArticleInfo> articleInfos = articles.stream()
                    .map(article -> new ArticleInfo(article.getId(), article.getTitle()))
                    .collect(Collectors.toList());

            String acceptHeader = request.getHeader("Accept");

            if (acceptHeader.contains("application/json")) {
                writeJsonBody(servletResponse, articleInfos);
                servletResponse.setStatus(HttpServletResponse.SC_OK);
            }

            else if (acceptHeader.contains("text/html")) {
                String html = generateHtml(articleInfos);
                servletResponse.setContentType("text/html");
                servletResponse.setCharacterEncoding("UTF-8");
                try {
                    servletResponse.getWriter().write(html);
                }
                catch (IOException e) {
                    e.printStackTrace(); // Log the exception
                    servletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                servletResponse.setStatus(HttpServletResponse.SC_OK);
            }
            else {
                servletResponse.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            }

            request.setHandled(true);
        });

        get("/available", List.of("application/json"), request, servletResponse, () -> {

            List<ArticleRecord> availableArticles = gateway.findAvailable();

            List<ArticleInfo> articleInfos = availableArticles.stream()
                    .map(article -> new ArticleInfo(article.getId(), article.getTitle())) // Assuming ArticleInfo constructor or a static method can map Article to ArticleInfo
                    .collect(Collectors.toList());

            writeJsonBody(servletResponse, articleInfos);

            servletResponse.setStatus(HttpServletResponse.SC_OK);

            request.setHandled(true);
        });
    }
}
