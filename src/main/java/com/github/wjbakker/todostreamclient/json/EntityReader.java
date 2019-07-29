package com.github.wjbakker.todostreamclient.json;

import com.google.gson.stream.JsonReader;

import java.io.IOException;

public interface EntityReader<T> {
    T read(JsonReader jsonReader) throws IOException;
}
