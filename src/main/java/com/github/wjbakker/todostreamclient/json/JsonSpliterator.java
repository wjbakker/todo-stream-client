package com.github.wjbakker.todostreamclient.json;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JsonSpliterator<T> implements AutoCloseable, Spliterator<T> {
    private static final String ALREADY_CLOSED = "Stream is already closed";
    private static final String UNABLE_TO_PROCESS_JSON = "Unable to process JSON";

    private final Supplier<InputStream> inputStreamSupplier;
    private final EntityReader<T> entityReader;

    private JsonReader jsonReader;
    private boolean closed;

    JsonSpliterator(Supplier<InputStream> inputStreamSupplier, EntityReader<T> entityReader) {
        this.inputStreamSupplier = inputStreamSupplier;
        this.entityReader = entityReader;
    }

    @Override
    public synchronized void close() {
        doClose();
    }

    @Override
    public synchronized boolean tryAdvance(Consumer<? super T> action) {
        assertNotClosed();

        try {
            if (!isOpened()) {
                open();
            }

            if (hasNext()) {
                action.accept(next());
                return true;
            }

            doClose();
            return false;
        } catch (IOException exception) {
            doClose();
            throw new UncheckedIOException(UNABLE_TO_PROCESS_JSON, exception);
        } catch (RuntimeException exception) {
            doClose();
            throw new JsonSpliteratorException(UNABLE_TO_PROCESS_JSON, exception);
        }
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE | ORDERED | NONNULL;
    }

    private boolean hasNext() throws IOException {
        JsonToken token = jsonReader.peek();
        switch (token) {
            case END_ARRAY:
                return false;
            case BEGIN_OBJECT:
                return true;
            default:
                throw new IllegalStateException(String.format("Unexpected token '%s'", token));
        }
    }

    private T next() throws IOException {
        return entityReader.read(jsonReader);
    }

    private void doClose() {
        if (closed) {
            return;
        }
        closed = true;

        System.out.println("Closing Stream");
        safeClose(jsonReader);
    }

    private void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException exception) {
                System.out.println("Failed to close: " + exception.getMessage());
            }
        }
    }

    private void assertNotClosed() {
        if (closed) {
            throw new IllegalStateException(ALREADY_CLOSED);
        }
    }

    private boolean isOpened()
    {
        return jsonReader != null;
    }

    private void open() throws IOException {
        System.out.println("Opening Stream");

        jsonReader = new Gson().newJsonReader(new InputStreamReader(inputStreamSupplier.get()));
        jsonReader.beginArray();
    }
}
