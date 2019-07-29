package com.github.wjbakker.todostreamclient.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonStreamFactory {
    public <T> Stream<T> create(String uri, EntityReader<T> entityReader) {
        JsonSpliterator<T> spliterator = new JsonSpliterator<>(() -> getInputStream(uri), entityReader);
        return StreamSupport.stream(spliterator, false).onClose(spliterator::close);
    }

    private static InputStream getInputStream(String uri) {
        HttpURLConnection connection = createConnection(uri);
        return getInputStream(connection);
    }

    private static InputStream getInputStream(HttpURLConnection connection) {
        try {
            validateResponseCode(connection);
            return connection.getInputStream();
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to get input stream", exception);
        }
    }

    private static void validateResponseCode(HttpURLConnection connection) {
        int responseCode = getResponseCode(connection);
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IllegalStateException("Failed : HTTP error code : " + responseCode);
        }
    }

    private static int getResponseCode(HttpURLConnection connection) {
        try {
            return connection.getResponseCode();
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to get response code", exception);
        }
    }

    private static HttpURLConnection createConnection(String uri) {
        URL url = getUrl(uri);
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            return connection;
        }
        catch (IOException exception) {
            throw new UncheckedIOException("Failed to create connection", exception);
        }
    }

    private static URL getUrl(String uri) {
        try {
            return new URL(uri);
        }
        catch (IOException exception) {
            throw new UncheckedIOException("Failed to parse URL", exception);
        }
    }
}
