/*
 * The MIT License
 *
 * Copyright 2017 Fernando van Loenhout.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.ferrybig.javacoding.teamspeakconnector.util;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author Fernando van Loenhout
 */
public class FutureUtilTest {

	private static final NioEventLoopGroup group = new NioEventLoopGroup();
	private EventLoop executor;

	@Before
	public void before() {
		executor = group.next();
	}

	@AfterClass
	public static void afterClass() {
		group.shutdownGracefully();
	}

	@Test
	public void chainFutureDirectSuccessTest() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Future<String> chainFuture = FutureUtil.chainFuture(executor.newPromise(), executor.newSucceededFuture("bar"), s -> s + "foo");
			assertTrue(chainFuture.toString(), chainFuture.isSuccess());
			assertEquals("barfoo", chainFuture.getNow());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void chainFutureDelayedSuccessTest() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Promise<String> delayed = executor.newPromise();
			Future<String> chainFuture = FutureUtil.chainFuture(executor.newPromise(), delayed, s -> s + "foo");
			assertFalse(chainFuture.isDone());
			delayed.setSuccess("baz");
			assertTrue(chainFuture.toString(), chainFuture.isSuccess());
			assertEquals("bazfoo", chainFuture.getNow());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void chainFutureDelayedCancelledTest() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Promise<String> delayed = executor.newPromise();
			Future<String> chainFuture = FutureUtil.chainFuture(executor.newPromise(), delayed, s -> s + "foo");
			assertFalse(chainFuture.isDone());
			chainFuture.cancel(true);
			assertTrue(chainFuture.isDone());
			assertTrue(delayed.isDone());
			assertTrue(delayed.isCancelled());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void chainFutureDelayedUncancelableCancelledTest() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Promise<String> delayed = executor.newPromise();
			delayed.setUncancellable();
			Function<String, String> mapping = mock(Function.class);
			Future<String> chainFuture = FutureUtil.chainFuture(executor.newPromise(), delayed, mapping);
			assertFalse(chainFuture.isDone());

			chainFuture.cancel(true);
			assertTrue(chainFuture.isDone());
			assertFalse(delayed.isDone());
			assertFalse(delayed.isCancelled());

