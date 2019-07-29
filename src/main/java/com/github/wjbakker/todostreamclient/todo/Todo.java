package com.github.wjbakker.todostreamclient.todo;

import java.util.Objects;

public class Todo {
    private final String title;
    private final boolean completed;
    private final long order;

    public Todo(String title, boolean completed, long order) {
        this.title = title;
        this.completed = completed;
        this.order = order;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public long getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Todo todo = (Todo) other;
        return Objects.equals(title, todo.title)
                && completed == todo.completed
                && order == todo.order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, completed, order);
    }

    @Override
    public String toString() {
        return "Todo{" +
                "title='" + title + "'" +
                ", completed=" + completed +
                ", order=" + order +
                '}';
    }
}
