CREATE SCHEMA IF NOT EXISTS tables;
CREATE TABLE tables.client (
                        client_id VARCHAR(255) PRIMARY KEY,
                        client_description VARCHAR(255),
                        client_uri VARCHAR(255) NOT NULL,
                        is_active BOOLEAN NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ClientRequest.sql
CREATE TABLE tables.client_request (
                                client_request_id SERIAL PRIMARY KEY,
                                client_request_type VARCHAR(255) NOT NULL,
                                client_request_uri VARCHAR(255) NOT NULL,
                                client_id VARCHAR(255) NOT NULL,
                                client_request_name VARCHAR(255),
                                FOREIGN KEY (client_id) REFERENCES tables.client(client_id)
);

-- ClientRequestParameter.sql
CREATE TABLE tables.client_request_parameter (
                                          client_request_parameter_id SERIAL PRIMARY KEY,
                                          client_request_id INT NOT NULL,
                                          client_request_parameter_name VARCHAR(255) NOT NULL,
                                          client_request_parameter_type VARCHAR(255) NOT NULL,
                                          client_request_parameter_data_type VARCHAR(255) NOT NULL,
                                          client_request_parameter_data_type_name VARCHAR(255),
                                          client_request_parameter_of_type_data_type VARCHAR(255),
                                          client_request_parameter_of_type_data_type_name VARCHAR(255),
                                          parent_client_request_parameter_id INT,
                                          FOREIGN KEY (client_request_id) REFERENCES tables.client_request(client_request_id),
                                          FOREIGN KEY (parent_client_request_parameter_id) REFERENCES tables.client_request_parameter(client_request_parameter_id)
);

-- Page.sql
CREATE TABLE tables.page (
                      page_id SERIAL PRIMARY KEY,
                      page_name VARCHAR(255) NOT NULL
);

-- PageClientRequestConfiguration.sql
CREATE TABLE tables.page_client_request_configuration (
                                                   page_client_request_configuration_id SERIAL PRIMARY KEY,
                                                   page_id INT NOT NULL,
                                                   client_request_id INT NOT NULL,
                                                   FOREIGN KEY (page_id) REFERENCES tables.page(page_id),
                                                   FOREIGN KEY (client_request_id) REFERENCES tables.client_request(client_request_id)
);

-- PageRequestParameterConfiguration.sql
CREATE TABLE tables.page_request_parameter_configuration (
                                                      page_client_request_configuration_id INT NOT NULL,
                                                      client_request_parameter_id INT NOT NULL,
                                                      PRIMARY KEY (page_client_request_configuration_id, client_request_parameter_id),
                                                      FOREIGN KEY (page_client_request_configuration_id) REFERENCES tables.page_client_request_configuration(page_client_request_configuration_id),
                                                      FOREIGN KEY (client_request_parameter_id) REFERENCES tables.client_request_parameter(client_request_parameter_id)
);
