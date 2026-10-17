package database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    public static Connection getConnection() {

        Connection con = null;

        try {

            String url = "jdbc:mysql://localhost:3306/smartcampus";
            String username = "root";
            String password = "SmartCampus@123";

            con = DriverManager.getConnection(url, username, password);

            System.out.println("Database connected successfully!");

        } catch (Exception e) {

            System.out.println("Database connection failed!");
            System.out.println(e);
        }

        return con;
    }
}