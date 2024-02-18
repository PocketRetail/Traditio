package org.pocketretail.core.deliverylayer.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_id", updatable = false, nullable = false, columnDefinition = "int")
    private Integer pageId;

    @Column(name = "page_name", nullable = false, columnDefinition = "varchar(255)")
    private String pageName;


}
