package com.github.wjbakker.todostreamclient.json;

import com.google.gson.Gson;
import com.github.wjbakker.todostreamclient.todo.Todo;
import com.github.wjbakker.todostreamclient.todo.TodoReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

class JsonSpliteratorTest {
    private static final Todo TODO1 = new Todo("TestTitle1", true, 1);
    private static final Todo TODO2 = new Todo("TestTitle2", true, 2);
    private static final Todo TODO3 = new Todo("TestTitle3", true, 3);

    private JsonSpliterator<Todo> jsonSpliterator;

    private InputStream inputStream;
    private EntityReader<Todo> entityReader;

    @BeforeEach
    void beforeEach() {
        entityReader = new TodoReader();

        jsonSpliterator = new JsonSpliterator<>(() -> inputStream, entityReader);
    }

    @Test
    void tryAdvance_emptyResponse_shouldReturnFalse() {
        givenJson(new Gson().toJson(List.of()));

        List<Todo> actualValues = new ArrayList<>();
        assertThat(jsonSpliterator.tryAdvance(actualValues::add)).isFalse();
        assertThat(actualValues).isEmpty();
    }

    @Test
    void tryAdvance_responseWithSingleValue_shouldReturnFalseAfterLast() {
        givenJson(new Gson().toJson(List.of(TODO1)));

        List<Todo> actualValues = new ArrayList<>();
        assertThat(jsonSpliterator.tryAdvance(actualValues::add)).isTrue();
        assertThat(jsonSpliterator.tryAdvance(actualValues::add)).isFalse();

        assertThat(actualValues).containsExactly(TODO1);
    }

    @Test
    void tryAdvance_responseWithMultipleValues_shouldReturnFalseAfterLast() {
        givenJson(new Gson().toJson(List.of(TODO1, TODO2, TODO3)));

        List<Todo> actualValues = new ArrayList<>();
        assertThat(jsonSpliterator.tryAdvance(actualValues::add)).isTrue();
        assertThat(jsonSpliterator.tryAdvance(actualValues::add)).isTrue();
        assertThat(jsonSpliterator.tryAdvance(actualValues::add)).isTrue();
        assertThat(jsonSpliterator.tryAdvance(actualValues::add)).isFalse();

        assertThat(actualValues).containsExactly(TODO1, TODO2, TODO3);
    }

    @Test
    void tryAdvance_notAnArray_shouldThrowException() {
        givenJson(new Gson().toJson(TODO1));

        try {
            jsonSpliterator.tryAdvance(aValue -> {});
            failBecauseExceptionWasNotThrown(JsonSpliteratorException.class);
        } catch (JsonSpliteratorException aException) {
            assertThat(aException).hasMessage("Unable to process JSON");
            assertThat(aException.getCause()).hasMessageStartingWith("Expected BEGIN_ARRAY but was BEGIN_OBJECT");
        }

        verifyClosed();
    }

    @Test
    void tryAdvance_invalidArray_shouldThrowException() {
        givenJson(new Gson().toJson(List.of(List.of())));

        try {
            jsonSpliterator.tryAdvance(aValue -> {});
            failBecauseExceptionWasNotThrown(JsonSpliteratorException.class);
        } catch (JsonSpliteratorException aException) {
            assertThat(aException).hasMessage("Unable to process JSON");
            assertThat(aException.getCause()).hasMessage("Unexpected token 'BEGIN_ARRAY'");
        }

        verifyClosed();
    }

    @Test
    void tryAdvance_invalidElementInArray_shouldThrowException() {
        givenJson(new Gson().toJson(List.of("TestInvalidValue")));

        try {
            jsonSpliterator.tryAdvance(aValue -> {});
            failBecauseExceptionWasNotThrown(JsonSpliteratorException.class);
        } catch (JsonSpliteratorException aException) {
            assertThat(aException).hasMessage("Unable to process JSON");
            assertThat(aException.getCause()).hasMessage("Unexpected token 'STRING'");
        }

        verifyClosed();
    }

    @Test
    void tryAdvance_emptyResponse_shouldCloseInputStream() {
        givenJson(new Gson().toJson(List.of()));

        List<Todo> actualValues = new ArrayList<>();
        jsonSpliterator.tryAdvance(actualValues::add);
        assertThat(actualValues).isEmpty();

        verifyClosed();
    }

    @Test
    void tryAdvance_allElementsConsumed_shouldCloseInputStream() {
        givenJson(new Gson().toJson(List.of(TODO1, TODO2, TODO3)));

        List<Todo> actualValues = new ArrayList<>();
        jsonSpliterator.tryAdvance(actualValues::add);
        jsonSpliterator.tryAdvance(actualValues::add);
        jsonSpliterator.tryAdvance(actualValues::add);
        jsonSpliterator.tryAdvance(actualValues::add);
        assertThat(actualValues).containsExactly(TODO1, TODO2, TODO3);

        verifyClosed();
    }

    @Test
    void tryAdvance_hasRemainingElements_shouldNotCloseInputStream() {
        givenJson(new Gson().toJson(List.of(TODO1, TODO2, TODO3)));

        List<Todo> actualValues = new ArrayList<>();
        jsonSpliterator.tryAdvance(actualValues::add);
        jsonSpliterator.tryAdvance(actualValues::add);
        assertThat(actualValues).containsExactly(TODO1, TODO2);

        verifyNotClosed();
    }

