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
package me.ferrybig.javacoding.teamspeakconnector;

/**
 * Exception thrown if there is a problem connecting to the Teamspeak server.
 */
public class ConnectException extends TeamspeakException {

	private static final long serialVersionUID = -4471004975006446997L;
	private final String message;

	/**
	 * Constructs an {@code ConnectException} with {@code null} as its error
	 * detail message.
	 */
	public ConnectException() {
		super();
		message = null;
	}

	/**
	 * Constructs an {@code ConnectException} with the specified detail message.
	 *
	 * @param message The detail message (which is saved for later retrieval by
	 * the {@link #getMessage()} method)
	 */
	public ConnectException(String message) {
		this(message, message);
	}

	/**
	 * Constructs an {@code ConnectException} with the specified detail message.
	 *
	 * @param message The detail message (which is saved for later retrieval by
	 * the {@link #toString()} method)
	 * @param fullMessage The detail message (which is saved for later retrieval
	 * by the {@link #getMessage()} method)
	 */
	public ConnectException(String message, String fullMessage) {
		super(fullMessage);
		this.message = message;
	}

	/**
	 * Constructs an {@code ConnectException} with the specified detail message
	 * and cause.
	 *
	 * <p>
	 * Note that the detail message associated with {@code cause} is
	 * <i>not</i> automatically incorporated into this exception's detail
	 * message.
	 *
	 * @param message The detail message (which is saved for later retrieval by
	 * the {@link #getMessage()} method)
	 *
	 * @param cause The cause (which is saved for later retrieval by the
	 * {@link #getCause()} method). (A null value is permitted, and indicates
	 * that the cause is nonexistent or unknown.)
	 *
	 */
	public ConnectException(String message, Throwable cause) {
		this(message, message, cause);
	}

	/**
	 * Constructs an {@code ConnectException} with the specified detail message
	 * and cause.
	 *
	 * <p>
	 * Note that the detail message associated with {@code cause} is
	 * <i>not</i> automatically incorporated into this exception's detail
	 * message.
	 *
	 * @param message The detail message (which is saved for later retrieval by
	 * the {@link #toString()} method)
	 * @param fullMessage The detail message (which is saved for later retrieval
	 * by the {@link #getMessage()} method)
	 *
	 * @param cause The cause (which is saved for later retrieval by the
	 * {@link #getCause()} method). (A null value is permitted, and indicates
	 * that the cause is nonexistent or unknown.)
	 *
	 */
	public ConnectException(String message, String fullMessage, Throwable cause) {
		super(fullMessage, cause);
		this.message = message;
	}

	/**
	 * Constructs an {@code ConnectException} with the specified cause and a
	 * detail message of {@code (cause==null ? null : cause.toString())} (which
	 * typically contains the class and detail message of {@code cause}). This
	 * constructor is useful for IO exceptions that are little more than
	 * wrappers for other throwables.
	 *
	 * @param cause The cause (which is saved for later retrieval by the
	 * {@link #getCause()} method). (A null value is permitted, and indicates
	 * that the cause is nonexistent or unknown.)
	 *
	 */
	public ConnectException(Throwable cause) {
		super(cause);
		this.message = null;
	}

	public String toString() {
        String s = getClass().getName();
        return (message != null) ? (s + ": " + message) : s;
    }


}
