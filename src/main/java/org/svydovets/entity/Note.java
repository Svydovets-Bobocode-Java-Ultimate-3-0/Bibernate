package org.svydovets.entity;

import lombok.Data;
import org.svydovets.annotation.Column;
import org.svydovets.annotation.Entity;
import org.svydovets.annotation.Id;
import org.svydovets.annotation.Table;

@Entity
@Data
@Table(name = "notes")
public class Note {

    @Id
    private Long id;

    private String title;

    private String body;

    @Column(name = "user_id")
    private Long userId;
}
