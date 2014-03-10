package org.purplejrank;

import java.io.IOException;
import java.io.NotActiveException;
import java.lang.reflect.Field;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CollectionsJrankInput extends PurpleJrankInput {

	public CollectionsJrankInput(ReadableByteChannel in) throws IOException {
		super(in);
	}

	public CollectionsJrankInput(ReadableByteChannel in, ClassLoader cl)
			throws IOException {
		super(in, cl);
	}

	@Override
	protected Object newArray(JrankClass desc, int size) {
		return Arrays.asList(new Object[size]);
	}
	
	@Override
	protected Object newOrdinaryObject(JrankClass desc) {
		Map<String, Object> ret = new TreeMap<String, Object>();
		ret.put("-class", desc.getName());
		return ret;
	}
	
	@Override
	protected Class<?> resolveClass(String name) throws IOException, ClassNotFoundException {
		try {
			return super.resolveClass(name);
		} catch(ClassNotFoundException e) {
			return Map.class;
		}
	}
	
	@Override
	protected void skipOptionalData() throws IOException, ClassNotFoundException {
		setBlockMode(false);
		for(byte b = peek(); b != JrankConstants.WALL; b = peek()) {
			switch(b) {
			case JrankConstants.BLOCK_DATA:
				setBlockMode(true).setBlockMode(false);
				break;
			case JrankConstants.FIELDS:
				defaultReadObject();
				break;
			default:
				readObject0(true);
			}
			setBlockMode(false);
		}
	}
	
	@Override
	public void defaultReadObject() throws IOException, ClassNotFoundException {
		JrankContext ctx = context.peekLast();
		if(ctx == JrankContext.NO_CONTEXT)
			throw new NotActiveException();
		JrankGetFields fields = readFields();
		for(String name : ctx.getType().getFieldNames()) {
			((Map<String, Object>) ctx.getObject()).put(name, fields.get(name, null));
		}
	}
	
	@Override
	protected void checkSerialVersion(JrankClass desc, Class<?> cls) throws ClassNotFoundException {
	}
	
	@Override
	protected void setArrayElement(Object array, int index, Object value) {
		((List<Object>) array).set(index, value);
	}

}
