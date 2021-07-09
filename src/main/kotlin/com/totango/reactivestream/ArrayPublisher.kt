package com.totango.reactivestream

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class ArrayPublisher<T>(private val source: Array<T>) : Publisher<T> {

    override fun subscribe(subscriber: Subscriber<in T>) {
        subscriber.onSubscribe(object : Subscription {
            var cancelled = false
            var index = 0
            var requested: Long = 0
            override fun request(n: Long) {

                if (n <= 0 && !cancelled) {
                    cancel()
                    subscriber.onError(IllegalArgumentException("request number have to be > 0, given: $n"))
                    return
                }

                val initialRequested = requested
                requested += n

                if (initialRequested != 0L) {
                    return
                }
                var sent = 0L
                while (sent < requested && index < source.size) {
                    if (cancelled) {
                        return
                    }

                    val next = source[index]
                    if (next == null) {
                        subscriber.onError(NullPointerException())
                        return
                    }
                    subscriber.onNext(next)
                    sent += 1
                    index += 1
                }

                if (cancelled) {
                    return
                }

                if (index == source.size) {
                    subscriber.onComplete()
                    return
                }

                requested -= sent
            }

            override fun cancel() {
                cancelled = true
            }
        })

    }
}