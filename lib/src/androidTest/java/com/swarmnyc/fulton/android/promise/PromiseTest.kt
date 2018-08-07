package com.swarmnyc.fulton.android.promise

import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.swarmnyc.fulton.android.util.await
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicLong

@RunWith(AndroidJUnit4::class)
class PromiseTest {
    companion object {
        val TAG = PromiseTest::class.java.simpleName!!
    }

    @Test
    fun promiseTest() {
        // test basic promise

        val latch = CountDownLatch(1)
        var result: String? = null
        val executor: PromiseLambdaExecutor<String> = { resolve, _ ->
            resolve("Abc")
        }

        val p = Promise(executor = executor)

        p.then {
            result = it
            latch.countDown()
        }

        latch.await()

        assertEquals("Abc", result)
    }

    @Test
    fun thenTest() {
        // test resolve and then

        val latch = CountDownLatch(1)
        var result: String? = null
        Promise.resolve("Abc").then {
            result = it
            latch.countDown()
        }

        latch.await()

        assertEquals("Abc", result)
    }

    @Test
    fun then2Test() {
        // test two then, p1 -> p2 -> p3

        val latch = CountDownLatch(1)
        var result = 0
        val executor: PromiseLambdaExecutor<String> = { resolve, _ ->
            resolve("Abc")
        }

        val p = Promise(executor = executor)

        p.then {
            100
        }.then {
            result = it
            latch.countDown()
        }

        latch.await()

        assertEquals(100, result)
    }

    @Test
    fun then3Test() {
        // test two then, p1 -> p2, p1 -> p3

        val latch = CountDownLatch(2)

        val executor: PromiseLambdaExecutor<String> = { resolve, _ ->
            resolve("Abc")
        }

        val p = Promise(executor = executor)

        p.then {
            latch.countDown()
        }

        p.then {
            latch.countDown()
        }

        latch.await()
    }

    @Test
    fun then4Test() {
        // test three then, p1->p2->p4, p1->p3

        val latch = CountDownLatch(3)

        val executor: PromiseLambdaExecutor<String> = { resolve, _ ->
            resolve("Abc")
        }

        val p = Promise(executor = executor)

        val p2 = p.then {
            latch.countDown()
            "Cba"
        }

        p.then {
            latch.countDown()
        }

        var result = ""

        p2.then {
            result = it
            latch.countDown()
        }

        latch.await()

        assertEquals("Cba", result)
    }


    @Test
    fun catchTest() {
        // test catch error

        val latch = CountDownLatch(1)
        val error = Throwable("Test")
        var result: Throwable? = null
        val executor: PromiseLambdaExecutor<String> = { _, reject ->
            reject(error)
        }

        Promise(executor = executor).then {
            fail()
            latch.countDown()
        }.catch {
            result = it
            latch.countDown()
        }

        latch.await()

        assertEquals(error, result)
    }

    @Test
    fun catchFailOnExecTest() {
        // test catch fail on executor

        val latch = CountDownLatch(1)
        val error = Throwable("Test")
        var result: Throwable? = null
        val executor: PromiseLambdaExecutor<String> = { _, _ ->
            throw error
        }

        Promise(executor = executor).then {
            fail()
        }.catch {
            result = it
            latch.countDown()
        }

        latch.await()

        assertEquals(error, result)
    }

    @Test
    fun catchFailOnThenTest() {
        // test catch fail on then
        
        val latch = CountDownLatch(2)
        val error = Throwable("Test")
        var result: Throwable? = null
        val executor: PromiseLambdaExecutor<String> = { resolve, _ ->
            resolve("Abc")
        }

        Promise(executor = executor).then {
            latch.countDown()
            throw error
        }.catch {
            result = it
            latch.countDown()
        }

        latch.await()

        assertEquals(error, result)
    }

    @Test
    fun catchFailOnThen2Test() {
        // test catch fail by p1->p2->catch, p1->p3->then
        
        val latch = CountDownLatch(3)
        val error = Throwable("Test")
        var result: Throwable? = null
        val executor: PromiseLambdaExecutor<String> = { resolve, _ ->
            resolve("Abc")
        }

        val p = Promise(executor = executor)

        p.then {
            latch.countDown()
            throw error
        }.catch {
            result = it
            latch.countDown()
        }

        var result2 = ""
        p.then {
            result2 = it
            latch.countDown()
        }

        latch.await()

        assertEquals(error, result)
        assertEquals("Abc", result2)
    }

    @Test
    fun catchRejectTest() {
        // test two catch
        
        val latch = CountDownLatch(2)
        val error = Throwable("Test")
        var result1: Throwable? = null
        var result2: Throwable? = null

        Promise.reject(error).then {
            fail()
        }.catch {
            result1 = it
            latch.countDown()
        }.catch {
            result2 = it
            latch.countDown()
        }

        latch.await()

        assertEquals(error, result1)
        assertEquals(error, result2)
    }

