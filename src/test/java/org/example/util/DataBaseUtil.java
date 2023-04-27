package org.example.util;

import org.example.model.Task;
import org.example.model.TaskStatus;
import org.example.model.User;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class DataBaseUtil {


    public static User findUserByUsername(String username) {

        try {

            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM USER_ WHERE username = '" + username + "'");
                 ResultSet result = statement.executeQuery()
            ) {
                User user = null;
                if (result.next()) {
                    user = readUser(result);
                }
                return user;
            }
        }catch (SQLException e) {

            throw new RuntimeException(e);

        }
    }

    public static User findUserById(Long id) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM USER_ WHERE id = " + id);
             ResultSet result = statement.executeQuery()
        ) {
            User user = null;
            if (result.next()) {
                user = readUser(result);
            }
            return user;
        }
    }

    private static User readUser(ResultSet result) throws SQLException {
        User user = new User();
        user.setId(result.getLong("id"));
        user.setName(result.getString("name"));
        user.setUsername(result.getString("username"));
        user.setPassword(result.getString("password"));
        return user;
    }

    public static void insertUser(User user) {

        try {

            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO USER_(name, username, password) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS)
            ) {
                statement.setString(1, user.getName());
                statement.setString(2, user.getUsername());
                statement.setString(3, user.getPassword());
                statement.executeUpdate();
                ResultSet result = statement.getGeneratedKeys();
                if (result.next()) {
                    Long id = result.getLong(1);
                    user.setId(id);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(e);

        }
    }

    public static void updateUser(User user) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE USER_ SET name = ?, username = ?, password = ? WHERE id = ?");
        ) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPassword());
            statement.setLong(4, user.getId());
            statement.executeUpdate();
        }
    }


    public static List<Task> findTaskByUser(Long userId) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM TASK WHERE user_id = " + userId);
             ResultSet result = statement.executeQuery()
        ) {
            List<Task> tasks = new ArrayList<>();
            while (result.next()) {
                tasks.add(readTask(result));
            }
            return tasks;
        }
    }

    public static Task findTaskById(Long id) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM TASK WHERE id = " + id);
             ResultSet result = statement.executeQuery()
        ) {
            Task task = null;
            if (result.next()) {
                task = readTask(result);
            }
            return task;
        }
    }

    private static Task readTask(ResultSet result) throws SQLException {
        Task task = new Task();
        task.setId(result.getLong("id"));
        task.setTitle(result.getString("title"));
        task.setDescription(result.getString("description"));
        task.setStatus(TaskStatus.valueOf(result.getString("status")));
        task.setUser(findUserById(result.getLong("user_id")));

        task.setClosedAt((Date) result.getObject("closed_at"));
        return task;
    }

    public static void insertTask(Task task) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO TASK(title, description, status, user_id, closed_at) VALUES(?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, task.getDescription());
            statement.setString(2, task.getDescription());
            statement.setString(3, task.getStatus().name());
            statement.setLong(4, task.getUser().getId());
            statement.setObject(5, task.getClosedAt());
            statement.executeUpdate();
            ResultSet result = statement.getGeneratedKeys();
            if (result.next()) {
                Long id = result.getLong(1);
                task.setId(id);
            }
        }
    }

    public static void updateTask(Task task) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE TASK SET title = ?, description = ?, status = ?, user_id = ?, closed_at = ? WHERE id = ?");
        ) {
            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setString(3, task.getStatus().name());
            statement.setLong(4, task.getUser().getId());
            statement.setObject(5, task.getClosedAt());
            statement.setLong(6, task.getId());
            statement.executeUpdate();
        }
    }


    private static Connection getConnection() throws SQLException {
        return DriverManager
                .getConnection("jdbc:h2:tcp://localhost:9092/nio:~/schedule", "sa", "password");
    }
}
