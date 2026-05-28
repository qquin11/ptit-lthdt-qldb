package com.app.dao;

import com.app.model.DatabaseHelper;
import java.sql.Connection;

public abstract class BaseDAO {
    protected Connection getConnection() {
        return DatabaseHelper.getConnection();
    }
}