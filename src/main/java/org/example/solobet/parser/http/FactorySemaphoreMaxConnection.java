package org.example.solobet.parser.http;

import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.netty.channel.ConnectionSemaphore;
import org.asynchttpclient.netty.channel.ConnectionSemaphoreFactory;
import org.asynchttpclient.netty.channel.InfiniteSemaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

public class FactorySemaphoreMaxConnection implements ConnectionSemaphoreFactory {
    private static final Logger logger = LoggerFactory.getLogger(FactorySemaphoreMaxConnection.class);

    private int maxConnection;

    public FactorySemaphoreMaxConnection(int maxConnection) {
        this.maxConnection = maxConnection;
    }

    @Override
    public ConnectionSemaphore newConnectionSemaphore(AsyncHttpClientConfig asyncHttpClientConfig) {

        return new ConnectionSemaphore() {
            protected final Semaphore freeChannels = (maxConnection > 0 ? new Semaphore(maxConnection) : InfiniteSemaphore.INSTANCE);

            public void acquireChannelLock(Object partitionKey){
                try {
                    this.freeChannels.acquire();
                } catch (InterruptedException e) {
                    logger.warn("Error during blocking",e);
                }
            }

            public void releaseChannelLock(Object partitionKey) {
                this.freeChannels.release();
            }
        };
    }
}
