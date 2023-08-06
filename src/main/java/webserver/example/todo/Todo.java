package webserver.example.todo;

import webserver.annotations.JsonField;

public class Todo {
    @JsonField
    private final long creationTimestamp;
    @JsonField
    private final String text;
    @JsonField
    private final boolean done;

    public Todo(long creationTimestamp, String text, boolean done) {
        this.creationTimestamp = creationTimestamp;
        this.text = text;
        this.done = done;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public String getText() {
        return text;
    }

    public boolean isDone() {
        return done;
    }
}
