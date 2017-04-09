package com.verint.itunes;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by home on 4/7/2017.
 */
public class AppInfoRestRepository implements AppInfoRepository {
    private Logger logger = LogManager.getLogger(this.getClass());

    private CloseableHttpClient httpClient = null;
    private String appInfoBaseUrl = "http://itunes.apple.com/lookup";

    public AppInfoRestRepository (){

        // Create message constraints
        MessageConstraints messageConstraints = MessageConstraints.custom()
                .setMaxHeaderCount(200)
                .setMaxLineLength(2000)
                .build();

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8)
                .setMessageConstraints(messageConstraints)
                .build();

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultConnectionConfig(connectionConfig);

        // Create global request configuration
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .setExpectContinueEnabled(true)
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();

        // Create an HttpClient with the given custom dependencies and configuration.
        httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();
    }

    public List<AppInfo> readAppInfo(List<String> appIds) {
        List<AppInfo> appsInfo = new ArrayList<>();

        if (appIds != null) {
            appsInfo =  appIds.stream()
                              .map(appId -> loadAppInfo(appId))
                              .collect(toList());
        }

        return appsInfo;
    }

    private AppInfo loadAppInfo(String appId) {
        AppInfo appInfo = null;

        String url = this.appInfoBaseUrl + String.format("?id=%s", appId);

        logger.debug("Going to retrieve app info from {}", url);

        HttpGet httpget = new HttpGet(url);

        // Execution context can be customized locally.
        HttpClientContext context = HttpClientContext.create();

        CloseableHttpResponse response = null;
        try {
            // invoke http request
            httpget.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            response = httpClient.execute(httpget, context);

            StatusLine statusLine = response.getStatusLine();
            logger.debug("Response status line {}", statusLine);

            if (statusLine.getStatusCode() == 200) {
                String appInfoJson = EntityUtils.toString(response.getEntity());

                if (!StringUtils.isEmpty(appInfoJson)) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(appInfoJson);

                    JsonNode results = rootNode.path("results");
                    if (!results.isMissingNode() && results.isArray()) {

                        JsonNode appNode = results.get(0);
                        appInfo = new AppInfo(appId, appNode.path("description").asText(""));
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Error retrieving app info ", ex);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception ex) {
                logger.error("Exception closing http response", ex);
            }
        }

        return appInfo;
    }
}
