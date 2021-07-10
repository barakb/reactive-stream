package com.totango.reactivestream

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.atomic.AtomicLong

class ArrayPublisher<T>(private val source: Array<T>) : Publisher<T> {

    override fun subscribe(subscriber: Subscriber<in T>) {
        subscriber.onSubscribe(object : Subscription {
            @Volatile
            var cancelled = false
            var index = 0
            var requested = AtomicLong(0L)

            override fun request(n: Long) {

                if (n <= 0 && !cancelled) {
                    cancel()
                    subscriber.onError(IllegalArgumentException("request number have to be > 0, given: $n"))
                    return
                }

                val initialRequested = requested.safeGetAndAdd(n)

                if (initialRequested != 0L) {
                    return
                }
                var toSend = n

                while(true){
                    var sent = 0L
                    while (sent < toSend && index < source.size) {
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

                    toSend = requested.addAndGet(-sent)
                    if(toSend == 0L){
                        return
                    }
                }
            }

            override fun cancel() {
                cancelled = true
            }
        })



    }
}

