/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.event;

import io.netty.util.concurrent.Future;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public interface Handler<L> {

	Future<?> addHandler(L handler);

	Future<?> removeHandler(L handler);
	
}
