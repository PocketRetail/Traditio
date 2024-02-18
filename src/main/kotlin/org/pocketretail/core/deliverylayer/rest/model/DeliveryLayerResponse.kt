package org.pocketretail.core.deliverylayer.rest.model

import java.util.Date

class DeliveryLayerResponse(
    msg: String,
    status: Int,
    timestamp: Date,
    var data: Any? = null // Optional: Standardmäßig null, wenn nicht anders angegeben
) : DeliveryLayerCommonResponse(msg, status, timestamp)
