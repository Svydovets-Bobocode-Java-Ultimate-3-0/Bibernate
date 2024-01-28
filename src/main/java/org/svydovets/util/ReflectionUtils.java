package org.svydovets.util;

import org.svydovets.annotation.Entity;
import org.svydovets.annotation.Id;
import org.svydovets.exception.AnnotationMappingException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

public class ReflectionUtils {

    public static Field[] getEntityFieldsSortedByName(Class<?> entityClazz) {
        return Arrays.stream(entityClazz.getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }

    public static Field[] getEntityFieldsWithoutIdFieldSortedByName(Class<?> entityClazz) {
        return Arrays.stream(entityClazz.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }

    public static Field getIdField(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new AnnotationMappingException(String.format(
                    "Not a managed type. Class must be marked as'@Entity': %s",
                    clazz.getName())
            );
        }
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny()
                .orElseThrow(() -> new AnnotationMappingException(String.format(
                        "Identifier is not specified for type: %s (Each entity must have field marked as '@Id')",
                        clazz.getName()))
                );
    }
}
