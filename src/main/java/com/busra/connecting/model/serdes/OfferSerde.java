package com.busra.connecting.model.serdes;

import com.busra.connecting.model.Offer;
import org.springframework.kafka.support.serializer.JsonSerde;

public class OfferSerde extends JsonSerde<Offer> {
    public OfferSerde(){
        super();
        this.ignoreTypeHeaders();
    }
}