			delayed.setSuccess("ok");
			assertTrue(delayed.isDone());
			assertFalse(delayed.isCancelled());
			verify(mapping, times(0)).apply("ok");
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void chainFutureDirectExceptionTest() throws Throwable {
		Future<?> f = executor.submit(() -> {
			IOException ex = new IOException();
			Future<String> chainFuture = FutureUtil.chainFuture(executor.newPromise(), executor.newFailedFuture(ex), s -> s + "foo");
			assertFalse(chainFuture.toString(), chainFuture.isSuccess());
			assertEquals(ex, chainFuture.cause());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void chainFutureDelayedExceptionTest() throws Throwable {
		Future<?> f = executor.submit(() -> {
			IOException ex = new IOException();
			Promise<String> delayed = executor.newPromise();
			Future<String> chainFuture = FutureUtil.chainFuture(executor.newPromise(), delayed, s -> s + "foo");
			assertFalse(chainFuture.isDone());
			delayed.setFailure(ex);
			assertFalse(chainFuture.toString(), chainFuture.isSuccess());
			assertEquals(ex, chainFuture.cause());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void generateListenerSingleMethodSuccessString() throws Throwable {
		Future<?> f = executor.submit(() -> {
			BiConsumer<Object, Throwable> mock = mock(BiConsumer.class);
			executor.newSucceededFuture("bar").addListener(FutureUtil.generateListener(mock));
			verify(mock).accept("bar", null);
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void generateListenerSingleMethodSuccessInteger() throws Throwable {
		Future<?> f = executor.submit(() -> {
			BiConsumer<Object, Throwable> mock = mock(BiConsumer.class);
			executor.newSucceededFuture(22).addListener(FutureUtil.generateListener(mock));
			verify(mock).accept(22, null);
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test(expected = AssertionError.class)
	public void generateListenerSingleMethodSuccessIntegerFalsePositive() throws Throwable {
		Future<?> f = executor.submit(() -> {
			BiConsumer<Object, Throwable> mock = mock(BiConsumer.class);
			executor.newSucceededFuture(22).addListener(FutureUtil.generateListener(mock));
			verify(mock).accept(23, null);
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void generateListenerSingleMethodFailed() throws Throwable {
		Future<?> f = executor.submit(() -> {
			BiConsumer<Object, Throwable> mock = mock(BiConsumer.class);
			Throwable dummyException = new IOException();
			executor.newFailedFuture(dummyException).addListener(FutureUtil.generateListener(mock));
			verify(mock).accept(null, dummyException);
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void generateListenerDoubleMethodSuccessString() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Consumer<Object> success = mock(Consumer.class);
			Consumer<Throwable> error = mock(Consumer.class);
			executor.newSucceededFuture("bar").addListener(FutureUtil.generateListener(success, error));
			verify(success).accept("bar");
			verify(error, times(0)).accept(any());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void generateListenerDoubleMethodSuccessInteger() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Consumer<Object> success = mock(Consumer.class);
			Consumer<Throwable> error = mock(Consumer.class);
			executor.newSucceededFuture(42).addListener(FutureUtil.generateListener(success, error));
			verify(success).accept(42);
			verify(error, times(0)).accept(any());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test(expected = AssertionError.class)
	public void generateListenerDoubleMethodSuccessIntegerFalsePositive() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Consumer<Object> success = mock(Consumer.class);
			Consumer<Throwable> error = mock(Consumer.class);
			executor.newSucceededFuture(42).addListener(FutureUtil.generateListener(success, error));
			verify(success).accept(24);
			verify(error, times(0)).accept(any());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void generateListenerDoubleMethodFailed() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Consumer<Object> success = mock(Consumer.class);
			Consumer<Throwable> error = mock(Consumer.class);
			Throwable dummyException = new IOException();
			executor.newFailedFuture(dummyException).addListener(FutureUtil.generateListener(success, error));
			verify(success, times(0)).accept(any());
			verify(error).accept(same(dummyException));
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void generateListenerExecutorSingleMethodSuccessString() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Consumer<Runnable> futureExecutor = mock(Consumer.class);
			doAnswer((Answer<Object>) (InvocationOnMock invocation) -> {
				invocation.getArgumentAt(0, Runnable.class).run();
				return null;
			}).when(futureExecutor).accept(any());;
			BiConsumer<Object, Throwable> mock = mock(BiConsumer.class);
			executor.newSucceededFuture("bar").addListener(FutureUtil.generateListener(futureExecutor, mock));
			verify(mock).accept("bar", null);
			verify(futureExecutor).accept(any());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void generateListenerExecutorSingleMethodSuccessInteger() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Consumer<Runnable> futureExecutor = mock(Consumer.class);
			doAnswer((Answer<Object>) (InvocationOnMock invocation) -> {
				invocation.getArgumentAt(0, Runnable.class).run();
				return null;
			}).when(futureExecutor).accept(any());;
			BiConsumer<Object, Throwable> mock = mock(BiConsumer.class);
			executor.newSucceededFuture(22).addListener(FutureUtil.generateListener(futureExecutor, mock));
			verify(mock).accept(22, null);
			verify(futureExecutor).accept(any());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test(expected = AssertionError.class)
	public void generateListenerExecutorSingleMethodSuccessIntegerFalsePositive() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Consumer<Runnable> futureExecutor = mock(Consumer.class);
			doAnswer((Answer<Object>) (InvocationOnMock invocation) -> {
				invocation.getArgumentAt(0, Runnable.class).run();
				return null;
			}).when(futureExecutor).accept(any());;
			BiConsumer<Object, Throwable> mock = mock(BiConsumer.class);
			executor.newSucceededFuture(22).addListener(FutureUtil.generateListener(futureExecutor, mock));
			verify(mock).accept(23, null);
			verify(futureExecutor).accept(any());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void generateListenerExecutorSingleMethodFailed() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Consumer<Runnable> futureExecutor = mock(Consumer.class);
			doAnswer((Answer<Object>) (InvocationOnMock invocation) -> {
				invocation.getArgumentAt(0, Runnable.class).run();
				return null;
			}).when(futureExecutor).accept(any());;
			BiConsumer<Object, Throwable> mock = mock(BiConsumer.class);
			Throwable dummyException = new IOException();
			executor.newFailedFuture(dummyException).addListener(FutureUtil.generateListener(futureExecutor, mock));
			verify(mock).accept(null, dummyException);
			verify(futureExecutor).accept(any());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void generateListenerExecutorDoubleMethodSuccessString() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Consumer<Runnable> futureExecutor = mock(Consumer.class);
			doAnswer((Answer<Object>) (InvocationOnMock invocation) -> {
				invocation.getArgumentAt(0, Runnable.class).run();
				return null;
			}).when(futureExecutor).accept(any());;
			Consumer<Object> success = mock(Consumer.class);
			Consumer<Throwable> error = mock(Consumer.class);
			executor.newSucceededFuture("bar").addListener(FutureUtil.generateListener(futureExecutor, success, error));
			verify(success).accept("bar");
			verify(error, times(0)).accept(any());
			verify(futureExecutor).accept(any());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void generateListenerExecutorDoubleMethodSuccessInteger() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Consumer<Runnable> futureExecutor = mock(Consumer.class);
			doAnswer((Answer<Object>) (InvocationOnMock invocation) -> {
				invocation.getArgumentAt(0, Runnable.class).run();
				return null;
			}).when(futureExecutor).accept(any());;
			Consumer<Object> success = mock(Consumer.class);
			Consumer<Throwable> error = mock(Consumer.class);
			executor.newSucceededFuture(42).addListener(FutureUtil.generateListener(futureExecutor, success, error));
			verify(success).accept(42);
			verify(error, times(0)).accept(any());
			verify(futureExecutor).accept(any());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test(expected = AssertionError.class)
	public void generateListenerExecutorDoubleMethodSuccessIntegerFalsePositive() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Consumer<Runnable> futureExecutor = mock(Consumer.class);
			doAnswer((Answer<Object>) (InvocationOnMock invocation) -> {
				invocation.getArgumentAt(0, Runnable.class).run();
				return null;
			}).when(futureExecutor).accept(any());;
			Consumer<Object> success = mock(Consumer.class);
			Consumer<Throwable> error = mock(Consumer.class);
			executor.newSucceededFuture(42).addListener(FutureUtil.generateListener(futureExecutor, success, error));
			verify(success).accept(24);
			verify(error, times(0)).accept(any());
			verify(futureExecutor).accept(any());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

	@Test
	public void generateListenerExecutorDoubleMethodFailed() throws Throwable {
		Future<?> f = executor.submit(() -> {
			Consumer<Runnable> futureExecutor = mock(Consumer.class);
			doAnswer((Answer<Object>) (InvocationOnMock invocation) -> {
				invocation.getArgumentAt(0, Runnable.class).run();
				return null;
			}).when(futureExecutor).accept(any());
			Consumer<Object> success = mock(Consumer.class);
			Consumer<Throwable> error = mock(Consumer.class);
			Throwable dummyException = new IOException();
			executor.newFailedFuture(dummyException).addListener(FutureUtil.generateListener(futureExecutor, success, error));
			verify(success, times(0)).accept(any());
			verify(error).accept(same(dummyException));
			verify(futureExecutor).accept(any());
		}).await();
		if (f.cause() != null) {
			throw f.cause();
		}
	}

}
