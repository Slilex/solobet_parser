package org.example.solobet.parser.http;

import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.exception.TooManyConnectionsException;
import org.asynchttpclient.netty.channel.ConnectionSemaphore;
import org.asynchttpclient.netty.channel.ConnectionSemaphoreFactory;
import org.asynchttpclient.netty.channel.InfiniteSemaphore;
import org.asynchttpclient.netty.channel.MaxConnectionSemaphore;
import org.asynchttpclient.util.ThrowableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class FactorySemaphoreMaxConnection implements ConnectionSemaphoreFactory {
    private static final Logger logger = LoggerFactory.getLogger(FactorySemaphoreMaxConnection.class);

    private int timeout;
    private int maxConnection;

    public FactorySemaphoreMaxConnection(int maxConnection, int acquireTimeout) {
    }

    @Override
    public ConnectionSemaphore newConnectionSemaphore(AsyncHttpClientConfig asyncHttpClientConfig) {

        return new ConnectionSemaphore() {
            protected final Semaphore freeChannels = (Semaphore) (maxConnection > 0 ? new Semaphore(maxConnection) : InfiniteSemaphore.INSTANCE);
            protected final IOException tooManyConnections = (IOException) ThrowableUtil.unknownStackTrace(new TooManyConnectionsException(maxConnection), MaxConnectionSemaphore.class, "acquireChannelLock");
            protected final int acquireTimeout = Math.max(0, timeout);

            public void acquireChannelLock(Object partitionKey) throws IOException {
                try {
                    if (!this.freeChannels.tryAcquire((long) this.acquireTimeout, TimeUnit.MILLISECONDS)) {
                        throw this.tooManyConnections;
                    }
                } catch (InterruptedException var3) {
                    //todo log
                }
            }

            public void releaseChannelLock(Object partitionKey) {
                this.freeChannels.release();
            }
        };
    }
}
