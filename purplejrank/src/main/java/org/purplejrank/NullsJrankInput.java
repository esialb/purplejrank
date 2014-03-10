package org.purplejrank;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public class NullsJrankInput extends PurpleJrankInput {

	public NullsJrankInput(ReadableByteChannel in) throws IOException {
		super(in);
	}

	@Override
	protected Class<?> resolveClass(String name) throws IOException, ClassNotFoundException {
		try {
			return super.resolveClass(name);
		} catch(ClassNotFoundException e) {
			return null;
		}
	}
	
}
