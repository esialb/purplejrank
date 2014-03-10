package org.purplejrank.mods;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.channels.ReadableByteChannel;

import org.purplejrank.PurpleJrankInput;

/**
 * Implementation of {@link ObjectInputStream} that doesn't barf if a class
 * is missing.  Just substitutes null for the instance and goes along its
 * way deserializing.
 * @author robin
 *
 */
public class NullsJrankInput extends PurpleJrankInput {

	public NullsJrankInput(ReadableByteChannel in) throws IOException {
		super(in);
	}

	public NullsJrankInput(ReadableByteChannel in, ClassLoader cl) throws IOException {
		super(in, cl);
	}

	public NullsJrankInput(InputStream in, ClassLoader cl) throws IOException {
		super(in, cl);
	}

	public NullsJrankInput(InputStream in) throws IOException {
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
