package com.busra.connecting.service;

import org.springframework.stereotype.Service;

@Service
public interface NewsStreams {
    String PAGEVIEWS_OUT = "my-pageviews-topics";
    String REPORTS_OUT = "my-reports-topics";
    String NEWS_OUT = "my-news-topics";
    String USERS_OUT = "my-users-topics";
    String AUTHS_OUT = "my-auths-topics";
    String PAGEVIEWS_IN = "my-pageviews-topics";
    String REPORTS_IN = "my-reports-topics";
    String NEWS_IN = "my-news-topics";
    String USERS_IN = "my-users-topics";
    String OFFERS_OUT = "my-offers-topics";
    String OFFERS_IN = "my-offers-topics";
    String OFFERVIEWS_OUT = "my-offerviews-topics";
}
