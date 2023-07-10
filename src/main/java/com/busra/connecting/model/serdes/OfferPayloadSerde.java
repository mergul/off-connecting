package com.busra.connecting.model.serdes;

import com.busra.connecting.model.OfferPayload;
import org.springframework.kafka.support.serializer.JsonSerde;

public class OfferPayloadSerde extends JsonSerde<OfferPayload>{
    public OfferPayloadSerde(){
        super();
        this.ignoreTypeHeaders();
    }
}
