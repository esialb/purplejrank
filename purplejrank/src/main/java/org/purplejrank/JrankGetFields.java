package org.purplejrank;

import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link GetField} for purple jrank
 * @author robin
 *
 */
public class JrankGetFields extends GetField {

	private Map<String, Object> values = new HashMap<String, Object>();
	
	void put(String name, Object val) {
		values.put(name, val);
	}
	
	@Override
	public ObjectStreamClass getObjectStreamClass() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean defaulted(String name) throws IOException {
		return !values.containsKey(name);
	}

	@Override
	public boolean get(String name, boolean val) throws IOException {
		return (Boolean) get(name, val);
	}

	@Override
	public byte get(String name, byte val) throws IOException {
		return (Byte) get(name, val);
	}

	@Override
	public char get(String name, char val) throws IOException {
		return (Character) get(name, val);
	}

	@Override
	public short get(String name, short val) throws IOException {
		return (Short) get(name, val);
	}

	@Override
	public int get(String name, int val) throws IOException {
		return (Integer) get(name, val);
	}

	@Override
	public long get(String name, long val) throws IOException {
		return (Long) get(name, val);
	}

	@Override
	public float get(String name, float val) throws IOException {
		return (Float) get(name, val);
	}

	@Override
	public double get(String name, double val) throws IOException {
		return (Double) get(name, val);
	}

	@Override
	public Object get(String name, Object val) throws IOException {
		if(!values.containsKey(name))
			return val;
		return values.get(name);
	}

}
