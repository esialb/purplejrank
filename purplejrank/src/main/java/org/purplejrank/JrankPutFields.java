package org.purplejrank;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream.PutField;
import java.util.HashMap;
import java.util.Map;

public class JrankPutFields extends PutField {
	
	private Map<String, Object> values = new HashMap<String, Object>();
	
	public Object get(String name) {
		return values.get(name);
	}

	@Override
	public void put(String name, boolean val) {
		values.put(name, val);
	}

	@Override
	public void put(String name, byte val) {
		values.put(name, val);
	}

	@Override
	public void put(String name, char val) {
		values.put(name, val);
	}

	@Override
	public void put(String name, short val) {
		values.put(name, val);
	}

	@Override
	public void put(String name, int val) {
		values.put(name, val);
	}

	@Override
	public void put(String name, long val) {
		values.put(name, val);
	}

	@Override
	public void put(String name, float val) {
		values.put(name, val);
	}

	@Override
	public void put(String name, double val) {
		values.put(name, val);
	}

	@Override
	public void put(String name, Object val) {
		values.put(name, val);
	}

	@Override
	public void write(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
