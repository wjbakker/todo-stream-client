package com.github.wjbakker.todostreamclient.todo;

import com.google.gson.stream.JsonReader;
import com.github.wjbakker.todostreamclient.json.EntityReader;

import java.io.IOException;

public class TodoReader implements EntityReader<Todo> {
    @Override
    public Todo read(JsonReader jsonReader) throws IOException {
        String title = null;
        boolean completed = false;
        long order = 0L;

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String s = jsonReader.nextName();
            switch (s) {
                case "title":
                    title = jsonReader.nextString();
                    break;
                case "completed":
                    completed = jsonReader.nextBoolean();
                    break;
                case "order":
                    order = jsonReader.nextLong();
                    break;
                case "url":
                    jsonReader.skipValue();
                    break;
            }
        }
        jsonReader.endObject();

        return new Todo(title, completed, order);
    }
}
