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
package me.ferrybig.javacoding.teamspeakconnector.entities;

import io.netty.util.concurrent.Future;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;

public class UnresolvedFile {

	private final TeamspeakConnection connection;
	private final UnresolvedChannel channel;
	private final String name;

	public UnresolvedFile(TeamspeakConnection connection, UnresolvedChannel channel, String name) {
		this.connection = connection;
		this.channel = channel;
		this.name = name;
	}

	public Future<File> resolve() {
		return this.connection.getFileByChannelAndName(channel, name);
	}

	public UnresolvedChannel getChannel() {
		return channel;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "UnresolvedFile{" + "connection=" + connection + ", channel=" + channel + ", name=" + name + '}';
	}

//	public ProgressiveFuture<?> uploadFile(java.io.File file) throws FileNotFoundException {
//		return uploadFile(file, false);
//	}
//
//	public ProgressiveFuture<?> uploadFile(java.io.File file, boolean reportStatus) throws FileNotFoundException {
//		return uploadFile(new FileInputStream(file), file.length(), reportStatus);
//	}
//
//	public ProgressiveFuture<?> uploadFile(@WillClose InputStream file, long size) {
//		return uploadFile(file, size, false);
//	}
//
//	public ProgressiveFuture<?> uploadFile(@WillClose InputStream file, long size, boolean reportStatus) {
//		ProgressivePromise<?> progressive = connection.io().getChannel().eventLoop().newProgressivePromise();
//		connection.io().sendPacket(new ComplexRequestBuilder("").build()).addListener((Future<ComplexResponse> f) -> {
//			file.close();
//			if(f.isSuccess() && !progressive.isCancelled()) {
//				progressive.setFailure(new UnsupportedOperationException());
//			} else {
//				progressive.setFailure(f.cause());
//			}
//		});
//		return progressive;
//	}

}
