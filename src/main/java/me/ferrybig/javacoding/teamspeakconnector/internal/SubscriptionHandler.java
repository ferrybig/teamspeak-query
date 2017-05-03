/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal;

import io.netty.util.concurrent.Future;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 * @param <L>
 */
public class SubscriptionHandler<L> {

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
