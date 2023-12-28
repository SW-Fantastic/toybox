package org.swdc.toybox.core;

import org.apache.lucene.document.Document;

public class IndexResult {

    public enum State {

        REMOVED,
        ADDED,
        UPDATED,
        NOT_CHANGED
    }

    private Document document;

    private State state;

    public IndexResult(State state, Document document) {
        this.state = state;
        this.document = document;
    }

    public State getState() {
        return state;
    }

    public Document getDocument() {
        return document;
    }
}
