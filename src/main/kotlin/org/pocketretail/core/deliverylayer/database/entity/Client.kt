package org.pocketretail.core.deliverylayer.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Client {

    @Id
    @Column(name = "client_id", updatable = false, nullable = false, columnDefinition = "varchar(255)")
    private String clientId;

    @Column(name = "client_uri", nullable = false, columnDefinition = "varchar(255)")
    private String clientURI;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean")
    private boolean active;

}
