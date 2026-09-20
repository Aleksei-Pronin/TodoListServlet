package ru.academits.todolistservlet.data;

import java.util.List;

public interface TodoItemsRepository {
    List<TodoItem> getAll();

    void create(String itemText);

    void update(TodoItem item);

    void delete(int itemId);
}