package org.example.solobet.parser.http;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

public class WebClient  {
    private static final Logger logger = LoggerFactory.getLogger(WebClient.class);
    private final AsyncHttpClient asyncHttpClient;
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final int maxConnection = 5;
    private final int acquireTimeout = 2000;

    public WebClient() {
        asyncHttpClient = asyncHttpClient(config()
                .setKeepAlive(true)
                .setConnectionSemaphoreFactory(new FactorySemaphoreMaxConnection(maxConnection, acquireTimeout))
                .setMaxConnections(maxConnection)
                .build());
        logger.info("web client initialized");
    }

    public void close() throws IOException {
        executorService.shutdown();
        asyncHttpClient.close();
    }

    public void execute(String url, AsyncCompletionHandler asyncCompletionHandler) {
        BoundRequestBuilder boundRequestBuilder = asyncHttpClient.prepareGet(url);
        executorService.submit(new ExecutorThread(boundRequestBuilder, asyncCompletionHandler));
    }
}
