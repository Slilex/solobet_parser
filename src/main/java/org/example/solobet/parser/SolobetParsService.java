package org.example.solobet.parser;

import org.example.solobet.parser.entitys.DocumentType;
import org.example.solobet.parser.entitys.Event;
import org.example.solobet.parser.entitys.Odd;
import org.example.solobet.parser.entitys.ResponseEntity;
import org.example.solobet.parser.handlers.RequestHandler;
import org.example.solobet.parser.handlers.ResponseHandler;
import org.example.solobet.parser.http.WebClient;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

public class SolobetParsService {
    private static final Logger logger = LoggerFactory.getLogger(SolobetParsService.class);

    private static final String REPLACE_GAME_ID = "replaceGameId";
    private static final String MATCH_ID_URL = "https://solobet15.com/Sports/specialbets?MatchId=" + REPLACE_GAME_ID + "&quicktip=false&popup=false";
    private static final String HOST = "https://solobet15.com/";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy.dd.MM-hh:mm");
    private WebClient webClient;
    private Map<Integer, DocumentType> idDocumentTypeMap = new ConcurrentHashMap<>();
    private Map<Integer, Event> idEvents = new ConcurrentHashMap<>();
    private RequestHandler requestHandler;
    private ResponseHandler responseHandler;
    private Set<Event> finalResult = new CopyOnWriteArraySet<>();
    private AtomicBoolean run = new AtomicBoolean(true);

    public SolobetParsService(WebClient webClient) {
        this.webClient = webClient;
        responseHandler = new ResponseHandler(this);
        requestHandler = new RequestHandler(webClient, responseHandler);
    }

    public void start(){
        responseHandler.start();
        requestHandler.start();
        ResponseEntity entity = new ResponseEntity();
        Integer id = Util.getId();
        entity.setRequestId(id);
        idDocumentTypeMap.put(id, DocumentType.HOME);
        process(entity);
    }

    public void process(ResponseEntity responseEntity) {
        run.set(true);
        DocumentType documentType = idDocumentTypeMap.get(responseEntity.getRequestId());
        switch (documentType) {
            case HOME:
                processHomeTypeResponse(responseEntity);
                break;
            case SPORT:
                processSportTypeResponse(responseEntity);
                break;
            case LIGA:
                processLeagaType(responseEntity);
                break;
            case MATCH:
                processMatchTypeResponse(responseEntity);
                break;
            case ODD:
                processODDTypeResponse(responseEntity);
                break;
            default:
                logger.warn("unknown document type '{}'", documentType);
                break;
        }
    }



    private void processHomeTypeResponse(ResponseEntity responseEntity) {
        final Integer id = Util.getId();
        requestHandler.add(HOST + "/Home", id);
        idDocumentTypeMap.put(id, DocumentType.SPORT);
    }

    private void processLeagaType(ResponseEntity responseEntity) {
        Document document = responseEntity.getDocument();
        Integer requestId = responseEntity.getRequestId();
        Event baseEvent = idEvents.getOrDefault(requestId, new Event());
        Element element = document.getElementsByClass("league-items").first();
        Elements elementsA = element.getElementsByTag("a");
        for (Element link : elementsA) {
            String url = link.attr("href");
            String text = link.text();
            final Integer id = Util.getId();
            Event event = new Event(baseEvent);
            event.setLigaName(text);
            idEvents.put(id, event);
            idDocumentTypeMap.put(id, DocumentType.MATCH);
            requestHandler.add(HOST + url, id);
        }
    }


    private void processSportTypeResponse(ResponseEntity responseEntity) {
        Document document = responseEntity.getDocument();
        Integer requestId = responseEntity.getRequestId();
        Event baseEvent = idEvents.getOrDefault(requestId, new Event());
        Element element = document.getElementsByClass("sports").first();
        Elements elementsA = element.getElementsByTag("a");
        for (Element link : elementsA) {
            String url = link.attr("href");
            String text = link.text();
            if (text.equalsIgnoreCase("Fußball")) {
                text = "Soccer";
            }
            final Integer id = Util.getId();
            Event event = new Event(baseEvent);
            event.setSportType(text);
            idEvents.put(id, event);
            idDocumentTypeMap.put(id, DocumentType.LIGA);
            requestHandler.add(HOST + url, id);
        }
    }


    private void processMatchTypeResponse(ResponseEntity responseEntity) {
        Document document = responseEntity.getDocument();
        Integer requestId = responseEntity.getRequestId();
        Elements games = document.getElementsByClass("teams").parents();
        Event baseEvent = idEvents.get(requestId);

        for (Element game : games) {

            String gameid = game.attr("gameid");
            if (gameid != null && !gameid.isEmpty()) {
                Element teamsName = game.getElementsByClass("teams").first();
                Elements team = teamsName.getElementsByClass("team");
                String dateString = game.getElementsByClass("date").first().text();
                String timeString = game.getElementsByClass("time").first().text();
                LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
                String dateTimeString = localDateTime.getYear() + "." + dateString + "-" + timeString;
                OffsetDateTime date;
                try {
                    date = SIMPLE_DATE_FORMAT.parse(dateTimeString).toInstant().atOffset(ZoneOffset.UTC);
                } catch (ParseException e) {
                    logger.warn("date parse error : {}", e.getErrorOffset(), e);
                    date = null;
                }

                String team1Name = team.get(0).text();
                String team2Name = team.get(1).text();

                Event event = new Event(baseEvent);
                event.setDate(date);
                event.setMatchName(team1Name + " - " + team2Name);
                event.setMatchId(gameid);
                final Integer id = Util.getId();

                String fullUrl = MATCH_ID_URL.replace(REPLACE_GAME_ID, gameid);

                idDocumentTypeMap.put(id, DocumentType.ODD);
                idEvents.put(id, event);
                requestHandler.add(fullUrl, id);
            }

        }

    }

    private void processODDTypeResponse(ResponseEntity responseEntity) {
        Document document = responseEntity.getDocument();
        Integer requestId = responseEntity.getRequestId();

        Event baseEvent = idEvents.get(requestId);

        Elements panels = document.getElementsByClass("betpanel");
        Set<Odd> oddsSet = new HashSet<>();

        for (Element panel : panels) {
            String betType = panel.getElementsByClass("betheader").first().text();
            Elements odds = panel.getElementsByClass("odds").first().children();
            for(Element odd : odds) {
                String tiptext = odd.getElementsByClass("tiptext").text();
                String oddStr = odd.getElementsByClass("odd").text();
                if (!(oddStr.isEmpty() || tiptext.isEmpty() || betType.isEmpty())) {
                    Odd oddClass = new Odd();
                    oddClass.setName(tiptext);
                    oddClass.setType(betType);
                    oddClass.setValue(oddStr);
                    oddsSet.add(oddClass);
                }

            }

        }
        Event event = new Event(baseEvent);
        event.setOds(oddsSet);
        finalResult.add(event);


    }

    public boolean stillWorks() {
        boolean b = !(requestHandler.isEmpty() && responseHandler.isEmpty()) || run.get();
        run.set(false);
        return b;
    }

    public Set<Event> getResult() {
        return finalResult;
    }

    public void stop() throws IOException {
        requestHandler.close();
        responseHandler.close();
    }
}
