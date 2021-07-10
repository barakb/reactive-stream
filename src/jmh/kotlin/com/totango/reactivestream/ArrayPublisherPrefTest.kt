package com.totango.reactivestream

import com.totango.reactivestream.ArrayPublisher
import com.totango.reactivestream.PerfSubscriber
import com.totango.reactivestream.UnoptimizedArrayPublisher
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import org.reactivestreams.Subscriber
import java.util.concurrent.TimeUnit


@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
open class ArrayPublisherPrefTest {
    var times: Int = 1000_000

    var unoptimizedArrayPublisher: UnoptimizedArrayPublisher<Int>? = null
    var arrayPublisher: ArrayPublisher<Int>? = null

    @Setup
    fun setup(){
        val intArray = Array(times) { 777 }
        unoptimizedArrayPublisher = UnoptimizedArrayPublisher(intArray)
        arrayPublisher = ArrayPublisher(intArray)
    }

    @Benchmark
    fun publisherPerformance(bh: Blackhole): Subscriber<Any?> {
        val lo = PerfSubscriber(bh)
        arrayPublisher!!.subscribe(lo)
        return lo
    }

    @Benchmark
    fun publisherUnoptimizedPerformance(bh: Blackhole): Subscriber<Any?> {
        val lo = PerfSubscriber(bh)
        unoptimizedArrayPublisher!!.subscribe(lo)
        return lo
    }
}