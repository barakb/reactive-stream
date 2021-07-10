package com.totango.reactivestream

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class ArrayPublisher<T>(private val source: Array<T>) : Publisher<T> {

    override fun subscribe(subscriber: Subscriber<in T>) {
        subscriber.onSubscribe(object : Subscription {
            var cancelled = AtomicBoolean(false)
            var index = AtomicInteger(0)
            var requested = AtomicLong(0L)

            override fun request(n: Long) {

                if (n <= 0 && !cancelled.get()) {
                    cancel()
                    subscriber.onError(IllegalArgumentException("request number have to be > 0, given: $n"))
                    return
                }

                val initialRequested = requested.safeGetAndAdd(n)


                if (initialRequested != 0L) {
                    return
                }

                do {
                    var sent = 0L
                    while (sent < requested.get() && index.get() < source.size) {
                        if (cancelled.get()) {
                            return
                        }

                        val next = source[index.get()]
                        if (next == null) {
                            subscriber.onError(NullPointerException())
                            return
                        }
                        subscriber.onNext(next)
                        sent += 1
                        index.getAndIncrement()
                    }

                    if (cancelled.get()) {
                        return
                    }

                    if (index.get() == source.size) {
                        subscriber.onComplete()
                        return
                    }

                } while (requested.addAndGet(-sent) != 0L)
            }

            override fun cancel() {
                cancelled.set(true)
            }
        })

    }
}

