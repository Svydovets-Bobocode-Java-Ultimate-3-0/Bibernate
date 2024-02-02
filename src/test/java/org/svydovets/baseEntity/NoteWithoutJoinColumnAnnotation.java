package org.svydovets.baseEntity;

import org.svydovets.annotation.Entity;
import org.svydovets.annotation.Id;
import org.svydovets.annotation.ManyToOne;
import org.svydovets.annotation.Table;

@Entity
@Table(name = "notes")
public class NoteWithoutJoinColumnAnnotation {

    @Id
    private Integer id;

    private String title;

    private String body;

    @ManyToOne
    private Person person;
}
