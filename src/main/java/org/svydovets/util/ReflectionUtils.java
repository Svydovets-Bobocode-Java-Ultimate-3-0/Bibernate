package org.svydovets.util;

import org.svydovets.annotation.Id;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

public class ReflectionUtils {

    public static Field[] getSortedEntityFields(Class<?> entityClazz) {
        return Arrays.stream(entityClazz.getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }

    public static Field[] getSortedEntityFieldsWithoutIdField(Class<?> entityClazz) {
        return Arrays.stream(entityClazz.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }
}
