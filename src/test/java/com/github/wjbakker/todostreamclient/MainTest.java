package com.github.wjbakker.todostreamclient;

import com.github.wjbakker.todostreamclient.todo.Todo;
import com.github.wjbakker.todostreamclient.todo.TodoResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MainTest {
    private Main main;

    private TodoResource todoResource;

    @BeforeEach
    void beforeEach() {
        todoResource = mock(TodoResource.class);

        main = new Main(todoResource);
    }

    @Test
    void shouldClose() {
        AtomicBoolean isClosed = new AtomicBoolean();

        Stream<Todo> stream = Stream.of(mock(Todo.class), mock(Todo.class), mock(Todo.class))
                .onClose(() -> isClosed.set(true));
        when(todoResource.stream()).thenReturn(stream);

        main.run();

        assertThat(isClosed).isTrue();
    }
}
