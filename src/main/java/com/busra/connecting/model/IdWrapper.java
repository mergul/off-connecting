package com.busra.connecting.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@JsonDeserialize
public class IdWrapper {
    @JsonSerialize(using = ToStringSerializer.class)
    private final ObjectId id;
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public IdWrapper(ObjectId id1){
        this.id = id1;
    }
    public ObjectId getId() {
        return id;
    }
}
