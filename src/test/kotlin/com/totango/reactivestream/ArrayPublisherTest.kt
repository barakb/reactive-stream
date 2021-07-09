package com.totango.reactivestream

import org.assertj.core.api.Assertions.assertThat
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.reactivestreams.tck.PublisherVerification
import org.reactivestreams.tck.TestEnvironment
import org.testng.annotations.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.LongStream

@Test
internal class ArrayPublisherTest : PublisherVerification<Long>(TestEnvironment()){

    @Test
    fun everyMethodInSubscriberShouldBeExecutedInParticularOrder() {
        val countDownLatch = CountDownLatch(1)
        val observedSignals: MutableList<String> = mutableListOf()
        val publisher = ArrayPublisher(generate(5))

        publisher.subscribe(object : Subscriber<Long> {
            override fun onSubscribe(s: Subscription) {
                observedSignals.add("onSubscribe()")
                s.request(10)
            }

            override fun onNext(next: Long) {
                observedSignals.add("onNext($next)")
            }

            override fun onError(t: Throwable) {
                observedSignals.add("onError()")
            }

            override fun onComplete() {
                observedSignals.add("onComplete()")
                countDownLatch.countDown()
            }
        })
        assertThat(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
            .isTrue
        assertThat(observedSignals).containsExactly(
            "onSubscribe()",
            "onNext(0)",
            "onNext(1)",
            "onNext(2)",
            "onNext(3)",
            "onNext(4)",
            "onComplete()"
        )

    }

    @Test
    fun mustSupportBackpressureControl() {
        val countDownLatch = CountDownLatch(1)
        val collected: MutableList<Long> = mutableListOf()
        val toRequest = 5L
        val array = generate(toRequest)
        val publisher = ArrayPublisher(array)
        val subscription = AtomicReference<Subscription>()

        publisher.subscribe(object : Subscriber<Long> {
            override fun onSubscribe(s: Subscription) {
                subscription.set(s)
            }

            override fun onNext(next: Long) {
                collected.add(next)
            }

            override fun onError(t: Throwable) {
            }

            override fun onComplete() {
                countDownLatch.countDown()
            }
        })

        assertThat(collected).isEmpty()

        subscription.get().request(1)
        assertThat(collected).containsExactly(0L)

        subscription.get().request(1)
        assertThat(collected).containsExactly(0L, 1L)

        subscription.get().request(1)
        assertThat(collected).containsExactly(0L, 1L, 2L)

        subscription.get().request(1)
        assertThat(collected).containsExactly(0L, 1L, 2L, 3L)

        subscription.get().request(1)
        assertThat(collected).containsExactly(0L, 1L, 2L, 3L, 4L)

        subscription.get().request(20)
        assertThat(collected).containsExactly(0L, 1L, 2L, 3L, 4L)

        assertThat(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
            .isTrue
    }

    @Test
    fun mustReturnErrorOnNull() {
        val countDownLatch = CountDownLatch(1)
        val collected: MutableList<Long?> = mutableListOf()
        val array: Array<Long?> = arrayOf(null)
        val errorReference = AtomicReference<Throwable>()
        val publisher = ArrayPublisher(array)
        val subscription = AtomicReference<Subscription>()

        publisher.subscribe(object : Subscriber<Long?> {
            override fun onSubscribe(s: Subscription) {
                subscription.set(s)
            }

            override fun onNext(next: Long?) {
                collected.add(next)
            }

            override fun onError(t: Throwable) {
                errorReference.set(t)
                countDownLatch.countDown()
            }

            override fun onComplete() {
            }
        })

        assertThat(collected).isEmpty()
        assertThat(errorReference.get()).isNull()

        subscription.get().request(1)
        assertThat(collected).isEmpty()
        assertThat(errorReference.get()).isInstanceOf(NullPointerException::class.java)

        assertThat(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
            .isTrue
    }

    @Test
    fun shouldNotDieOnStackOverflow() {
        val countDownLatch = CountDownLatch(1)
        val publisher = ArrayPublisher(generate(1000))

        publisher.subscribe(object : Subscriber<Long> {
            var s: Subscription? = null
            override fun onSubscribe(subscription: Subscription) {
                s = subscription
                s!!.request(1)
            }

            override fun onNext(next: Long) {
                s!!.request(1)
            }

            override fun onError(t: Throwable) {

            }

            override fun onComplete() {
                countDownLatch.countDown()
            }
        })
        assertThat(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
            .isTrue
    }

    @Test
    fun shouldCancel() {
        val countDownLatch = CountDownLatch(1)
        val publisher = ArrayPublisher(generate(2))
        val sent = AtomicBoolean(false)
        publisher.subscribe(object : Subscriber<Long> {

            override fun onSubscribe(subscription: Subscription) {
                subscription.cancel()
                subscription.request(1)
            }

            override fun onNext(next: Long) {
                sent.set(true)
            }

            override fun onError(t: Throwable) {

            }

            override fun onComplete() {
                countDownLatch.countDown()
            }
        })
        assertThat(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
            .isFalse
        assertThat(sent.get())
            .isFalse

    }


    companion object {
        fun generate(num: Long): Array<Long> = LongStream.range(0, if (num >= Int.MAX_VALUE) 1000000 else num)
            .boxed().toArray {
                arrayOfNulls<Long>(it)
            }
    }

    override fun createPublisher(elements: Long): Publisher<Long> {
        return ArrayPublisher(generate(elements))
    }

    override fun createFailedPublisher(): Publisher<Long>? {
        return null
    }
}