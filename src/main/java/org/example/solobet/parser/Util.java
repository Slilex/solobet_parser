package org.example.solobet.parser;


import org.asynchttpclient.Response;
import org.example.solobet.parser.entitys.ResponseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.concurrent.atomic.AtomicInteger;

public class Util {

    private static AtomicInteger id = new AtomicInteger(0);

    public static ResponseEntity convert(Response take, Integer integer) {
        String responseBody = take.getResponseBody();
        if(!responseBody.isEmpty() && integer != null) {
            Document doc = Jsoup.parse(responseBody);

            ResponseEntity responseEntity = new ResponseEntity();
            responseEntity.setDocument(doc);
            responseEntity.setRequestId(integer);
            return responseEntity;

        }
        return null;
    }

    public static Integer getId(){
        return id.incrementAndGet();
    }
}
