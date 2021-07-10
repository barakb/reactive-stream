package com.totango.reactivestream

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.atomic.AtomicLong


class ArrayPublisher<T>(private val source: Array<T>) : Publisher<T> {

    override fun subscribe(subscriber: Subscriber<in T>) {
        subscriber.onSubscribe(ArrayPublisherSubscription(source, subscriber))
    }

    class ArrayPublisherSubscription<T>(
        private val source: Array<T>,
        private val subscriber: Subscriber<in T>
    ) : Subscription {
        @Volatile
        var cancelled = false
        private var index = 0
        private var requested = AtomicLong(0L)

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

            while (true) {
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
                if (toSend == 0L) {
                    return
                }
            }
        }

        override fun cancel() {
            cancelled = true
        }
    }
}

fun AtomicLong.safeGetAndAdd(n: Long): Long {
    var current: Long
    do {
        current = get()
        if (current == Long.MAX_VALUE) {
            return Long.MAX_VALUE
        }
        var sum = current + n
        if (sum < 0) {
            sum = Long.MAX_VALUE
        }
    } while (!compareAndSet(current, sum))
    return current
}