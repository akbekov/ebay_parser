/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import database.Database;
import java.sql.Connection;
import java.sql.Statement;

/**
 *
 * @author takbekov
 */
public class AmazonParser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new AmazonParser().run();
    }

    private void run() {
        try {
            Database database = new Database();
            Connection connection = database.getConnection();
            System.out.println(connection);
            Statement state = connection.createStatement();
            database.create("item", "id INTEGER PRIMARY KEY, url TEXT NOT NULL UNIQUE, asin TEXT NOT NULL, title TEXT, status INTEGER DEFAULT 1, brand TEXT NOT NULL", state);
            state.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
