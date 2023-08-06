package webserver.example.todo;

import webserver.annotations.Form;
import webserver.annotations.JsonField;

@Form(jsMethodName = "todoCreation")
public class TodoCreationRequest {
    @JsonField
    private final String text;
    @JsonField
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
