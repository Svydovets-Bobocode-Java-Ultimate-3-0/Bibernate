package org.svydovets.baseEntity;

import lombok.Builder;
import org.svydovets.annotation.*;

@Entity
@Builder
@Table(name = "persons")
public class PersonWithVersionAnnotation {

    @Id
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column
    private Integer age;

    @Version
    private Integer version;

    private String male;
}
