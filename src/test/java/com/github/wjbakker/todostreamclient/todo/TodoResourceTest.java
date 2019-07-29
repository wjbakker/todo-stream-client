package com.github.wjbakker.todostreamclient.todo;

import com.github.wjbakker.todostreamclient.json.JsonStreamFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TodoResourceTest {
    private TodoResource todoResource;

    private String uri = "TestUri";
    private JsonStreamFactory jsonStreamFactory = mock(JsonStreamFactory.class);

    @BeforeEach
    void beforeEach()
    {
        todoResource = new TodoResource(uri, jsonStreamFactory);
    }

    @Test
    void shouldGetStream() {
        Stream<Object> stream = mock(Stream.class);

        when(jsonStreamFactory.create(eq(uri), any())).thenReturn(stream);

        assertThat(todoResource.stream()).isEqualTo(stream);
    }
}
