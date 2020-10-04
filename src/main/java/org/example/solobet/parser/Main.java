package org.example.solobet.parser;

import org.example.solobet.parser.entitys.Event;
import org.example.solobet.parser.entitys.Odd;
import org.example.solobet.parser.http.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        if(logger.isInfoEnabled()) {
            logger.info("App starting");
        }

        WebClient webClient = new WebClient();
        SolobetParsService solobetParsService = new SolobetParsService(webClient);
        solobetParsService.start();

        if(logger.isInfoEnabled()) {
            logger.info("App started");
        }

        waitResult(solobetParsService);

        Set<Event> result = solobetParsService.getResult();
        solobetParsService.stop();
        webClient.close();
        String stringResult = getStringResult(result);

        System.out.println(stringResult);

        if(logger.isInfoEnabled()) {
            logger.info("App stopped");
        }

    }

    private static void waitResult(SolobetParsService solobetParsService) throws InterruptedException {
        while (solobetParsService.stillWorks()) {
            Thread.sleep(600);
            System.out.print(".");;
        }
        System.out.println();
    }


    public static String getStringResult(Set<Event> result) {
        StringBuilder stringBuilder = new StringBuilder();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm");

        Map<String, List<Event>> eventCollect = result.stream().collect(Collectors.groupingBy(Event::getSportType));
        for (Map.Entry<String, List<Event>> eventSportTypeMap : eventCollect.entrySet()) {

            List<Event> collect = eventSportTypeMap.getValue().stream().sorted(Comparator.comparing(Event::getLigaName)).collect(Collectors.toList());
            for (Event event : collect) {
                stringBuilder.append(event.getSportType()).append(", ")
                        .append(event.getLigaName()).append(", ")
                        .append(event.getMatchName()).append(", ")
                        .append(event.getDate().format(dateTimeFormatter)).append(", ")
                        .append(event.getMatchId());

                Map<String, List<Odd>> oddCollect = event.getOds().stream().collect(Collectors.groupingBy(Odd::getType));
                oddCollect.forEach((k, v) -> {
                    stringBuilder.append("\n").append("     ").append(k);
                    for (Odd odd : v) {
                        stringBuilder.append("\n").append("          ").append(odd.getName()).append(odd.getValue());
                    }
                });

                stringBuilder.append("\n");
            }
        }

        return stringBuilder.toString();

    }

}
