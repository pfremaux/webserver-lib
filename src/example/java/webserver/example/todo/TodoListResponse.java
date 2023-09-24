package webserver.example.todo;

import webserver.annotations.JsonField;

import java.util.List;

public class TodoListResponse {
    @JsonField
    private final List<Todo> todoList;

    public TodoListResponse(List<Todo> todoList) {
        this.todoList = todoList;
    }

    public List<Todo> getTodoList() {
        return todoList;
    }
}
