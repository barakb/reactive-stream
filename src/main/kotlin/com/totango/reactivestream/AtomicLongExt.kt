package com.totango.reactivestream

import java.util.concurrent.atomic.AtomicLong

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