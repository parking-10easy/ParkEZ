package com.parkez.parkinglot.domain.repository;

import java.sql.Connection;
import java.sql.SQLException;

public interface PageStateRepository {

    int readPage(Connection connection) throws SQLException;

    void updatePage(Connection connection, int nextPage) throws SQLException;

}
