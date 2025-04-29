package com.parkez.user.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.exception.UserErrorCode;

import java.sql.*;

public class JdbcUserReader {
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;

    public JdbcUserReader(String jdbcUrl, String dbUser, String dbPassword) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    // getUserByEmailAndRole 구현
    public User getUserByEmailAndRole(String email, UserRole role) {
        String sql = """
                SELECT id, email, role
                FROM users
                WHERE email = ?
                AND role = ?
                AND deleted_at IS NULL
                """;

        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, email);
            ps.setString(2, role.name());

            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    Long id = result.getLong("id");
                    String userEmail = result.getString("email");
                    UserRole userRole = UserRole.valueOf(result.getString("role"));
                    return User.ofIdEmailRole(id, userEmail, userRole);
                }
            }
            throw new ParkingEasyException(UserErrorCode.USER_NOT_FOUND);
        } catch (SQLException e) {
            throw new RuntimeException("UserReader JDBC 오류", e);
        }
    }
}
