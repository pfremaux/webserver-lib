package webserver.example.todo;

import webserver.example.todo.Todo;

import java.util.List;

public class TodoListResponse {
    private final List<Todo> todoList;

    public TodoListResponse(List<Todo> todoList) {
        this.todoList = todoList;
    }

    public List<Todo> getTodoList() {
        return todoList;
    }
}
