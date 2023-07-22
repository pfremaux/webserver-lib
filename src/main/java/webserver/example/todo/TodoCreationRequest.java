package webserver.example.todo;

import webserver.annotations.Form;

@Form(jsMethodName = "todoCreation")
public class TodoCreationRequest {
    private final String text;
    private final boolean done;

    public TodoCreationRequest(String text, boolean done) {
        this.text = text;
        this.done = done;
    }

    public String getText() {
        return text;
    }

    public boolean isDone() {
        return done;
    }
}
