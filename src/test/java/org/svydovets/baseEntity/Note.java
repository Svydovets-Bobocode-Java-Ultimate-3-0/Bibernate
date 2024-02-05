package org.svydovets.baseEntity;

import org.svydovets.annotation.Entity;
import org.svydovets.annotation.Id;
import org.svydovets.annotation.JoinColumn;
import org.svydovets.annotation.ManyToOne;
import org.svydovets.annotation.Table;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    private Integer id;

    private String title;

    private String body;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;
}
