package com.github.wjbakker.todostreamclient.todo;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.assertj.core.api.Assertions.assertThat;

class TodoReaderTest {
    private TodoReader todoReader;

    @BeforeEach
    void beforeEach() {
        todoReader = new TodoReader();
    }

    @Test
    void shouldRead() throws IOException {
        String json = "{\"title\":\"TestTitle\", \"completed\": True, \"order\": 1, \"url\":\"TestIgnoredValue\"}";
        JsonReader jsonReader = new Gson().newJsonReader(new InputStreamReader(new ByteArrayInputStream(json.getBytes())));

        Todo todo = todoReader.read(jsonReader);

        assertThat(todo.getTitle()).isEqualTo("TestTitle");
        assertThat(todo.isCompleted()).isTrue();
        assertThat(todo.getOrder()).isEqualTo(1L);
    }
}
