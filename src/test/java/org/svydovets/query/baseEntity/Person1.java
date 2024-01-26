package org.svydovets.query.baseEntity;

import org.svydovets.annotation.Column;
import org.svydovets.annotation.Entity;
import org.svydovets.annotation.Id;
import org.svydovets.annotation.Table;

@Entity
@Table(name = "persons")
public class Person1 {

    @Id
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column
    private Integer age;

    private String male;
}
