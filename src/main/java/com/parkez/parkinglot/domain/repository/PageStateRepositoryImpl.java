package com.parkez.parkinglot.domain.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PageStateRepositoryImpl implements PageStateRepository {

    @Override
    public int readPage(Connection connection) throws SQLException {
        String sql = """
                SELECT current_page
                 FROM public_data_page_state
                 WHERE id = 1
                 FOR UPDATE
                """;
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            if (resultSet.next()) {
                return resultSet.getInt("current_page");
            }
            throw new IllegalStateException("페이지 상태가 없음");
        }
    }

    @Override
    public void updatePage(Connection connection, int nextPage) throws SQLException {
        String sql = """
                UPDATE public_data_page_state
                 SET current_page = ?,
                    updated_at = NOW() 
                 WHERE id = 1
                """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, nextPage);
            preparedStatement.executeUpdate();
        }
    }
}
