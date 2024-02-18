package org.pocketretail.core.deliverylayer.database.converter

import org.pocketretail.core.deliverylayer.database.constant.ClientRequestType
import org.pocketretail.core.deliverylayer.database.constant.ParameterType
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

@WritingConverter
class ClientRequestTypeWriteConverter : Converter<ClientRequestType, String> {
    override fun convert(tokenType: ClientRequestType): String {
        return tokenType.name
    }
}

@ReadingConverter
class ClientRequestTypeReadConverter : Converter<String, ClientRequestType> {
    override fun convert(tokenType: String): ClientRequestType {
        return ClientRequestType.valueOf(tokenType)
    }
}

@WritingConverter
class ParameterTypeWriteConverter : Converter<ParameterType, String> {
    override fun convert(tokenType: ParameterType): String {
        return tokenType.name
    }
}

@ReadingConverter
class ParameterTypeReadConverter : Converter<String, ParameterType> {
    override fun convert(tokenType: String): ParameterType {
        return ParameterType.valueOf(tokenType)
    }
}