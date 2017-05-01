/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import java.io.IOException;

/**
 *
 * @author Fernando
 */
public class TeamspeakException extends IOException {

	private static final long serialVersionUID = -4471004975006446997L;

	/**
     * Constructs an {@code TeamspeakException} with {@code null}
     * as its error detail message.
     */
    public TeamspeakException() {
        super();
    }

    /**
     * Constructs an {@code TeamspeakException} with the specified detail message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method)
     */
    public TeamspeakException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code TeamspeakException} with the specified detail message
     * and cause.
     *
     * <p> Note that the detail message associated with {@code cause} is
     * <i>not</i> automatically incorporated into this exception's detail
     * message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method)
     *
     * @param cause
     *        The cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A null value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
     *
     */
    public TeamspeakException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@code TeamspeakException} with the specified cause and a
     * detail message of {@code (cause==null ? null : cause.toString())}
     * (which typically contains the class and detail message of {@code cause}).
     * This constructor is useful for IO exceptions that are little more
     * than wrappers for other throwables.
     *
     * @param cause
     *        The cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A null value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
     *
     */
    public TeamspeakException(Throwable cause) {
        super(cause);
    }
}
