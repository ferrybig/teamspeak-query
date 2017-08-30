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

import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * FutureUtil provides large amount of utility functions for working with
 * futures. On of the most useful utilities of this class are the
 * {@code chainFuture} and {@code chainFutureFlat} methods, these methods allow
 * for easy chaining of multiple Futures in a row.
 *
 * @see FutureUtil#chainFuture(io.netty.util.concurrent.Promise, io.netty.util.concurrent.Future, java.util.function.Function)
 * @see FutureUtil#chainFutureFlat(io.netty.util.concurrent.Promise, io.netty.util.concurrent.Future, java.util.function.Function)
 * @see FutureUtil#generateListener(java.util.function.Consumer, java.util.function.Consumer)
 */
@ParametersAreNonnullByDefault
public class FutureUtil {

	private FutureUtil() {
		assert false;
	}

	@Nonnull
	private static <T, R> Future<R> delegateFutureResult(Future<T> future,
			Promise<R> prom, Function<T, R> map) {
		Objects.requireNonNull(map, "map");
		return delegateFutureResult(future, prom, (s, t) -> {
			if (t != null) {
				throw t;
			}
			return map.apply(s);
		});
	}

	@Nonnull
	private static <T, R> Future<R> delegateFutureResult(Future<T> future,
			Promise<R> prom, BiExFunction<T, Throwable, R> map) {
		Objects.requireNonNull(map, "map");
		Objects.requireNonNull(prom, "prom");
		Objects.requireNonNull(future, "future");
		future.addListener((ignored) -> {
			assert ignored == future;
			try {
				T val = future.isSuccess() ? future.getNow() : null;
				Throwable t = future.cause();
				assert val == null || t == null;
				if (prom.isDone()) {
					ReferenceCountUtil.release(val);
					return;
				}
				R result = map.apply(val, t);
				if (!prom.trySuccess(result)) {
					ReferenceCountUtil.release(result);
				}
			} catch (Throwable e) {
				prom.tryFailure(e);
			}
		});
		prom.addListener((f) -> {
			assert f == prom;
			if (prom.isCancelled() && !future.isDone()) {
				future.cancel(true);
			}
		});
		return prom;
	}

	@Nonnull
	public static <T, R> Future<R> chainFuture(Promise<R> result,
			Future<T> future, Function<T, R> mapping) {
		return delegateFutureResult(future, result, mapping);
	}

	@Nonnull
	public static <T, R> Future<R> chainFutureAdvanced(Promise<R> result,
			Future<T> future, BiExFunction<T, Throwable, R> mapping) {
		return delegateFutureResult(future, result, mapping);
	}

	@Nonnull
	public static <T, R> Future<R> chainFutureFlat(Promise<R> result,
			Future<T> future, Function<T, Future<R>> mapping) {
		return chainFutureFlat(result, future, mapping, (t, i) -> i);
	}

	@Nonnull
	public static <T, I, R> Future<R> chainFutureFlat(Promise<R> result,
			Future<T> future, Function<T, Future<I>> mapping,
			BiFunction<T, I, R> secondary) {
		future.addListener(ignored -> {
			assert ignored == future;
			try {
				if (future.isSuccess()) {
					delegateFutureResult(mapping.apply(future.getNow()), result,
							i -> secondary.apply(future.getNow(), i));
				} else {
					result.tryFailure(future.cause());
				}
			} catch (Throwable e) {
				result.tryFailure(e);
			}
		});
		result.addListener((f) -> {
			assert f == result;
			if (result.isCancelled() && !future.isDone()) {
				future.cancel(true);
			}
		});
		return result;
	}

	@Nonnull
	public static <T> List<T> waitSync(Future<? extends T> future)
			throws InterruptedException, ExecutionException {
		return Collections.singletonList(future.get());
	}

	@Nonnull
	public static <T> List<T> waitSync(Future<? extends T> future1,
			Future<? extends T> future2)
			throws InterruptedException, ExecutionException {
		List<T> result = new ArrayList<>(2);
		result.add(future1.get());
		result.add(future2.get());
		return result;
	}

	@Nonnull
	public static <T> List<T> waitSync(Future<? extends T> future1,
			Future<? extends T> future2, Future<? extends T> future3)
			throws InterruptedException, ExecutionException {
		List<T> result = new ArrayList<>(3);
		result.add(future1.get());
		result.add(future2.get());
		result.add(future3.get());
		return result;
	}

	@Nonnull
	public static <T> List<T> waitSync(Future<? extends T> future1,
			Future<? extends T> future2, Future<? extends T> future3,
			Future<? extends T> future4)
			throws InterruptedException, ExecutionException {
		List<T> result = new ArrayList<>(4);
		result.add(future1.get());
		result.add(future2.get());
		result.add(future3.get());
		result.add(future4.get());
		return result;
	}

	@SafeVarargs
	@Nonnull
	public static <T> List<T> waitSync(Future<? extends T>... list)
			throws InterruptedException, ExecutionException {
		List<T> result = new ArrayList<>(list.length);
		for (Future<? extends T> future : list) {
			result.add(future.get());
		}
		return result;
	}

	@Nonnull
	public static <T> List<T> waitSync(Iterable<Future<? extends T>> iterable)
			throws InterruptedException, ExecutionException {
		List<T> result;
		if (iterable instanceof Collection<?>) {
			result = new ArrayList<>(((Collection<?>) iterable).size());
		} else {
			result = new ArrayList<>();
		}
		for (Future<? extends T> future : iterable) {
			result.add(future.get());
		}
		return result;
	}

	public static <T> GenericFutureListener<? extends Future<T>>
			generateListener(BiConsumer<? super T, Throwable> toCall) {
		return f -> toCall.accept(f.isSuccess() ? f.getNow() : null, f.cause());
	}

	public static <T> GenericFutureListener<? extends Future<T>>
			generateListener(
					Consumer<? super T> success, Consumer<Throwable> error) {
		return f -> {
			if (f.isSuccess()) {
				success.accept(f.getNow());
			} else {
				error.accept(f.cause());
			}
		};
	}

	public static <T> GenericFutureListener<? extends Future<T>>
			generateListener(Consumer<Runnable> executor,
					BiConsumer<? super T, Throwable> toCall) {
		if (executor == null) {
			return generateListener(toCall);
		}
		return f
				-> executor.accept(()
						-> toCall.accept(f.isSuccess() ? f.getNow() : null, f.cause()));
	}

	public static <T> GenericFutureListener<? extends Future<T>>
			generateListener(Consumer<Runnable> executor,
					Consumer<? super T> success, Consumer<Throwable> error) {
		if (executor == null) {
			return generateListener(success, error);
		}
		return f -> executor.accept(() -> {
			if (f.isSuccess()) {
				success.accept(f.getNow());
			} else {
				error.accept(f.cause());
			}
		});
	}

}
