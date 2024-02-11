package org.svydovets.baseEntity;

import org.svydovets.annotation.*;

@Entity
@Table(name = "persons")
public class PersonWithTwoVersionAnnotations {

    @Id
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column
    private Integer age;

    @Version
    private Integer version1;

    @Version
    private Integer version2;

    private String male;
}
