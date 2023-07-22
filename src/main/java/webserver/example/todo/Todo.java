package webserver.example.todo;

public class Todo {
    private final long creationTimestamp;
    private final String text;
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
