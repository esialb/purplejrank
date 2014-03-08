package org.purplejrank;

import java.io.IOException;

public class JrankingException extends IOException {

	public JrankingException() {
	}

	public JrankingException(String message, Throwable cause) {
		super(message, cause);
	}

	public JrankingException(String message) {
		super(message);
	}

	public JrankingException(Throwable cause) {
		super(cause);
	}

}
