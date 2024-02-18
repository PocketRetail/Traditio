package org.pocketretail.core.deliverylayer.rest.model

import java.util.Date

class DeliveryLayerErrorResponse(
    msg: String,
    status: Int,
    timestamp: Date,
    var stackTrace: String
) : DeliveryLayerCommonResponse(msg, status, timestamp)
