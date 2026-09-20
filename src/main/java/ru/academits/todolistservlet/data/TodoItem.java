package ru.academits.todolistservlet.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class TodoItem {
    private int id;
    private String text;

    public TodoItem(TodoItem item) {
        this(item.id, item.text);
    }
}