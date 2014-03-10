package org.purplejrank;

import java.io.ObjectStreamException;

public class JrankStreamException extends ObjectStreamException {
	private static final long serialVersionUID = 0;

	public JrankStreamException() {
	}

	public JrankStreamException(String classname) {
		super(classname);
	}

	public JrankStreamException(Throwable cause) {
		initCause(cause);
	}
	
	public JrankStreamException(String message, Throwable cause) {
		this(message);
		initCause(cause);
	}
	
}
