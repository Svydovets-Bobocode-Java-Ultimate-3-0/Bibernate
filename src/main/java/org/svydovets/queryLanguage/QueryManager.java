package org.svydovets.queryLanguage;

import org.svydovets.query.ParameterNameResolver;
import org.svydovets.util.EntityReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Java Query language (JQL) - The query language uses an SQL-like syntax to select objects
 * or values based on entity abstract schema types and relationships among them. The object
 * entity name is used as the table name, and the object field names are used as column names.
 *
 * <p>Creating Queries Using the JQL
 *
 * <p>Example #1:
 * <blockquote><pre>
 * final String jpQuery = "select p from Person p
 *                         where p.firstName = :firstName
 *                         and p.lastName = :lastName
 *                         and p.age = :age";
 * QueryManager<Person> queryManager = QueryManager.of(jpQuery, Person.class);
 * queryManager.setParameters("age", 20);
 * queryManager.setParameters("firstName", "firstName");
 * queryManager.setParameters("lastName", "lastName");
 *
 * String nativeQuery = queryManager.toSqlString();
 * Object[] parameters = queryManager.getParameters();
 * </pre></blockquote>
 *
 *
 * <p>Example #2
 * <blockquote><pre>
 * final String jpQuery = "select n from Note n where n.Person.id = :personId";
 * QueryManager<Person> queryManager = QueryManager.of(jpQuery, Note.class);
 * queryManager.setParameters("personId", 1L);
 * String nativeQuery = queryManager.toSqlString();
 * Object[] parameters = queryManager.getParameters();
 * </pre></blockquote>
 *
 * <p>Implement only one level relationship.
 * <blockquote><pre>
 * n.Person.id => n.person_id - it's correct;
 * n.Person.User.id => it's not correct;
 * </pre></blockquote>
 *
 * <p>Named Parameters in Queries
 *
 * <p>Named parameters are query parameters that are prefixed with a colon (:).
 * Named parameters in a query are bound to an argument by the following method:
 *<blockquote><pre>
 * queryManager.setParameter(String name, Object value)
 * </pre></blockquote>
 *
 * @param <T>
 */
public class QueryManager<T> {

    private static final String LEFT_JOIN = "LEFT JOIN %s %s ON %s = %s";

    private static final String JOIN_SHORT = "join_%d";

    Class<T> entityType;

    private Map<String, Object> parameters = new HashMap<>();

    private List<String> arrQuery;

    private List<String> joinQueryList = new ArrayList<>();

    public QueryManager(final Class<T> entityType) {
        this.entityType = entityType;
    }

    public static <T> QueryManager<T> of(Class<T> entityType) {
        return new QueryManager<>(entityType);
    }

    public static <T> QueryManager<T> of(final String query, final Class<T> entityType) {
        QueryManager<T> queryManager = QueryManager.of(entityType);
        queryManager.query(query);

        return queryManager;
    }

    /**
     * set jql query into QueryManager
     *
     * @param query
     */
    public void query(final String query) {
        this.arrQuery = Arrays.asList(query.split(" "));

        initJoinQuery(entityType);
    }

    /**
     * set parameters into QueryManager
     *
     * @param paramName
     * @param value
     */
    public void setParameters(final String paramName, final Object value) {
        parameters.put(":" + paramName, value);
    }

    /**
     * get parameters into QueryManager
     *
     * @return
     */
    public Object[] getParameters() {
        Set<String> keyParameters = parameters.keySet();

        return arrQuery.stream()
                .filter(field -> keyParameters.contains(replace(field)))
                .map(key -> parameters.get(replace(key)))
                .toArray();
    }

    /**
     * get entityType from QueryManager
     *
     * @return
     */
    public Class<T> getEntityType() {
        return entityType;
    }

