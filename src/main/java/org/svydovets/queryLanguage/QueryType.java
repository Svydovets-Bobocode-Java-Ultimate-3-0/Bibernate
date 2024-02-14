package org.svydovets.queryLanguage;

public enum QueryType {

    SELECT,
    UPDATE,
    INSERT,
    DELETE;

    public String getType() {
        return name();
    }
}
