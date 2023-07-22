package webserver.example;

import tools.MdDoc;
import webserver.EmptyBody;
import webserver.annotations.Endpoint;
import webserver.example.todo.Todo;
import webserver.example.todo.TodoCreationRequest;
import webserver.example.todo.TodoListResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyEndpoints {

    @Endpoint(path = "/toto", method = "GET")
    @MdDoc(description = "My description!!!")
    // TODO PFR remettre @Role(value = "coucou")
    public Body go(Map<String, List<String>> headers, Body body) {
        return new Body("reponse");
    }

    @Endpoint(path = "/totoPost", method = "POST")
    public Body post(Map<String, List<String>> headers, Body body) {
        return new Body("reponse");
    }

    @Endpoint(path = "/sum", method = "POST")
    public SumResult sum(Map<String, List<String>> headers, Pair body) {
        return new SumResult(body.getA() + body.getB());
    }

    @Endpoint(path = "/testArray", method = "POST")
    public TestArray array(Map<String, List<String>> headers, TestArray2 body) {
        return new TestArray(List.of("aaa", "bbb"), List.of(3.1, 2.2), List.of(new Body("toto1"), new Body("toto2")));
    }


    // DO NOT DO THAT IN PRODUCTION. IT'S JUST tp ease my life with databases for this example.
    private static List<Todo> todos = new ArrayList<>();

    static {
        todos.add(new Todo(System.currentTimeMillis() - 10000L, "Already finished", true));
        todos.add(new Todo(System.currentTimeMillis(), "Finish this", false));
    }

    @Endpoint(path = "/todos", method = "GET")
    public TodoListResponse loadTodos(Map<String, List<String>> headers) {
        return new TodoListResponse(todos);
    }



	@Endpoint(path = "/todos/create", method = "POST")
	public EmptyBody createTodo(Map<String, List<String>> headers, TodoCreationRequest todo) {
		todos.add(new Todo(System.currentTimeMillis(), todo.getText(), todo.isDone()));
		return new EmptyBody();
	}

}

