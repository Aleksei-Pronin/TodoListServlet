package ru.academits.todolistservlet.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TodoItemsInMemoryRepository implements TodoItemsRepository {
    private final List<TodoItem> todoItems = new ArrayList<>();
    private final AtomicInteger currentItemId = new AtomicInteger(1);

    @Override
    public List<TodoItem> getAll() {
        synchronized (todoItems) {
            return todoItems.stream()
                    .map(TodoItem::new)
                    .toList();
        }
    }

    @Override
    public void create(TodoItem item) {
        synchronized (todoItems) {
            int id = currentItemId.getAndIncrement();
            todoItems.add(new TodoItem(id, item.getText()));
        }
    }

    @Override
    public void update(TodoItem item) {
        synchronized (todoItems) {
            TodoItem repositoryItem = todoItems.stream()
                    .filter(it -> it.getId() == item.getId())
                    .findFirst()
                    .orElse(null);

            if (repositoryItem == null) {
                throw new IllegalArgumentException("TodoItem with id " + item.getId() + " not found");
            }

            repositoryItem.setText(item.getText());
        }
    }

    @Override
    public void delete(int itemId) {
        synchronized (todoItems) {
            todoItems.removeIf(item -> item.getId() == itemId);
        }
    }
}
