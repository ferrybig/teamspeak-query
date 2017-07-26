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

public class TeamspeakCommandException extends TeamspeakException {

	private static final long serialVersionUID = 166767223889L;

	private final String command;
	private final int error;
	private final String message;
	private final String extraMessage;

	public TeamspeakCommandException(String command, int error, String message, String extraMessage) {
		super(generateMessage(command, error, message, extraMessage));
		this.command = command;
		this.error = error;
		this.message = message;
		this.extraMessage = extraMessage;
	}

	private static String generateMessage(String command, int error, String message, String extraMessage) {
		if (extraMessage == null) {
			return command + ": " + error + ", " + message;
		} else {
			return command + ": " + error + ", " + message + "; " + extraMessage;
		}
	}

	public String getCommand() {
		return command;
	}

	public int getError() {
		return error;
	}

	public String getOrginalMessage() {
		return message;
	}

	public String getExtraMessage() {
		return extraMessage;
	}

}
