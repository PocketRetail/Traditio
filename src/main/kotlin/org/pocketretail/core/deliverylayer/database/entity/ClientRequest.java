package org.pocketretail.core.deliverylayer.database.entity;

import org.pocketretail.core.deliverylayer.database.constant.ClientRequestType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ClientRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_request_id", updatable = false, nullable = false, columnDefinition = "varchar(255)")
    private int clientRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_request_type", nullable = false, columnDefinition = "varchar(255)")
    private ClientRequestType clientRequestType;

    @Column(name = "client_request_uri", nullable = false, columnDefinition = "varchar(255)")
    private String clientRequestURI;

    @ManyToOne(targetEntity = Client.class)
    @JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "fk_client_id"))
    private Client clientId;

    @Column(name = "client_request_name", nullable = false, columnDefinition = "varchar(255)")
    private String clientRequestName;
}
