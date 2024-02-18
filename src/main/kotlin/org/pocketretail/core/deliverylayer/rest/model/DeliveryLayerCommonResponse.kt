package org.pocketretail.core.deliverylayer.rest.model

import java.util.Date

abstract class DeliveryLayerCommonResponse(
    var msg: String,
    var status: Int,
    var timestamp: Date
)
