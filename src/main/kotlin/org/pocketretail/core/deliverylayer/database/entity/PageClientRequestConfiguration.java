package org.pocketretail.core.deliverylayer.database.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PageClientRequestConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_client_request_configuration_id", updatable = false, nullable = false, columnDefinition = "int")
    private Integer pageClientRequestConfigurationId;

    @ManyToOne(targetEntity = Page.class)
    @JoinColumn(name = "page_id", foreignKey = @ForeignKey(name = "fk_page_id"))
    private Page pageId;

    @ManyToOne(targetEntity = ClientRequest.class)
    @JoinColumn(name = "client_request_id", foreignKey = @ForeignKey(name = "fk_client_request_id"))
    private ClientRequest clientRequestId;

    @ManyToMany(targetEntity = ClientRequestParameter.class)
    @JoinTable(name = "page_request_parameter_configuration",
            joinColumns = @JoinColumn(name = "page_client_request_configuration_id", referencedColumnName = "page_client_request_configuration_id"),
            inverseJoinColumns = @JoinColumn(name = "client_request_parameter_id", referencedColumnName = "client_request_parameter_id"))
    private Set<ClientRequestParameter> neededClientRequestParameters;
}