    @Test
    void tryAdvance_alreadyClosed_shouldThrowException() {
        givenJson(new Gson().toJson(List.of()));

        List<Todo> actualValues = new ArrayList<>();
        jsonSpliterator.tryAdvance(actualValues::add);
        assertThat(actualValues).isEmpty();

        try {
            jsonSpliterator.tryAdvance(actualValues::add);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException aException) {
            assertThat(aException).hasMessageStartingWith("Stream is already closed");
        }
    }

    @Test
    void close_notInitialized_shouldDoNothing() {
        givenJson(new Gson().toJson(List.of()));

        jsonSpliterator.close();

        verifyZeroInteractions(inputStream);
    }

    @Test
    void close_alreadyClosed_shouldDoNothing() {
        givenJson(new Gson().toJson(List.of()));

        List<Todo> actualValues = new ArrayList<>();
        jsonSpliterator.tryAdvance(actualValues::add);
        assertThat(actualValues).isEmpty();

        verifyClosed();

        reset(inputStream);

        jsonSpliterator.close();

        verifyZeroInteractions(inputStream);
    }

    @Test
    void close_hasRemainingElements_shouldCloseInputStream() {
        givenJson(new Gson().toJson(List.of(TODO1, TODO2)));

        jsonSpliterator.tryAdvance(action -> {});

        verifyNotClosed();

        jsonSpliterator.close();

        verifyClosed();
    }

    @Test
    void close_allElementsConsumed_shouldCloseInputStream() {
        givenJson(new Gson().toJson(List.of(TODO1, TODO2, TODO3)));

        jsonSpliterator.tryAdvance(action -> {});
        jsonSpliterator.tryAdvance(action -> {});
        jsonSpliterator.tryAdvance(action -> {});
        jsonSpliterator.tryAdvance(action -> {});

        verifyClosed();

        reset(inputStream);

        jsonSpliterator.close();

        verifyZeroInteractions(inputStream);
    }

    @Test
    void close_throwsIOException_shouldBeSuppressed() throws IOException {
        givenJson(new Gson().toJson(List.of()));

        String message = "TestExpectedException";
        doThrow(new IOException(message)).when(inputStream).close();

        jsonSpliterator.tryAdvance(action -> {});

        verifyClosed();
    }

    @Test
    void trySplit_shouldNotSplit() {
        assertThat(jsonSpliterator.trySplit()).isNull();
    }

    @Test
    void estimateSize_shouldBeUnknown() {
        assertThat(jsonSpliterator.estimateSize()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void characteristics_shouldBeNonnull() {
        assertThat(jsonSpliterator.hasCharacteristics(Spliterator.IMMUTABLE)).isTrue();
        assertThat(jsonSpliterator.hasCharacteristics(Spliterator.NONNULL)).isTrue();
        assertThat(jsonSpliterator.hasCharacteristics(Spliterator.ORDERED)).isTrue();
    }

    @Test
    void stream_allElementsConsumed_shouldCloseInputStream() {
        givenJson(new Gson().toJson(List.of(TODO1, TODO2, TODO3)));

        Stream<Todo> stream = StreamSupport.stream(jsonSpliterator, false);

        List<Todo> todos = stream.collect(Collectors.toList());

        verifyClosed();

        assertThat(todos).containsOnly(TODO1, TODO2, TODO3);
    }

    @Test
    void stream_hasRemainingElements_shouldCloseInputStream() {
        givenJson(new Gson().toJson(List.of(TODO1, TODO2, TODO3)));

        Stream<Todo> stream = StreamSupport.stream(jsonSpliterator, false).onClose(jsonSpliterator::close);

        List<Todo> todos;
        try(stream) {
            todos = stream.limit(2).collect(Collectors.toList());
        }

        verifyClosed();

        assertThat(todos).containsOnly(TODO1, TODO2);
    }

    @Test
    void stream_notAnArray_shouldThrowExceptionAndCloseInputStream() {
        givenJson(new Gson().toJson(TODO1));

        Stream<Todo> stream = StreamSupport.stream(jsonSpliterator, false).onClose(jsonSpliterator::close);

        try(stream) {
            stream.forEach(todo -> {});
            failBecauseExceptionWasNotThrown(JsonSpliteratorException.class);
        }
        catch(JsonSpliteratorException exception) {
            assertThat(exception).hasMessage("Unable to process JSON");
            assertThat(exception.getCause()).hasMessageStartingWith("Expected BEGIN_ARRAY but was BEGIN_OBJECT");
        }

        verifyClosed();
    }

    private void givenJson(String json) {
        inputStream = spy(new ByteArrayInputStream(json.getBytes()));
    }

    private void verifyClosed() {
        try {
            verify(inputStream).close();
        } catch (IOException exception) {
            fail("Unexpected exception", exception);
        }
    }

    private void verifyNotClosed() {
        try {
            verify(inputStream, never()).close();
        } catch (IOException exception) {
            fail("Unexpected exception", exception);
        }
    }
}
