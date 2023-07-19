package com.busra.connecting.controller;

import com.busra.connecting.model.*;
import com.busra.connecting.service.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@Component
public class NewsHandler {

//    @Value("${topics.kafka.listcom-out}")
//    private String listcomTopics;
//    @Value("${topics.kafka.partitioncom-out}")
//    private String partitioncomTopics;
//    @Value("${topics.kafka.paymentcom-out}")
//    private String paymentcomTopics;
//    @Value("${topics.kafka.balancecom-out}")
//    private String balancecomTopics;
//    @Value("${topics.kafka.checkout-out}")
//    private String checkoutTopics;
//    @Value("${topics.kafka.usersHistories-out}")
//    private String usersHistoriesTopics;

    private final UserService userService;
    private final NewsService newsService;
    private final OfferService offerService;
    private final Sender kafkaSender;

    public NewsHandler(@Qualifier("userService") UserService userService,
                       @Qualifier("newsService") NewsService newsService,
                       @Qualifier("offerService") OfferService offerService,
                       Sender kafkaSender) {
        this.userService = userService;
        this.newsService = newsService;
        this.offerService = offerService;
        this.kafkaSender = kafkaSender;
    }

    private Mono<User> getAuthUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> (User) auth.getPrincipal());
    }

    Mono<Boolean> saveNewsMetadata(Mono<NewsFeed> newsFeedMono) {
        ObjectId newsId = new ObjectId();
        return getAuthUser().flatMap(user -> newsFeedMono.map(newsFeed -> News.of(newsId)
                .withOwnerId(user.getId().toHexString())
                .withSummary(newsFeed.getSummary())
                .withTopic(newsFeed.getTopic())
                .withOwner(user.getUsername())
                .withTags(newsFeed.getTags())
                .withMediaParts(newsFeed.getMediaParts())
                .withDate(newsFeed.getDate())
                .withCount(0L)
                .withClean(false)
                .withArrest(false).withOwnerUrl(user.getImage())
                .withMediaReviews(newsFeed.getMediaReviews())
                .withOffers(new ArrayList<>()).build()))
                .flatMapMany(this::addNews).reduce(true, (aLong, aLong2) -> aLong && aLong2);
    }

    private Flux<Boolean> addNews(News newNews) {
        return Mono.zip(this.newsService.save(newNews).subscribeOn(Schedulers.boundedElastic())
                        , this.kafkaSender.send(NewsStreams.PAGEVIEWS_OUT, newsService.extractNewsPayload(newNews), newNews.getId().toHexString().getBytes(), true).subscribeOn(Schedulers.boundedElastic())
                )
                .flatMapMany(objects -> Flux.just(objects.getT1(), objects.getT2()));
    }
    Mono<IdWrapper> saveOfferMetadata(Mono<OfferFeed> offerFeedMono) {
        ObjectId offerId = new ObjectId();
        return getAuthUser().flatMap(user -> offerFeedMono.map(newsFeed -> Offer.of(offerId)
                        .withOwnerId(user.getId().toHexString())
                        .withNewsId(newsFeed.getNewsId())
                        .withNewsOwnerId(newsFeed.getNewsOwnerId())
                        .withSummary(newsFeed.getSummary())
                        .withTopic(newsFeed.getTopic())
                        .withTags(newsFeed.getTags())
                        .withMediaParts(newsFeed.getMediaParts())
                        .withStartDate(newsFeed.getDate())
                        .withEndDate(newsFeed.getDate())
                        .withPrice(newsFeed.getPrice())
                        .withMediaReviews(newsFeed.getMediaReviews())
                        .withActive(true).build()))
                .flatMapMany(this::addOffer).reduce(true, (aLong, aLong2) -> aLong && aLong2).map(vv->new IdWrapper(offerId));
    }

    private Flux<Boolean> addOffer(Offer newOffer) {
        return Mono.zip(this.offerService.save(newOffer).subscribeOn(Schedulers.boundedElastic())
                        , this.kafkaSender.send(NewsStreams.OFFERVIEWS_OUT, this.offerService.extractOfferPayload(newOffer), newOffer.getId().toHexString().getBytes(), true).subscribeOn(Schedulers.boundedElastic())
                        , updateNews(newOffer).subscribeOn(Schedulers.boundedElastic())
                        , updateUser(newOffer).subscribeOn(Schedulers.boundedElastic())
                )
                .flatMapMany(objects -> Flux.just(objects.getT1(), objects.getT2(), objects.getT3()));
    }
    private Mono<Boolean> updateNews(Offer newOffer) {
        return this.newsService.findById(newOffer.getNewsId()).flatMap(news -> {
            List<String> offers=new ArrayList<>();
            if (news.getOffers()!=null)
                offers.addAll(news.getOffers());
            offers.add(newOffer.getId().toHexString());
            return this.newsService.save(News.from(news).withOffers(offers).build());
        });
    }
    private Mono<Boolean> updateUser(Offer newOffer) {
        return getAuthUser().flatMap(user ->{
            List<String> offers=new ArrayList<>();
            if (user.getOffers()!=null)
                offers.addAll(user.getOffers());
            offers.add(newOffer.getId().toHexString());
            return this.userService.save(User.from(user).withOffers(offers).build());
        }).map(u->true);
    }

    Mono<News> getNewsById(String id) {
        return this.newsService.findById(id).flatMap(news -> {
            kafkaSender.send(NewsStreams.PAGEVIEWS_OUT, newsService.extractNewsPayload(news), news.getId().toHexString().getBytes(), true).subscribeOn(Schedulers.boundedElastic());
            return Mono.just(news);
        });
    }

    Mono<Boolean> setNewsCounts(String id) {
        return this.newsService.findById(id)
                .flatMap(news -> kafkaSender.send(NewsStreams.PAGEVIEWS_OUT, newsService.extractNewsPayload(news), news.getId().toHexString().getBytes(), true).subscribeOn(Schedulers.boundedElastic()));
    }
    Flux<News> findAllNames() {
        return null;
    }

    Mono<User> getUserById(String id, String random, String fed) {
        return getAuthUser().flatMap(user -> {
            if (fed.equals("0")) kafkaSender.send(NewsStreams.AUTHS_OUT, UserPayload.of(user.getId().toHexString()).withTags(user.getTags()).withUsers(user.getUsers()).withIndex(0).withRandom(random).withIsAdmin(user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))).withOffers(user.getOffers()).build(), user.getId().toHexString().getBytes(), false).subscribeOn(Schedulers.boundedElastic());
            return Mono.just(user);
        });
    }
    public Flux<OfferPayload> getOffersList(List<String> ids) {
        return this.offerService.findAllByIds(ids);
    }
    Mono<Offer> getOfferById(String id) {
        return this.offerService.findById(id).flatMap(offer -> {
            this.kafkaSender.send(NewsStreams.OFFERVIEWS_OUT, this.offerService.extractOfferPayload(offer), offer.getId().toHexString().getBytes(), true).subscribeOn(Schedulers.boundedElastic());
            return Mono.just(offer);
        });
    }
    Mono<Boolean> closeOfferById(String id) {
        return this.offerService.findById(id).flatMap(offer -> this.offerService.completeOffer(offer));
    }
    Mono<Boolean> completeOffer(String newsId, String offerId) {
        return Mono.zip(getAuthUser().subscribeOn(Schedulers.boundedElastic()),
                this.newsService.findById(newsId).subscribeOn(Schedulers.boundedElastic()), this.offerService.findById(offerId).subscribeOn(Schedulers.boundedElastic()))
                .flatMapMany(objects -> {
                    User user= objects.getT1();
                    News news= objects.getT2();
                    Offer offer=  objects.getT3();
                    return Mono.zip(this.userService.completeOffer(user, newsId).subscribeOn(Schedulers.boundedElastic()),
                    this.newsService.completeOffer(news).subscribeOn(Schedulers.boundedElastic()),
                    this.offerService.completeOffer(offer).subscribeOn(Schedulers.boundedElastic()));
                })
                .then(Mono.just(true));

    }
    public Mono<Boolean> addTag(Mono<UserTag> body, String random) {
        return Mono.zip(getAuthUser().subscribeOn(Schedulers.boundedElastic()), body.subscribeOn(Schedulers.boundedElastic()))
                .flatMapMany(objects -> {
                    UserTag userTag=objects.getT2();
                    String tag = userTag.getTag();
                    List<String> followings = new ArrayList<String>();
                    boolean isTag = tag.charAt(0) == '#';
                    User.Builder builder = User.from(objects.getT1());
                    if (isTag && !objects.getT1().getTags().contains(objects.getT2().getTag().substring(1))) {
                        followings.addAll(objects.getT1().getTags());
                        followings.add(tag.substring(1));
                        builder.withTags(followings);
                    } else if (!objects.getT1().getUsers().contains(objects.getT2().getTag().substring(1))) {
                        followings.addAll(objects.getT1().getUsers());
                        followings.add(objects.getT2().getTag().substring(1));
                        builder.withUsers(followings);
                    }
                    User user= builder.build();
                    return Mono.zip(this.userService.save(builder.build()).subscribeOn(Schedulers.boundedElastic()),
                            kafkaSender.send(NewsStreams.AUTHS_OUT, UserPayload.of(user.getId().toHexString()).withTags(isTag ? Collections.singletonList(userTag.getTag()) : Collections.emptyList()).withUsers(isTag ? Collections.emptyList() : Collections.singletonList(userTag.getTag())).withIndex(isTag ? 1 : 2).withRandom(random).withIsAdmin(user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))).build(), user.getId().toHexString().getBytes(), false).subscribeOn(Schedulers.boundedElastic()),
                            !isTag ? manageFollowers(userTag, true).subscribeOn(Schedulers.boundedElastic()) : Mono.empty().subscribeOn(Schedulers.boundedElastic()));
                })
                .then(Mono.just(true));
    }

    public Mono<Boolean> removeTag(Mono<UserTag> body) {
        return Mono.zip(getAuthUser().subscribeOn(Schedulers.boundedElastic()), body.subscribeOn(Schedulers.boundedElastic()))
                .flatMapMany(objects -> {
                    String tag = objects.getT2().getTag();
                    List<String> followings = new ArrayList<String>();
                    boolean isTag = tag.charAt(0) == '#';
                    User.Builder builder = User.from(objects.getT1());
                    if (isTag) {
                        objects.getT1().getTags().forEach(_tag -> {
                            if (!_tag.equals(tag.substring(1))) followings.add(_tag);
                        });
                        builder.withTags(followings);
                    } else {
                        objects.getT1().getUsers().forEach(_tag -> {
                            if (!_tag.equals(tag.substring(1))) followings.add(_tag);
                        });
                        builder.withUsers(followings);
                    }
                    User user = builder.build();
                    return Mono.zip(this.userService.save(user).subscribeOn(Schedulers.boundedElastic()), !isTag ?
                            manageFollowers(objects.getT2(), false).subscribeOn(Schedulers.boundedElastic()) : Mono.empty().subscribeOn(Schedulers.boundedElastic()));
                })
                    .then(Mono.just(true));
    }

    private Mono<User> manageFollowers(UserTag userTag, boolean adding) {
        List<String> followers = new ArrayList<String>();
        String follower = userTag.getId();
        return this.userService.findById(userTag.getTag().substring(1))
                .flatMap(user -> {
                    User.Builder builder = User.from(user);
                    followers.addAll(user.getFollowers());
                    if (adding) {
                        if (!followers.contains(follower)) {
                            followers.add(follower);
                        }
                    } else {
                        followers.remove(follower);
                    }
                    builder.withFollowers(followers);
                    return this.userService.save(builder.build());
                }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<User> getUserByStart(String id, String random) {
        return this.userService.findById(id).flatMap(user -> {
            kafkaSender.send(NewsStreams.AUTHS_OUT, UserPayload.of(user.getId().toHexString()).withTags(user.getTags()).withUsers(user.getUsers()).withIndex(0).withRandom(random).withIsAdmin(user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))).withOffers(user.getOffers()).build(), user.getId().toHexString().getBytes(), false).subscribeOn(Schedulers.boundedElastic());
            return Mono.just(user);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<User> getUserByEmail(String mid, String email, String random) {
        if (!mid.contains("@") && !mid.contains("#")) {
            String id = mid;
            if (mid.length() == 12) id = (new ObjectId(mid.getBytes())).toHexString();
            return this.userService.findById(id).flatMap(user -> {
                        if (!email.equals("a") && user.getBlocked() != null && user.getBlocked().size() > 0 && user.getBlocked().contains(email)) {
                            return Mono.just(User.of(user.getId()).build());
                        } else {
                            if (!email.equals("a"))
                                kafkaSender.send(NewsStreams.AUTHS_OUT, UserPayload.of(user.getId().toHexString()).withTags(user.getTags()).withUsers(user.getUsers()).withIndex(0).withRandom(random).withIsAdmin(user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))).withOffers(user.getOffers()).build(), user.getId().toHexString().getBytes(), false).subscribeOn(Schedulers.boundedElastic());
                            return Mono.just(user);
                        }
                    }
            );
        } else if (!mid.contains("@")) { //username
            return this.userService.findByUsernameId(mid.substring(1)).flatMap(user -> {
                        if (!email.equals("a") && user.getBlocked().contains((new ObjectId(email.getBytes())).toHexString()))
                            return Mono.just(User.of().build());
                        else return Mono.just(user);
//                                    kafkaSender.send(NewsStreams.AUTHS_OUT, UserPayload.of(user.getId().toHexString()).withTags(user.getTags()).withUsers(user.getUsers()).withIndex(0).withRandom(random).build(), user.getId().toHexString().getBytes(), false).subscribeOn(Schedulers.boundedElastic())
//                                    .map(vv -> user);
                    }
            ).subscribeOn(Schedulers.boundedElastic());
        } else return this.userService.findById(mid.substring(1)).subscribeOn(Schedulers.boundedElastic()); //email
    }

    public Flux<User> getUsers(List<String> ids) {
        return this.userService.findAllByIds(ids);
    }
    public Mono<Boolean> saveUser(Mono<User> body) {
        return body.flatMap(this.userService::save).subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(true));
    }

    public Mono<Boolean> deleteUser(Mono<User> body) {
        return body.flatMap(user -> this.userService.save(User.from(user).withEnabled(false).build())).subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(true));
    }

    public Mono<Boolean> blockUser(Mono<UserTag> body) {
        return body.flatMap(userTag -> this.userService.findById(userTag.getId()).map(user -> Tuples.of(user, userTag))).flatMap(tuple -> {
            List<String> list = new ArrayList<String>(tuple.getT1().getBlocked());
            if (tuple.getT2().getTag().equals("0")) {
                list.add(tuple.getT2().getEmail());
            } else {
                list.remove(tuple.getT2().getEmail());
            }
            return this.userService.save(User.from(tuple.getT1()).withBlocked(list).build()).subscribeOn(Schedulers.boundedElastic());
        }).then(Mono.just(true));
    }

    public Mono<Boolean> clearNews(Mono<String> id) {
        return id.flatMap(this.newsService::findById)
                .flatMap(news -> {
                    this.kafkaSender.send(NewsStreams.NEWS_OUT, News.from(news).withClean(true).withArrest(false).build(), news.getId().toHexString().getBytes(), true).subscribeOn(Schedulers.boundedElastic());
                    return Mono.just(true);
                });
    }

    public Flux<NewsPayload> getNewsByTopicsIn(String id) {
        return this.newsService.findAllByTopicsPart(id);
    }

    public Flux<User> getUsersByUsernameIn(String id) {
        return this.userService.findAllByUsernamePart(id);
    }

    public Mono<Boolean> saveNewsComments(Mono<CommentsFeed> body) {
        return body.flatMap(commentsFeed -> this.kafkaSender.send("comments-topics", commentsFeed, commentsFeed.getNewsId().getBytes(), false).subscribeOn(Schedulers.boundedElastic()));
    }

//    public Mono<Boolean> partitionMoney(Mono<String> para) {
//        // String key = String.valueOf(new Date().getTime());
//        return this.getAuthUser().flatMap(user -> para.map(pa -> Tuples.of(user, pa)))
//                .flatMap(tuple -> this.kafkaSender.send(partitioncomTopics, new PartitionCommand(tuple.getT1().getId().toHexString(), tuple.getT2()), tuple.getT1().getId().toByteArray(), false).subscribeOn(Schedulers.boundedElastic()));
//    }
//    public Mono<Boolean> handlePayments(Mono<List<String>> pa) {
//        String key = String.valueOf(new Date().getTime());
//        return pa.flatMap(val -> this.kafkaSender.send(paymentcomTopics, new PaymentCommand(key, val), key.getBytes(), false).subscribeOn(Schedulers.boundedElastic()));
//    }
}
