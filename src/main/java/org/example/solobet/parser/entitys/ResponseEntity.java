package org.example.solobet.parser.entitys;

import org.jsoup.nodes.Document;

import java.util.Objects;

public class ResponseEntity {
    private Integer requestId;
    private Document document;

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseEntity that = (ResponseEntity) o;
        return Objects.equals(requestId, that.requestId) &&
                Objects.equals(document, that.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, document);
    }
}
