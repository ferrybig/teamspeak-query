/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.util;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.util.function.Function;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
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
