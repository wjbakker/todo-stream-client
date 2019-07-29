package com.github.wjbakker.todostreamclient;

import com.github.wjbakker.todostreamclient.todo.Todo;
import com.github.wjbakker.todostreamclient.todo.TodoResource;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class Main {
    private final TodoResource todoResource;

    Main(TodoResource todoResource) {
        this.todoResource = todoResource;
    }

    void run() {
        Stream<Todo> stream = todoResource.stream();

        try(stream) {
            stream.filter(Predicate.not(Todo::isCompleted)).forEach(System.out::println);
        }
    }

    public static void main(String[] args) {
        new Main(new TodoResource("http://localhost:8080/todos")).run();
    }
}
