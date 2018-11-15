/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author takbekov
 */
public class Database {

    private final String url = "jdbc:sqlite:amazon.db";

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url);
        } catch (Exception e) {
            System.out.println("Database:connection error:" + e.getLocalizedMessage());
            return null;
        }
    }

    public void create(String table, String fields, Statement state) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE ").append(table).append(" (").append(fields).append(")");
            state.executeUpdate(String.valueOf(sql));
        } catch (Exception e) {
            System.out.println("Database:create error:" + e.getLocalizedMessage());
            System.out.println("Database:create error:table:" + table + ":fields:" + fields);
        }
    }

    public void insert(String table, StringBuilder fields, StringBuilder values, Statement state) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(table).append(" (").append(fields).append(") ");
            sql.append("VALUES (").append(values).append(")");
            state.executeUpdate(String.valueOf(sql));
        } catch (Exception e) {
            System.out.println("Database:insert error:" + e.getLocalizedMessage());
            System.out.println("Database:insert error:table:" + table + ":fields:" + fields + ":values:" + values);
        }
    }

    public void delete(String table, String condition, Statement state) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM ").append(table);
            if (condition != null) {
                sql.append("WHERE ").append(condition);
            }
            state.executeUpdate(String.valueOf(sql));
            System.out.println("Deleted rows:table:" + table);
        } catch (Exception e) {
            System.out.println("Database:delete error:" + e.getLocalizedMessage());
            System.out.println("Database:delete error:table:" + table + ":condition:" + condition);
        }
    }

    public ResultSet select(String table, String fields, String condition, String orderBy, Statement state) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ").append(fields);
            sql.append(" FROM ").append(table);
            if (condition != null) {
                sql.append(" WHERE ").append(condition);
            }
            if (orderBy != null) {
                sql.append(" ORDER BY ").append(orderBy);
            }
            return state.executeQuery(String.valueOf(sql));
        } catch (Exception e) {
            System.out.println("Database:delete error:" + e.getLocalizedMessage());
            System.out.println("Database:delete error:table:" + table + ":condition:" + condition);
            return null;
        }
    }

}
