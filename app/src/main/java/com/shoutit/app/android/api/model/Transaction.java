package com.shoutit.app.android.api.model;

import java.util.List;

public class Transaction {

    public static String TYPE_IN = "in";

    private final String id;
    private final long createdAt;
    private final Display display;
    private final String type;

    public Transaction(String id, long createdAt, Display display, String type) {
        this.id = id;
        this.createdAt = createdAt;
        this.display = display;
        this.type = type;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Display getDisplay() {
        return display;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public boolean isOut() {
        return !type.equals(TYPE_IN);
    }

    public class Display {

        private final String text;
        private final List<Ranges> ranges;

        public Display(String text, List<Ranges> ranges) {
            this.text = text;
            this.ranges = ranges;
        }

        public class Ranges {
            private final int length;
            private final int offset;

            public Ranges(int length, int offset) {
                this.length = length;
                this.offset = offset;
            }

            public int getLength() {
                return length;
            }

            public int getOffset() {
                return offset;
            }
        }

        public String getText() {
            return text;
        }

        public List<Ranges> getRanges() {
            return ranges;
        }
    }
}