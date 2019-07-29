package com.github.wjbakker.todostreamclient.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSpliteratorExceptionTest {
    private JsonSpliteratorException jsonSpliteratorException;

    @Test
    void shouldExtendRuntimeException() {
        assertThat(RuntimeException.class).isAssignableFrom(JsonSpliteratorException.class);
    }

    @Test
    void shouldConstructWithMessageAndCause() {
        String message = "TestMessage";
        Throwable cause = new RuntimeException();

        jsonSpliteratorException = new JsonSpliteratorException(message, cause);

        assertThat(jsonSpliteratorException).hasMessage(message);
        assertThat(jsonSpliteratorException).hasCause(cause);
    }
}
