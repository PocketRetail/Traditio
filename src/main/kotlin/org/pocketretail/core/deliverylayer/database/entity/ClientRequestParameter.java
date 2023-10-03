package org.pocketretail.core.deliverylayer.database.entity;

import org.pocketretail.core.deliverylayer.database.constant.ParameterType;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ClientRequestParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_request_parameter_id", updatable = false, nullable = false, columnDefinition = "int")
    private Integer clientRequestParameterId;

    @ManyToOne(targetEntity = ClientRequest.class)
    @JoinColumn(name = "client_request_id", foreignKey = @ForeignKey(name = "fk_client_request_id"), columnDefinition = "int")
    private ClientRequest clientRequestId;

    @Column(name = "client_request_parameter_name", nullable = false, columnDefinition = "varchar(255)")
    private String clientRequestParameterName;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_request_parameter_type", nullable = false, columnDefinition = "varchar(255)")
    private ParameterType clientRequestParameterType;

    @Column(name = "client_request_parameter_data_type", nullable = false, columnDefinition = "varchar(255)")
    private String clientRequestParameterDataType;

    @Column(name = "client_request_parameter_data_type_name", columnDefinition = "varchar(255)")
    private String clientRequestParameterDataTypeName;

    @Column(name = "client_request_parameter_of_type_data_type", columnDefinition = "varchar(255)")
    private String clientRequestParameterOfTypeDataType;

    @Column(name = "client_request_parameter_of_type_data_type_name", columnDefinition = "varchar(255)")
    private String clientRequestParameterOfTypeDataTypeName;

    @ManyToOne(targetEntity = ClientRequestParameter.class)
    @JoinColumn(name = "parent_client_request_parameter_id", foreignKey = @ForeignKey(name = "fk_parent_client_request_parameter_id"))
    private ClientRequestParameter parentClientRequestParameterId;

    @OneToMany(targetEntity = ClientRequestParameter.class)
    @JoinColumn(name = "parent_client_request_parameter_id", foreignKey = @ForeignKey(name = "fk_parent_client_request_parameter_id"))
    private Set<ClientRequestParameter> childClientRequestParameterIds = new HashSet<>();

}
