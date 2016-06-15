package com.shoutit.app.android.api.model;

import java.util.List;

public class TransactionRsponse {

    private final List<Transaction> results;
    private final String previous;
    private final String next;

    public TransactionRsponse(List<Transaction> results, String previous, String next) {
        this.results = results;
        this.previous = previous;
        this.next = next;
    }

    public List<Transaction> getResults() {
        return results;
    }

    public String getPrevious() {
        return previous;
    }

    public String getNext() {
        return next;
    }
}
