package com.github.wjbakker.todostreamclient.todo;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TodoTest {
    @Test
    void shouldExposeFields() {
        String title = "TestTitle";
        boolean completed = true;
        long order = 1L;

        Todo todo = new Todo(title, completed, order);

        assertThat(todo.getTitle()).isEqualTo(title);
        assertThat(todo.isCompleted()).isEqualTo(completed);
        assertThat(todo.getOrder()).isEqualTo(order);
    }

    @Test
    void shouldIncludeFieldsInToString() {
        String title = "TestTitle";
        boolean completed = true;
        long order = 1L;

        Todo todo = new Todo(title, completed, order);
        String string = todo.toString();

        assertThat(string).contains("title='TestTitle'");
        assertThat(string).contains("completed=true");
        assertThat(string).contains("order=1");
    }

    @Test
    void shouldVerifyHashCodeEquals() {
        EqualsVerifier.forClass(Todo.class).usingGetClass().verify();
    }
}
