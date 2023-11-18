package com.busra.connecting.model.serdes;

import com.busra.connecting.model.Review;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReviewDeserializer extends StdDeserializer<List<Review>> {

    public ReviewDeserializer(){this(null);}
    protected ReviewDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public List<Review> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
        final JsonNode node = jp.getCodec().readTree(jp);
        List<Review> myList=new ArrayList<>();
        final Iterator<JsonNode> iterator=node.iterator();
        iterator.forEachRemaining((nod)->{
            String doc_description= nod.get("_doc_description").asText();
            String doc_name= nod.get("_doc_name").asText();
            String file_name= nod.get("_file_name").asText();
            String file_type= nod.get("_file_type").asText();
            Boolean has_medium= nod.get("_has_medium").asBoolean();
            myList.add(new Review(doc_description,doc_name,file_name,file_type,has_medium));
        });
        return myList;
    }
}
