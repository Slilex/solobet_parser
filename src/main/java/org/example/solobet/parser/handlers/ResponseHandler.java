package org.example.solobet.parser.handlers;

import io.netty.handler.codec.http.CombinedHttpHeaders;
import org.asynchttpclient.Response;
import org.asynchttpclient.netty.NettyResponse;
import org.asynchttpclient.netty.NettyResponseStatus;
import org.asynchttpclient.uri.Uri;
import org.example.solobet.parser.SolobetParsService;
import org.example.solobet.parser.entitys.ResponseEntity;
import org.example.solobet.parser.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResponseHandler extends Thread{
    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    private BlockingQueue<Response> responses = new LinkedBlockingQueue<>();
    private Map<Response, Integer> responseIntegerMap = new ConcurrentHashMap<>();
    private AtomicBoolean run = new AtomicBoolean(true);
    private SolobetParsService solobetParsService;
    private Response stopCommandResponse;
    private AtomicBoolean wasLastMsg = new AtomicBoolean(true);

    public ResponseHandler(SolobetParsService solobetParsService) {
        this.solobetParsService = solobetParsService;
        stopCommandResponse = createStopResponse();
    }

    @Override
    public void run() {
        try {
            while (run.get()) {
                Response take = responses.take();
                if (stopCommandResponse.equals(take)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("handler received a shutdown command. queue size : '{}'", responses.size());
                    }
                    return;
                }
                ResponseEntity responseEntity = Util.convert(take, responseIntegerMap.get(take));
                responseIntegerMap.remove(take);
                if(responseEntity != null) {
                    solobetParsService.process(responseEntity);
                }
            }
        } catch (Exception e) {
            logger.error("an error occurred during execution {}", e.getMessage(), e);
        } finally {
            logger.info("handler finished work");
        }

    }

    public void add(org.asynchttpclient.Response response, int requestId) {
        wasLastMsg.set(true);
        responseIntegerMap.put(response, requestId);
        responses.add(response);

    }

    public boolean isEmpty(){
        return !wasLastMsg.getAndSet(false) && responses.isEmpty();
    }

    public void close() {
        run.set(false);
        responses.offer(stopCommandResponse);
    }

    private Response createStopResponse(){
        return new NettyResponse(new NettyResponseStatus(Uri.create("http://STOP"), null, null), new CombinedHttpHeaders(false), new ArrayList<>());
    }
}
