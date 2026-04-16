package com.prototype.ipossa;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class MyJDBC {
    public static Connection getConnection() throws Exception {
        Properties props = new Properties();

        try (InputStream input = MyJDBC.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) throw new Exception("config.properties not found");
            props.load(input);
        }

        String url = "jdbc:mysql://"
                + props.getProperty("db.host") + ":"
                + props.getProperty("db.port") + "/"
                + props.getProperty("db.name")
                + "?sslmode=require";

        return DriverManager.getConnection(url,
                props.getProperty("db.username"),
                props.getProperty("db.password"));
    }

}
