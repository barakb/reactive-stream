package com.totango.reactivestream

import org.openjdk.jmh.infra.Blackhole
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription


class PerfSubscriber(private val bh: Blackhole) : Subscriber<Any?> {
    override fun onSubscribe(subscription: Subscription) {
        subscription.request(Long.MAX_VALUE)
    }

    override fun onNext(item: Any?) {
        bh.consume(item)
    }

    override fun onError(throwable: Throwable) {
        bh.consume(throwable)
    }

    override fun onComplete() {
        bh.consume(true)
    }
}
