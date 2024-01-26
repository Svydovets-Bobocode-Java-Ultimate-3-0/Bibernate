package org.svydovets.query.baseEntity;

import org.svydovets.annotation.Column;
import org.svydovets.annotation.Entity;
import org.svydovets.annotation.Id;
import org.svydovets.annotation.Table;

@Entity
@Table
public class Person3 {

    @Id
    private Integer Id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private Integer age;
}
