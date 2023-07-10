package com.busra.connecting.model;

import java.util.Date;
import java.util.List;

public class OfferFeed {
    private String newsOwnerId;
    private String newsId;
    private String topic;
    private String summary;
    private double price;
    private List<String> tags;
    private List<Review> mediaReviews;
    private List<String> mediaParts;
    private Date date;

    public OfferFeed() {
    }

    public OfferFeed(String newsOwnerId, String newsId, String topic, String summary, double price, List<String> tags, List<Review> mediaReviews, List<String> mediaParts, Date date) {
        this.newsOwnerId = newsOwnerId;
        this.newsId = newsId;
        this.topic = topic;
        this.summary = summary;
        this.price = price;
        this.tags = tags;
        this.mediaReviews = mediaReviews;
        this.mediaParts = mediaParts;
        this.date = date;
    }
    public String getNewsId() {
        return newsId;
    }
    public String getTopic() {
        return topic;
    }
    public String getSummary() {
        return summary;
    }
    public double getPrice() {
        return price;
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

    public String getNewsOwnerId() {
        return newsOwnerId;
    }
}
