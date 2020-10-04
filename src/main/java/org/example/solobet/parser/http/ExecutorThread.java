package org.example.solobet.parser.http;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.BoundRequestBuilder;

public class ExecutorThread implements Runnable{
    private final AsyncCompletionHandler asyncCompletionHandler;
    private final BoundRequestBuilder requestBuilder;

    public ExecutorThread(BoundRequestBuilder requestBuilder, AsyncCompletionHandler asyncCompletionHandler) {
        this.asyncCompletionHandler = asyncCompletionHandler;
        this.requestBuilder = requestBuilder;
    }

    @Override
    public void run() {
        requestBuilder.execute(asyncCompletionHandler);
    }
}
