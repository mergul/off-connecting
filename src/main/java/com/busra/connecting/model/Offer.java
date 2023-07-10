package com.busra.connecting.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
@JsonDeserialize(builder = Offer.Builder.class)
public class Offer implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    private final ObjectId id;
    private final String ownerId;
    private final String newsId;
    private final String newsOwnerId;
    private List<String> tags;
    private final List<Review> mediaReviews;
    private final List<String> mediaParts;
    private final String topic;
    private final String summary;
    private final double price;
    private final Date startDate;
    private final Date endDate;
    private final Boolean active;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Offer(ObjectId id, String ownerId, String newsId, String newsOwnerId, List<String> tags, List<Review> mediaReviews, List<String> mediaParts, String topic, String summary, double price, Date startDate, Date endDate, Boolean active){
        this.id=id;
        this.ownerId =ownerId;
        this.newsId =newsId;
        this.newsOwnerId = newsOwnerId;
        this.tags = tags;
        this.mediaReviews=mediaReviews;
        this.mediaParts = mediaParts;
        this.topic =topic;
        this.summary=summary;
        this.price=price;
        this.startDate=startDate;
        this.endDate=endDate;
        this.active=active;
    }

    public ObjectId getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }
    public String getNewsId() {
        return newsId;
    }
    public List<String> getTags() {
        return tags;
    }
    public List<Review> getMediaReviews() {
        return mediaReviews;
    }
    public List<String> getMediaParts() {
        return mediaParts;
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

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getNewsOwnerId() {
        return newsOwnerId;
    }

    public Boolean getActive() {
        return active;
    }
    @Override
    public String toString() {
        return "Offer{" +
                "id=" + id +
                ", ownerId='" + ownerId + '\'' +
                ", newsId='" + newsId + '\'' +
                ", newsOwnerId='" + newsOwnerId + '\'' +
                ", tags=" + tags +
                ", mediaReviews='" + mediaReviews + '\'' +
                ", mediaParts=" + mediaParts +
                ", topic='" + topic + '\'' +
                ", summary='" + summary + '\'' +
                ", price='" + price + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", active='" + active + '\'' +
                '}';
    }
    public static Builder of() {
        return new Builder();
    }

    public static Builder of(ObjectId id) {
        return new Builder(id);
    }
    public static Builder from(Offer offer) {
        final Builder builder= new Builder();
        builder.id=offer.id;
        builder.ownerId=offer.ownerId;
        builder.newsId= offer.newsId;
        builder.newsOwnerId= offer.newsOwnerId;
        builder.tags=offer.tags;
        builder.mediaReviews=offer.mediaReviews;
        builder.mediaParts=offer.mediaParts;
        builder.topic=offer.topic;
        builder.summary =offer.summary;
        builder.price=offer.price;
        builder.startDate=offer.startDate;
        builder.endDate=offer.endDate;
        builder.active=offer.active;
        return builder;
    }
    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static final class Builder {
        private ObjectId id;
        private String ownerId;
        private String newsId;
        private String newsOwnerId;
        private List<String> tags;
        private List<Review> mediaReviews;
        private List<String> mediaParts;
        private String topic;
        private String summary;
        private double price;
        private Date startDate;
        private Date endDate;
        private Boolean active;
        public Builder() {
        }

        public Builder(ObjectId id) {
            this.id = id;
        }
        public Builder withId(ObjectId id) {
            this.id = id;
            return this;
        }
        public Builder withOwnerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }
        public Builder withNewsId(String newsId) {
            this.newsId = newsId;
            return this;
        }
        public Builder withNewsOwnerId(String newsOwnerId) {
            this.newsOwnerId = newsOwnerId;
            return this;
        }
        public Builder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }
        public Builder withMediaReviews(List<Review> mediaReviews) {
            this.mediaReviews = mediaReviews;
            return this;
        }
        public Builder withMediaParts(List<String> mediaParts){
            this.mediaParts = mediaParts;
            return this;
        }
        public Builder withTopic(String topic) {
            this.topic = topic;
            return this;
        }
        public Builder withSummary(String summary) {
            this.summary = summary;
            return this;
        }
        public Builder withPrice(double price) {
            this.price = price;
            return this;
        }
        public Builder withStartDate(Date startDate) {
            this.startDate = startDate;
            return this;
        }
        public Builder withEndDate(Date endDate) {
            this.endDate = endDate;
            return this;
        }
        public Builder withActive(Boolean active) {
            this.active = active;
            return this;
        }
        public Offer build(){
            return new Offer(id, ownerId, newsId, newsOwnerId, tags, mediaReviews, mediaParts, topic, summary, price, startDate, endDate, active);
        }
    }
}
