package org.pocketretail.core.deliverylayer.graphql.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netflix.graphql.dgs.client.GraphQLResponse;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GraphQLSchemaResponse {

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



    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SchemaResponseData {

        private SchemaResponseDataSchema __schema;

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class SchemaResponseDataSchema {

            private List<DataSchemaType> types;

            @Getter
            @Setter
            @AllArgsConstructor
            @NoArgsConstructor
            public static class DataSchemaType {
                private String kind;
                private String name;
                private List<Field> fields;
                @JsonProperty("inputFields")
                private List<Field> inputFields;
                @JsonProperty("enumValues")
                private List<EnumValue> enumValues;

                @Getter
                @Setter
                @AllArgsConstructor
                @NoArgsConstructor
                public static class EnumValue {
                    private String name;
                    @JsonProperty("isDeprecated")
                    private Boolean isDeprecated;
                    @JsonProperty("deprecationReason")
                    private String deprecationReason;
                }


                @Getter
                @Setter
                @AllArgsConstructor
                @NoArgsConstructor
                public static class Field {
                    private String name;
                    private List<Arg> args;
                    private Type type;
                    @JsonProperty("isDeprecated")
                    private Boolean isDeprecated;
                    @JsonProperty("deprecationReason")
                    private String deprecationReason;


                    @Getter
                    @Setter
                    @AllArgsConstructor
                    @NoArgsConstructor
                    public static class Arg {
                        private String name;
                        private Type type;
                    }

                    @Getter
                    @Setter
                    @AllArgsConstructor
                    @NoArgsConstructor
                    public static class Type {
                        private String kind;
                        private String name;
                        @JsonProperty("ofType")
                        private OfType ofType;

                        @Getter
                        @Setter
                        @AllArgsConstructor
                        @NoArgsConstructor
                        public static class OfType {
                            private String kind;
                            private String name;
                            @JsonProperty("ofType")
                            private OfType ofType;
                        }
                    }
                }
            }

        }
    }
}