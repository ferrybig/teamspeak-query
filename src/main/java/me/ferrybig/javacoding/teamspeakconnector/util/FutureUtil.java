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

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.util.function.Function;

public class FutureUtil {

	private FutureUtil() {
		assert false;
	}

	private static <T, R> Future<R> delegateFutureResult(Future<T> future, Promise<R> prom, Function<T, R> map) {
		future.addListener(ignored -> {
			assert ignored == future;
			try {
				if (future.isSuccess()) {
					prom.setSuccess(map.apply(future.getNow()));
				} else {
					prom.setFailure(future.cause());
				}
			} catch (Throwable e) {
				prom.setFailure(e);
			}
		});
		return prom;
	}

	public static <T, R> Future<R> chainFuture(Promise<R> result, Future<T> future, Function<T, R> mapping) {
		return delegateFutureResult(future, result, mapping);
	}

	public static <T, R> Future<R> chainFutureFlat(Promise<R> result, Future<T> future, Function<T, Future<R>> mapping) {
		return chainFutureFlat(result, future, mapping, Function.identity());
	}

	public static <T, I, R> Future<R> chainFutureFlat(Promise<R> result, Future<T> future, Function<T, Future<I>> mapping, Function<I, R> secondary) {
		future.addListener(ignored -> {
			assert ignored == future;
			try {
				if (future.isSuccess()) {
					delegateFutureResult(mapping.apply(future.getNow()), result, secondary);
				} else {
					result.setFailure(future.cause());
				}
			} catch (Throwable e) {
				result.setFailure(e);
			}
		});
		return result;
	}
}
