/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.util.concurrent.Future;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 * @param <T>
 */
public interface Resolvable<T> {
	public default Future<T> resolve() {
		return this.forceResolve();
	}
	
	public Future<T> forceResolve();
	
	public boolean isResolved();
}
