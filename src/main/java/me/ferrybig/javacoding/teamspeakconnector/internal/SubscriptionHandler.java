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
package me.ferrybig.javacoding.teamspeakconnector.internal;

import io.netty.util.concurrent.Future;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.event.Handler;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;

public class SubscriptionHandler<L> implements Handler<L> {

	private final ComplexRequest subscribe;
	private final ComplexRequest unsubscribe;
	private final List<L> listeners = new CopyOnWriteArrayList<>();
	private final TeamspeakConnection con;
	private Future<?> lastFuture;

	public SubscriptionHandler(TeamspeakConnection con, ComplexRequest subscribe, ComplexRequest unsubscribe) {
		this.subscribe = subscribe;
		this.unsubscribe = unsubscribe;
		this.con = con;
	}

	public <T> void callAll(BiConsumer<? super L, T> consumer, T type) {
		listeners.forEach(r -> consumer.accept(r, type));
	}

	public Future<?> clear() {
		synchronized (this) {
			boolean isEmpty = listeners.isEmpty();
			if (isEmpty) {
				return lastFuture;
			}
			listeners.clear();
			return lastFuture = con.io().sendPacket(unsubscribe);
		}
	}

	@Override
	public Future<?> addHandler(L handler) {
		Future<?> f;
		synchronized (this) {
			boolean wasEmpty = listeners.isEmpty();
			listeners.add(handler);
			if (wasEmpty) {
				f = con.io().sendPacket(subscribe);
				lastFuture = f;
			} else {
				f = lastFuture;
			}
		}
		f.addListener(future -> {
			assert future == f;
			if (!future.isSuccess()) {
				removeHandler(handler);
			}
		});
		return f;

	}

	@Override
	public Future<?> removeHandler(L handler) {
		synchronized (this) {
			if (!listeners.remove(handler)) {
				return lastFuture;
			}
			if (listeners.isEmpty()) {
				lastFuture = con.io().sendPacket(unsubscribe);
			}
			return lastFuture;
		}
	}

}
