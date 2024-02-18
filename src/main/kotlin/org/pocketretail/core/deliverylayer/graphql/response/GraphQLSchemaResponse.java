package org.pocketretail.core.deliverylayer.graphql.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netflix.graphql.dgs.client.GraphQLResponse;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GraphQLSchemaResponse {

    public GraphQLSchemaResponse(SchemaResponseData data) {
        this.data = data;
    }

    public GraphQLSchemaResponse() {
    }

    private SchemaResponseData data;

    public static SchemaResponseData fromGraphQLResponse(
            @NotNull GraphQLResponse response) {
        return response.extractValueAsObject("data", SchemaResponseData.class);

    }

    @NotNull
    public GraphQLSchemaResponse copy() {
        GraphQLSchemaResponse copy = new GraphQLSchemaResponse();
        copy.setData(this.data);
        return copy;
    }




    public static class SchemaResponseData {

        @JsonProperty("__schema")
        private SchemaResponseDataSchema schema;


        public static class SchemaResponseDataSchema {

            private List<DataSchemaType> types;


            public static class DataSchemaType {
                private String kind;
                private String name;
                private List<Field> fields;
                @JsonProperty("inputFields")
                private List<Field> inputFields;
                @JsonProperty("enumValues")
                private List<EnumValue> enumValues;


                public static class EnumValue {
                    private String name;
                    @JsonProperty("isDeprecated")
                    private Boolean isDeprecated;
                    @JsonProperty("deprecationReason")
                    private String deprecationReason;

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public Boolean getDeprecated() {
                        return isDeprecated;
                    }

                    public void setDeprecated(Boolean deprecated) {
                        isDeprecated = deprecated;
                    }

                    public String getDeprecationReason() {
                        return deprecationReason;
                    }

                    public void setDeprecationReason(String deprecationReason) {
                        this.deprecationReason = deprecationReason;
                    }
                }



                public static class Field {
                    private String name;
                    private List<Arg> args;
                    private Type type;
                    @JsonProperty("isDeprecated")
                    private Boolean isDeprecated;
                    @JsonProperty("deprecationReason")
                    private String deprecationReason;



                    public static class Arg {
                        private String name;
                        private Type type;

                        public String getName() {
                            return name;
                        }

                        public void setName(String name) {
                            this.name = name;
                        }

                        public Type getType() {
                            return type;
                        }

                        public void setType(
                                Type type) {
                            this.type = type;
                        }
                    }


                    public static class Type {
                        private String kind;
                        private String name;
                        @JsonProperty("ofType")
                        private OfType ofType;



                        public static class OfType {
                            private String kind;
                            private String name;
                            @JsonProperty("ofType")
                            private OfType ofType;

                            public String getKind() {
                                return kind;
                            }

                            public void setKind(String kind) {
                                this.kind = kind;
                            }

                            public String getName() {
                                return name;
                            }

                            public void setName(String name) {
                                this.name = name;
                            }

                            public OfType getOfType() {
                                return ofType;
                            }

                            public void setOfType(
                                    OfType ofType) {
                                this.ofType = ofType;
                            }
                        }

                        public String getKind() {
                            return kind;
                        }

                        public void setKind(String kind) {
                            this.kind = kind;
                        }

                        public String getName() {
                            return name;
                        }

                        public void setName(String name) {
                            this.name = name;
                        }

                        public OfType getOfType() {
                            return ofType;
                        }

                        public void setOfType(
                                OfType ofType) {
                            this.ofType = ofType;
                        }
                    }

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public List<Arg> getArgs() {
                        return args;
                    }

                    public void setArgs(
                            List<Arg> args) {
                        this.args = args;
                    }

                    public Type getType() {
                        return type;
                    }

                    public void setType(
                            Type type) {
                        this.type = type;
                    }

                    public Boolean getDeprecated() {
                        return isDeprecated;
                    }

                    public void setDeprecated(Boolean deprecated) {
                        isDeprecated = deprecated;
                    }

                    public String getDeprecationReason() {
                        return deprecationReason;
                    }

                    public void setDeprecationReason(String deprecationReason) {
                        this.deprecationReason = deprecationReason;
                    }
                }

                public String getKind() {
                    return kind;
                }

                public void setKind(String kind) {
                    this.kind = kind;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public List<Field> getFields() {
                    return fields;
                }

                public void setFields(
                        List<Field> fields) {
                    this.fields = fields;
                }

                public List<Field> getInputFields() {
                    return inputFields;
                }

                public void setInputFields(
                        List<Field> inputFields) {
                    this.inputFields = inputFields;
                }

                public List<EnumValue> getEnumValues() {
                    return enumValues;
                }

                public void setEnumValues(
                        List<EnumValue> enumValues) {
                    this.enumValues = enumValues;
                }
            }

            public List<DataSchemaType> getTypes() {
                return types;
            }

            public void setTypes(
                    List<DataSchemaType> types) {
                this.types = types;
            }
        }

        public SchemaResponseDataSchema getSchema() {
            return schema;
        }

        public void setSchema(
                SchemaResponseDataSchema schema) {
            this.schema = schema;
        }
    }

    public SchemaResponseData getData() {
        return data;
    }

    public void setData(
            SchemaResponseData data) {
        this.data = data;
    }
}