    @Test
    fun catchReject2Test() {
        // test two catch, and first catch throw new error
        
        val latch = CountDownLatch(1)
        val error1 = Throwable("Test1")
        val error2 = Throwable("Test2")
        var result: Throwable? = null

        Promise.reject(error1).catch {
            throw error2
        }.catch {
            result = it
            latch.countDown()
        }

        latch.await()

        assertEquals(error2, result)
    }

    @Test
    fun uncaughtTest() {
        // test to throw uncaught error on root promise
        
        val latch = CountDownLatch(1)
        val error = Throwable("Test")
        var result: Throwable? = null

        val old = Promise.uncaughtError
        Promise.uncaughtError = {
            result = it
            latch.countDown()
        }

        val executor: PromiseLambdaExecutor<String> = { _, _ ->
            throw error
        }

        Promise(executor = executor)

        latch.await()

        assertEquals(error, result)

        Promise.uncaughtError = old
    }

    @Test
    fun uncaught2Test() {
        // test to throw uncaught error on child promise's catch 
        
        val latch = CountDownLatch(1)
        val error1 = Throwable("Test1")
        val error2 = Throwable("Test2")
        var result: Throwable? = null

        val old = Promise.uncaughtError
        Promise.uncaughtError = {
            result = it
            latch.countDown()
        }

        val executor: PromiseLambdaExecutor<String> = { _, _ ->
            throw error1
        }

        Promise(executor = executor).then {
            fail()
        }.catch {
            throw error1
        }.catch {
            throw error1
        }.catch {
            throw error1
        }.then {
            fail()
        }.catch {
            throw error2
        }

        latch.await()

        assertEquals(error2, result)

        Promise.uncaughtError = old
    }

    @Test
    fun uncaught3Test() {
        // test to throw uncaught error on child promise 

        val latch = CountDownLatch(1)
        val error = Throwable("Test1")
        var result: Throwable? = null

        Promise.uncaughtError = {
            result = it
            latch.countDown()
        }

        Promise.resolve("Abc").then {
            123
        }.then {
            throw error
        }.then {
            false
        }

        latch.await()

        assertEquals(error, result)
    }


    @Test
    fun chainTest() {
        // test promise chain
        
        val latch = CountDownLatch(1)
        var result = 0
        val executor: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(100)
            resolve("Abc")
        }

        val p = Promise(executor = executor)

        p.thenChain {
            Promise.resolve(123)
        }.then {
            result = it
            latch.countDown()
        }

        latch.await()