    /**
     * build native query from QueryManager
     *
     * @return
     */
    public String toSqlString() {
        Set<String> keyParams = parameters.keySet();
        final String tableName = ParameterNameResolver.resolveTableName(entityType);
        final Map<String, String> columnNameByFieldNameMap = ParameterNameResolver
                .getColumnNameByFieldNameForColumnFielsMap(entityType);
        final Set<String> fieldNames = columnNameByFieldNameMap.keySet();
        final Map<String, String> columnNameByFieldNameForEntityMap = ParameterNameResolver
                .getColumnNameByFieldNameForEntityFielsMap(entityType);
        final Set<String> columnNameByFieldNameForEntityKeys = columnNameByFieldNameForEntityMap.keySet();
        List<String> nativeQueryList = arrQuery.stream()
                .map(element -> getValueSqlSubqueryString(element, keyParams))
                .map(element -> {
                    String columnName = replace(element);
                    Optional<String> fieldNameOptional = getNameSqlSubqueryString(fieldNames, columnName);
                    if (fieldNameOptional.isEmpty()) {
                        return element;
                    }

                    String nativeColumnName = columnNameByFieldNameMap.get(fieldNameOptional.get());

                    return element.replace(fieldNameOptional.get(), nativeColumnName);
                })
                .map(element -> {
                    String columnName = replace(element);
                    Optional<String> fieldNameOptional = getNameSqlSubqueryString(columnNameByFieldNameForEntityKeys, columnName);
                    if (fieldNameOptional.isEmpty()) {
                        return element;
                    }

                    String nativeColumnName = columnNameByFieldNameForEntityMap.get(fieldNameOptional.get().toLowerCase());

                    return element.toLowerCase().replace(fieldNameOptional.get().toLowerCase(), nativeColumnName);
                })
                .collect(Collectors.toList());

        nativeQueryList.set(getIndexTableName(nativeQueryList), tableName);
        if (isSelectQuery()) {
            if (!joinQueryList.isEmpty()) {
                nativeQueryList = nativeQueryList.stream()
                        .map(item -> {
                            if (item.equalsIgnoreCase("WHERE")) {
                                return String.join(" ", joinQueryList) + " " + item;
                            }

                            return item;
                        })
                        .collect(Collectors.toList());
            }

            nativeQueryList.set(1, "*");
        }

        return String.join(" ", nativeQueryList);
    }

    private String getValueSqlSubqueryString(String element, Set<String> keyParams) {
        return keyParams.contains(replace(element))
                ? replace(element, replace(element),"?")
                : element;
    }

    private Optional<String> getNameSqlSubqueryString(Set<String> fieldNames, final String queryElement) {
        Predicate<String> stringPredicate = key -> queryElement.toLowerCase().contains("." + key.toLowerCase())
                || (queryElement.toLowerCase().contains(key.toLowerCase()) && queryElement.length() == key.length());

        return fieldNames.stream()
                .filter(stringPredicate)
                .findAny();
    }

    private String replace(final String oldStr) {
        return replace(replace(replace(oldStr, ","), "("), ")");
    }

    private String replace(final String oldStr, final String target) {
        return replace(oldStr, target, "");
    }

    private String replace(final String oldStr, final String target, final String replacement) {
        return oldStr.replace(target, replacement);
    }

    private int getIndexTableName(List<String> nativeQueryList) {
        return nativeQueryList.indexOf(getQueryType(nativeQueryList.get(0))) + 1;
    }

    private void initJoinQuery(Class<T> entityType) {
        if (isSelectQuery()) {
            List<Field> entityFields = EntityReflectionUtils.getEntityFields(entityType);
            entityFields.forEach(field -> {
                var currentEntityId = EntityReflectionUtils.getIdField(entityType).getName();
                var joinIndex = String.format(JOIN_SHORT, entityFields.indexOf(field));
                var joinEntity = ParameterNameResolver.resolveTableName(field.getType());
                var joinEntityColumnName = ParameterNameResolver.resolveJoinColumnName(field);
                var index = getIndexTableName(arrQuery) + 1;
                String joinQueryStr;
                if (arrQuery.get(index).equalsIgnoreCase("WHERE")) {
                    joinQueryStr = String.format(LEFT_JOIN, joinEntity, joinIndex,
                            currentEntityId, joinIndex + "." + joinEntityColumnName);
                } else {
                    joinQueryStr = String.format(LEFT_JOIN, joinEntity, joinIndex,
                             joinIndex + "." + currentEntityId, arrQuery.get(index) + "." + joinEntityColumnName);
                }

                joinQueryList.add(joinQueryStr);
            });
        }
    }

    private boolean isSelectQuery() {
        return arrQuery.get(0).toUpperCase().equals(QueryType.SELECT.name());
    }

    private String getQueryType(final String queryTypeParam) {
        return switch (queryTypeParam.toUpperCase()) {
            case "SELECT" -> "from";
            case "INSERT" -> "into";
            case "UPDATE" -> "update";
            case "DELETE" -> "from";
            default -> "";
        };
    }
}
