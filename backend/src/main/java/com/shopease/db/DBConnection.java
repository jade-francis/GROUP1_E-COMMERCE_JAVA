package com.shopease.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection — single place responsible for opening a connection
 * to the shopease PostgreSQL database.
 *
 * Credentials are NOT hardcoded here. They are read once, at startup,
 * from an external "db.properties" file that is listed in .gitignore
 * and therefore never committed to version control. This is the
 * difference between a student-project habit (secrets in source) and
 * a security-minded habit (secrets kept out of the codebase).
 *
 * To set up: copy db.properties.example to db.properties and fill in
 * your real values.
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static final Properties props = new Properties();

    // Static block runs ONCE, the first time this class is used.
    static {
        // Try the classpath first (works when db.properties sits next to the .class files),
        // then fall back to the current working directory.
        try (InputStream in = openConfig()) {
            if (in == null) {
                throw new IllegalStateException(
                    "Could not find " + CONFIG_FILE + ". " +
                    "Copy db.properties.example to db.properties and fill in your values.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
    }

    private static InputStream openConfig() throws IOException {
        InputStream in = DBConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        if (in != null) {
            return in;
        }
        java.io.File f = new java.io.File(CONFIG_FILE);
        return f.exists() ? new FileInputStream(f) : null;
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(
            props.getProperty("db.url"),
            props.getProperty("db.user"),
            props.getProperty("db.password"));
    }
}
