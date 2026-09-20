package ru.academits.todolistservlet.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.text.StringEscapeUtils;
import ru.academits.todolistservlet.data.TodoItem;
import ru.academits.todolistservlet.data.TodoItemsInMemoryRepository;
import ru.academits.todolistservlet.data.TodoItemsRepository;

import java.io.IOException;
import java.io.Serial;
import java.util.List;

@WebServlet("")
public class TodoListServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1231L;

    private TodoItemsRepository todoItemsRepository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        todoItemsRepository = new TodoItemsInMemoryRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");

        String baseUrl = req.getContextPath() + "/";
        String resetCssUrl = req.getContextPath() + "/reset.css";
        String styleCssUrl = req.getContextPath() + "/style.css";

        HttpSession session = req.getSession();
        String createError = session.getAttribute("createError") != null
                ? session.getAttribute("createError").toString()
                : "";

        String saveError = session.getAttribute("saveError") != null
                ? session.getAttribute("saveError").toString()
                : "";

        String editText = session.getAttribute("editText") != null
                ? session.getAttribute("editText").toString()
                : null;

        Integer editId = (Integer) session.getAttribute("editId");

        session.removeAttribute("createError");
        session.removeAttribute("saveError");

        List<TodoItem> todoItems = todoItemsRepository.getAll();

        StringBuilder todoItemStringBuilder = new StringBuilder();

        for (TodoItem todoItem : todoItems) {
            boolean isEditing = editId != null && editId == todoItem.getId();

            if (isEditing) {
                todoItemStringBuilder.append(String.format("""
                                <li>
                                    <form class="edit-form" action="%s" method="POST">
                                        <div class="edit-container%s">
                                            <input class="edit-todo-item-text-field" type="text" name="text" value="%s">
                                            <div class="error-message">%s</div>
                                        </div>
                                
                                        <button class="save-button" type="submit" name="action" value="save">Сохранить</button>
                                        <button class="cancel-button" type="submit" name="action" value="cancel">Отменить</button>
                                
                                        <input type="hidden" name="id" value="%s">
                                    </form>
                                </li>
                                """,
                        baseUrl,
                        saveError.isEmpty() ? "" : " invalid",
                        StringEscapeUtils.escapeHtml4(editText != null ? editText : todoItem.getText()),
                        StringEscapeUtils.escapeHtml4(saveError),
                        todoItem.getId()));
            } else {
                todoItemStringBuilder.append(String.format("""
                                <li>
                                    <span class="todo-item-text">%s</span>
                                
                                    <form action="%s" method="POST">
                                        <button class="edit-button" type="submit" name="action" value="edit">Редактировать</button>
                                        <button class="delete-button" type="submit" name="action" value="delete">Удалить</button>
                                
                                        <input type="hidden" name="id" value="%s">
                                    </form>
                                </li>
                                """,
                        StringEscapeUtils.escapeHtml4(todoItem.getText()),
                        baseUrl,
                        todoItem.getId()));
            }
        }

        resp.getWriter().printf("""
                        <!DOCTYPE html>
                        <html lang="ru">
                        <head>
                            <meta charset="UTF-8">
                            <title>Список задач</title>
                            <link rel="stylesheet" href="%s">
                            <link rel="stylesheet" href="%s">
                        </head>
                        <body>
                        <div class="main-block">
                            <h1 class="main-header">Список задач</h1>
                        
                            <form action="%s" method="POST" class="new-todo-form">
                                <div>
                                    <label for="new-todo-item-text-field">Введите текст задачи</label>
                                </div>
                        
                                <div class="field-wrapper%s">
                                    <input id="new-todo-item-text-field" type="text" name="text">
                                    <div class="error-message">%s</div>
                                </div>
                        
                                <button type="submit" name="action" value="create">Создать</button>
                            </form>
                        
                            <ul class="todo-list">%s</ul>
                        </div>
                        </body>
                        </html>
                        """,
                resetCssUrl,
                styleCssUrl,
                baseUrl,
                createError.isEmpty() ? "" : " invalid",
                StringEscapeUtils.escapeHtml4(createError),
                todoItemStringBuilder);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");

        try {
            switch (action) {
                case "create" -> {
                    String text = req.getParameter("text");

                    if (text == null || text.isBlank()) {
                        HttpSession session = req.getSession();
                        session.setAttribute("createError", "Необходимо заполнить поле");
                    } else {
                        todoItemsRepository.create(text.trim());
                    }
                }

                case "edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    req.getSession().setAttribute("editId", id);
                }

                case "save" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    String text = req.getParameter("text");

                    if (text == null || text.isBlank()) {
                        HttpSession session = req.getSession();
                        session.setAttribute("editId", id);
                        session.setAttribute("editText", text);
                        session.setAttribute("saveError", "Необходимо заполнить поле");
                    } else {
                        todoItemsRepository.update(new TodoItem(id, text.trim()));
                        req.getSession().removeAttribute("editId");
                        req.getSession().removeAttribute("editText");
                    }
                }

                case "cancel" -> {
                    req.getSession().removeAttribute("editId");
                    req.getSession().removeAttribute("editText");
                }

                case "delete" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    todoItemsRepository.delete(id);
                    req.getSession().removeAttribute("editId");
                }
            }
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        resp.sendRedirect(getServletContext().getContextPath() + "/");
    }
}