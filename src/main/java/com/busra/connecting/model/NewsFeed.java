package com.busra.connecting.model;

import com.busra.connecting.model.serdes.NewsFeedDeserializer;
import com.busra.connecting.model.serdes.ReviewDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class NewsFeed {
    private String topic;
    private String summary;
    private List<String> tags;
    private Date date;
   // @JsonDeserialize(using = ReviewDeserializer.class)
    private List<Review> mediaReviews;
    private List<String> mediaParts;
    private List<String> offers;

    public NewsFeed() {
    }

    public NewsFeed(String summary, String topic, List<String> tags, List<Review> mediaReviews, List<String> mediaParts, Date date, List<String> offers) {
        this.summary = summary;
        this.topic = topic;
        this.tags = tags;
        this.date = date;
        this.mediaReviews = mediaReviews;
        this.mediaParts = mediaParts;
        this.offers = offers;
    }
    @Override
    public String toString() {
        return "NewsFeed{" +
                "summary='" + summary + '\'' +
                ", topic='" + topic + '\'' +
                ", tags=" + tags +
                ", date=" + date +
                ", mediaReviews=" + mediaReviews +
                ", mediaParts=" + mediaParts +
                ", offers=" + offers +
                '}';
    }
    public String getTopic() {
        return topic;
    }

    public String getSummary() {
        return summary;
    }

    public List<Review> getMediaReviews() {
        return mediaReviews;
    }

    public Date getDate() {
        return date;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getMediaParts() {
        return mediaParts;
    }

    public List<String> getOffers() {
       return this.offers;
   }
}
