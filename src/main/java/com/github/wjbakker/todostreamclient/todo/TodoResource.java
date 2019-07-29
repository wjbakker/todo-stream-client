package com.github.wjbakker.todostreamclient.todo;

import com.github.wjbakker.todostreamclient.json.JsonStreamFactory;

import java.util.stream.Stream;

public class TodoResource {
    private final String uri;
    private final JsonStreamFactory jsonStreamFactory;

    public TodoResource(String uri) {
        this (uri, new JsonStreamFactory());
    }

    TodoResource(String uri, JsonStreamFactory jsonStreamFactory) {
        this.uri = uri;
        this.jsonStreamFactory = jsonStreamFactory;
    }

    public Stream<Todo> stream() {
        return jsonStreamFactory.create(uri, new TodoReader());
    }
}
