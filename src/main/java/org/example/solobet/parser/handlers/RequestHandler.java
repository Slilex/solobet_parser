package org.example.solobet.parser.handlers;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.Response;
import org.example.solobet.parser.http.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class RequestHandler extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);


    private BlockingQueue<String> urls = new LinkedBlockingQueue<>();
    private Map<String, Integer> urlIdMap = new ConcurrentHashMap<>();
    private AtomicBoolean run = new AtomicBoolean(true);
    private WebClient webClient;
    private ResponseHandler responseHandler;
    private AtomicBoolean wasLastMsg = new AtomicBoolean(true);

    public RequestHandler(WebClient webClient, ResponseHandler responseHandler){
        this.webClient = webClient;
        this.responseHandler = responseHandler;
    }


    @Override
    public void run() {
        try {
            while (run.get()) {
                    String url = urls.take();

                    if (url.equals("STOP")) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("handler received a shutdown command. queue size : '{}'", urls.size());
                        }
                        return;
                    }
                    final Integer id = urlIdMap.get(url);
                webClient.execute(url, new AsyncCompletionHandler<AsyncHandler.State>() {
                    @Override
                    public State onCompleted(Response response) throws Exception {
                        if (response.getStatusCode() == 200) {
                            responseHandler.add(response, id);
                        }
                        return State.ABORT;
                    }
                });
                urlIdMap.remove(url);

            }
        } catch (Exception e) {
           logger.error("an error occurred during execution {}", e.getMessage(), e);
        } finally {
            logger.info("handler finished work");
        }
    }

    public void add(String url, Integer id) {
        wasLastMsg.set(true);
        urlIdMap.put(url, id);
        urls.add(url);
    }

    public boolean isEmpty(){
        return !wasLastMsg.getAndSet(false) && urls.isEmpty();
    }

    public void close() {
        run.set(false);
        urls.add("STOP");
    }
}
