package com.busra.connecting.model.serdes;

import com.busra.connecting.model.NewsFeed;
import com.busra.connecting.model.Review;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class NewsFeedDeserializer extends StdDeserializer<NewsFeed> {
    public NewsFeedDeserializer() {
        this(null);
    }
    protected NewsFeedDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public NewsFeed deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
        final JsonNode node = jp.getCodec().readTree(jp);
        final String summary = node.get("summary").asText();
        final String topic = node.get("topic").asText();
        final List<String> tags = new ArrayList<>();
        final ArrayNode tagz = (ArrayNode) node.get("tags");
        tagz.iterator().forEachRemaining(tag-> tags.add(tag.asText()));
        final long date = node.get("date").asLong();
        Date myt=new Date(date);
        List<Review> myList=new ArrayList<>();
        final ArrayNode reviews= (ArrayNode) node.get("mediaReviews");
        final Iterator<JsonNode> iterator=reviews.iterator();
        iterator.forEachRemaining((nod)->{
            String doc_description= nod.get("_doc_description").asText();
            String doc_name= nod.get("_doc_name").asText();
            String file_name= nod.get("_file_name").asText();
            String file_type= nod.get("_file_type").asText();
            Boolean has_medium= nod.get("_has_medium").asBoolean();
            myList.add(new Review(doc_description,doc_name,file_name,file_type,has_medium));
        });
        final List<String> mediaParts=node.findValuesAsText("mediaParts");
        final List<String> offers=node.findValuesAsText("offers");

        return new NewsFeed(summary, topic, tags, myList, mediaParts, myt, offers);
    }
}
