package org.svydovets.dao;

/**
 * Holds configuration properties for database connections, including the URL,
 * username, and password required to establish a connection.
 */
public record Properties(String url, String user, String password, boolean isShownSql) {
}