        assertEquals(123, result)
    }

    @Test
    fun treadTest() {
        // test multi-threads, there will create 3 thread, 1. main thread, 2. promise body thread 3. result(success, fail, always) thread
        
        val mainThread = Thread.currentThread().id
        val executorThread = AtomicLong(0)
        val then1Thread = AtomicLong(0)
        val then2Thread = AtomicLong(0)

        Log.d(TAG, "Main Thread Id : ${Thread.currentThread().id}")

        val executor: PromiseLambdaExecutor<Unit> = { resolve, _ ->
            Thread.sleep(100)
            Log.d(TAG, "executor Thread Id : ${Thread.currentThread().id}")
            executorThread.set(Thread.currentThread().id)
            resolve(Unit)
        }

        val promise = Promise(executor = executor).then {
            Log.d(TAG, "Then 1 Thread Id : ${Thread.currentThread().id}")
            then1Thread.set(Thread.currentThread().id)
        }.thenUi {
            Log.d(TAG, "Then 2 Thread Id : ${Thread.currentThread().id}")
            then2Thread.set(Thread.currentThread().id)
        }

        val executor2Thread = AtomicLong(0)
        val then3Thread = AtomicLong(0)
        val then4Thread = AtomicLong(0)

        Log.d(TAG, "Main Thread Id : ${Thread.currentThread().id}")

        val executor2: PromiseLambdaExecutor<Unit> = { resolve, _ ->
            Log.d(TAG, "executor Thread Id : ${Thread.currentThread().id}")
            executor2Thread.set(Thread.currentThread().id)
            resolve(Unit)
        }

        val promise2 = Promise(executor = executor2).then {
            Log.d(TAG, "Then 1 Thread Id : ${Thread.currentThread().id}")
            then3Thread.set(Thread.currentThread().id)
        }.thenUi {
            Log.d(TAG, "Then 2 Thread Id : ${Thread.currentThread().id}")
            then4Thread.set(Thread.currentThread().id)
        }

        promise.await()
        promise2.await()

        assertNotEquals(mainThread, executorThread.get())
        assertEquals(executorThread.get(), then1Thread.get()) // then use the same thread
        assertNotEquals(then1Thread.get(), then2Thread.get()) // thenUi use ui thread

        assertNotEquals(mainThread, executor2Thread.get())
        // each promise use its own thread
        assertNotEquals(executorThread.get(), executor2Thread.get())
    }

    @Test
    fun allThenTest() {
        // test promise.all success
        
        val latch = CountDownLatch(1)
        val executor1: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(200)
            resolve("Abc")
        }

        val executor2: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(100)
            resolve("Efg")
        }

        var result: Array<Any>? = null

        val p = Promise.all(Promise(executor = executor1), Promise(executor = executor2)).then {
            result = it
            latch.countDown()
        }

        latch.await()

        assertArrayEquals(arrayOf("Abc", "Efg"), result)
    }

    @Test
    fun allFailTest() {
        // test promise.all with error

        val latch = CountDownLatch(1)
        val error = Throwable("Test")
        var result: Throwable? = null

        val executor1: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(200)
            throw error
        }

        val executor2: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(100)
            resolve("Efg")
        }

        Promise.all(Promise(executor = executor1), Promise(executor = executor2)).then {
            fail()
        }.catch {
            result = it
            latch.countDown()
        }

        latch.await()

        assertEquals(error, result)
    }

    @Test
    fun raceTest() {
        // test promise.race

        val latch = CountDownLatch(1)
        var result = ""

        val executor1: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(500)
            resolve("Abc")
        }

        val executor2: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(100)
            resolve("Efg")
        }

        Promise.race(Promise(executor = executor1), Promise(executor = executor2)).then {
            result = it as String
            latch.countDown()
        }

        latch.await()

        assertEquals("Efg", result)
    }

    @Test
    fun raceFailTest() {
        // test promise.race with error

        val latch = CountDownLatch(1)
        var result = ""

        val executor1: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(200)
            throw Throwable("Test")
        }

        val executor2: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(100)
            resolve("Efg")
        }

        Promise.race(Promise(executor = executor1), Promise(executor = executor2)).then {
            result = it as String
            latch.countDown()
        }

        latch.await()

        assertEquals("Efg", result)
    }

    @Test
    fun cancelTest() {
        // test cancel promise without error

        val latch = CountDownLatch(1)

        val executor1: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(100000)
            resolve("abc")
        }

        val p = Promise(executor1)

        p.then {
            fail()
        }.catch {
            fail()
        }

        Promise.defaultOptions.executor.submit {
            p.cancel()

            latch.countDown()
        }

        latch.await()

        assertEquals(PromiseState.Canceled.ordinal, p.state.get())
    }

    @Test
    fun cancel2Test() {
        // test cancel on root promise and cause error and catch it.

        val latch = CountDownLatch(1)
        var result: Throwable? = null
        val executor1: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(100000)
            resolve("abc")
        }

        val p = Promise(executor1)

        p.then {
            fail()
        }.catch {
            result = it
            latch.countDown()
        }

        Promise.defaultOptions.executor.submit {
            p.cancel(true)
        }

        latch.await()

        assertTrue(result is InterruptedException)

    }

    @Test
    fun cancel3Test() {
        // test cancel on child promise and cause error and catch it.

        val latch = CountDownLatch(1)
        var result: Throwable? = null
        val executor1: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(100000)
            resolve("abc")
        }

        val p = Promise(executor1).then {
            fail()
        }.catch {
            result = it
            latch.countDown()
        }

        Promise.defaultOptions.executor.submit {
            p.cancel(true)
        }

        latch.await()

        assertTrue(result is InterruptedException)
    }

    @Test
    fun timeoutTest() {
        // test timeout on root promise and cause error and catch it.

        val latch = CountDownLatch(1)
        var result: Throwable? = null
        val executor1: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(100000)
            resolve("abc")
        }

        Promise(executor1).timeout(1000, true).then {
            fail()
        }.catch {
            result = it
            latch.countDown()
        }

        latch.await()

        assertTrue(result is InterruptedException)
    }

    @Test
    fun timeout2Test() {
        // test timeout on child promise and cause error and catch it.

        val latch = CountDownLatch(1)
        var result: Throwable? = null
        val executor1: PromiseLambdaExecutor<String> = { resolve, _ ->
            Thread.sleep(100000)
            resolve("abc")
        }

        Promise(executor1).then {
            fail()
        }.catch {
            result = it
            latch.countDown()
        }.timeout(1000, true)

        latch.await()

        assertTrue(result is InterruptedException)
    }

    @Test
    fun timeout3Test() {
        // test timeout, but the promise is finished.

        val latch = CountDownLatch(1)
        var result: String? = null
        val executor1: PromiseLambdaExecutor<String> = { resolve, _ ->
            resolve("abc")
        }

        Promise(executor1).then {
            result = it
            latch.countDown()
        }.catch {
            fail()

        }.timeout(1000, true)

        latch.await()

        assertEquals("abc", result)
    }

    @Test
    fun timeout4Test() {
        // test timeout, but the promise is finished by error.

        val latch = CountDownLatch(1)
        val error = Throwable("Test")
        var result: Throwable? = null

        val executor1: PromiseLambdaExecutor<String> = { resolve, _ ->
            throw error
        }

        Promise(executor1).then {
            fail()
        }.catch {
            result = it
            latch.countDown()
        }.timeout(1000, true)

        latch.await()

        assertEquals(error, result)
    }
}