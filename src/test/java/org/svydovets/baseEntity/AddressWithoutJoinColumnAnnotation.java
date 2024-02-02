package org.svydovets.baseEntity;

import org.svydovets.annotation.Entity;
import org.svydovets.annotation.Id;
import org.svydovets.annotation.OneToOne;
import org.svydovets.annotation.Table;

@Entity
@Table(name = "addresses")
public class AddressWithoutJoinColumnAnnotation {

    @Id
    private Integer id;

    private String city;

    @OneToOne
    private Person person;
}
