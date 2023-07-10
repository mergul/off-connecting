package com.busra.connecting.service;

import com.busra.connecting.config.streams.KStreamConf;
import com.busra.connecting.model.Offer;
import com.busra.connecting.model.OfferPayload;
import com.google.common.util.concurrent.*;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
@Service
public class OfferService {
    private static final String OFFERS_STORES = "connect-offers-stores";

    final ListeningExecutorService pool = MoreExecutors.listeningDecorator(
            Executors.newFixedThreadPool(8)
    );
    private final StreamsBuilderFactoryBean factoryBean;
    private final Sender kafkaSender;

    public OfferService(StreamsBuilderFactoryBean factoryBean, Sender kafkaSender) {
        this.factoryBean = factoryBean;
        this.kafkaSender = kafkaSender;
    }
    public Mono<Boolean> save(Offer offer) {
        //return findAll().flatMap(x->this.kafkaSender.send(NewsStreams.OFFERS_OUT, offer, offer.getId().toHexString().getBytes(),true).subscribeOn(Schedulers.boundedElastic()));
        return this.kafkaSender.send(NewsStreams.OFFERS_OUT, offer, offer.getId().toHexString().getBytes(),true).subscribeOn(Schedulers.boundedElastic());
    }
    public Mono<Boolean> completeOffer(Offer offer){
        return this.kafkaSender.send(NewsStreams.OFFERS_OUT, Offer.from(offer).withEndDate(new Date()).withActive(false).build(), offer.getId().toHexString().getBytes(),true).subscribeOn(Schedulers.boundedElastic());
    }
    public Mono<Offer> findById(String offerId) {
//        logger.info("Number of sub topologies => {}", this.factoryBean.getTopology().describe());
        final ListenableFuture<ReadOnlyKeyValueStore<byte[], Offer>> offerFuture = future(OFFERS_STORES);
        Mono<ReadOnlyKeyValueStore<byte[], Offer>> offerStore = Mono.fromFuture(NewsService.toCompletableFuture(offerFuture));
        return offerStore.flatMap(store -> Mono.fromCallable(()->store.get(offerId.getBytes())).subscribeOn(Schedulers.boundedElastic())).onErrorReturn(Offer.of().build());
    }
    public Flux<OfferPayload> findAllByIds(List<String> ids) {
        final ListenableFuture<ReadOnlyKeyValueStore<byte[], Offer>> offersFuture = future(OFFERS_STORES);
        Mono<ReadOnlyKeyValueStore<byte[], Offer>> offersStore = Mono.fromFuture(NewsService.toCompletableFuture(offersFuture));
        return offersStore.flatMapMany(store -> Flux.fromIterable(ids).publishOn(Schedulers.boundedElastic())
                .map(sid -> extractOfferPayload(store.get(sid.getBytes()))));
    }
    public Mono<Boolean> findAll() {
        final ListenableFuture<ReadOnlyKeyValueStore<byte[], Offer>> offersFuture = future(OFFERS_STORES);
        Mono<ReadOnlyKeyValueStore<byte[], Offer>> offersStore = Mono.fromFuture(NewsService.toCompletableFuture(offersFuture));
        return offersStore.map(store -> store.all()).map(offerIter -> {
            while (offerIter.hasNext()){
                KeyValue<byte[], Offer> next = offerIter.next();
                System.out.println("count for " + Arrays.toString(next.key) + ": " + next.value.toString());
            }
            offerIter.close();
            return true;
        });
    }
    public OfferPayload extractOfferPayload(Offer newOffer) {
        return OfferPayload.of(newOffer.getId())
                .withOwnerId(newOffer.getOwnerId())
                .withNewsId(newOffer.getNewsId())
                .withNewsOwnerId(newOffer.getNewsOwnerId())
                .withTopic(newOffer.getTopic())
                .withThumb(newOffer.getMediaReviews().get(0).getFile_name())
                .withPrice(newOffer.getPrice())
                .withTags(newOffer.getTags())
                .withActive(newOffer.getActive())
                .withEndDate(newOffer.getEndDate())
                .withStartDate(newOffer.getStartDate())
                .withCount(0L)
                .build();
    }
    public <K, V> ListenableFuture<ReadOnlyKeyValueStore<K, V>> future(final String storeName) {
        return pool.submit(() -> NewsService.waitUntilStoreIsQueryable(storeName, QueryableStoreTypes.keyValueStore(), this.factoryBean.getKafkaStreams()));
    }

}
