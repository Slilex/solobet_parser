package org.example.solobet.parser.entitys;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;

public class Event {
    private String sportType;
    private String ligaName;
    private String matchName;
    private OffsetDateTime date;
    private String matchId;
    private Set<Odd> ods;

    public Event() {
    }

    public Event(Event event) {
        this.sportType = event.getSportType();
        this.ligaName = event.getLigaName();
        this.matchName = event.getMatchName();
        this.date = event.getDate();
        this.matchId = event.getMatchId();
        this.ods = event.getOds();
    }


    public String getSportType() {
        return sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }

    public String getLigaName() {
        return ligaName;
    }

    public void setLigaName(String ligaName) {
        this.ligaName = ligaName;
    }

    public String getMatchName() {
        return matchName;
    }

    public void setMatchName(String matchName) {
        this.matchName = matchName;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public Set<Odd> getOds() {
        return ods;
    }

    public void setOds(Set<Odd> ods) {
        this.ods = ods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(sportType, event.sportType) &&
                Objects.equals(ligaName, event.ligaName) &&
                Objects.equals(matchName, event.matchName) &&
                Objects.equals(date, event.date) &&
                Objects.equals(matchId, event.matchId) &&
                Objects.equals(ods, event.ods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sportType, ligaName, matchName, date, matchId, ods);
    }

    @Override
    public String toString() {
        return "Event{" +
                "sportType='" + sportType + '\'' +
                ", ligaName='" + ligaName + '\'' +
                ", matchName='" + matchName + '\'' +
                ", date=" + date +
                ", matchId='" + matchId + '\'' +
                ", ods=" + ods +
                '}';
    }
}
