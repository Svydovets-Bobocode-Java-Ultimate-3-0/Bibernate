package org.svydovets.baseEntity;

import org.svydovets.annotation.Entity;
import org.svydovets.annotation.Id;
import org.svydovets.annotation.JoinColumn;
import org.svydovets.annotation.ManyToOne;
import org.svydovets.annotation.Table;

@Entity
@Table(name = "notes")
public class NoteWithoutManyToOneAnnotation {

    @Id
    private Integer id;

    private String title;

    private String body;

    @JoinColumn
    private Person person;
}
