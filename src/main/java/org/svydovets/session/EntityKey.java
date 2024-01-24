package org.svydovets.session;

public record EntityKey<T>(Class<T> clazz, Object id) {
}
