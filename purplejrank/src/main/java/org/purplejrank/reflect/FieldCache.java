package org.purplejrank.reflect;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple cache for class fields
 * @author robin
 *
 */
public class FieldCache {

	private Map<String, Field> declared = new HashMap<String, Field>();
	private Map<String, Field[]> perClass = new HashMap<String, Field[]>();
	
	/**
	 * Get a declared field for a class
	 * @param cls
	 * @param name
	 * @return
	 */
	public Field declared(Class<?> cls, String name) {
		if(cls == null)
			return null;
		String key = cls.getName() + ":" + name;
		if(declared.containsKey(key))
			return declared.get(key);
		Field f = null;
		try {
			f = cls.getDeclaredField(name);
			f.setAccessible(true);
		} catch(NoSuchFieldException e) {
		}
		declared.put(key, f);
		return f;
	}

	/**
	 * Get all the declared fields for a class
	 * @param cls
	 * @return
	 */
	public Field[] declared(Class<?> cls) {
		if(cls == null)
			return null;
		String key = cls.getName();
		if(perClass.containsKey(key))
			return perClass.get(key);
		Field[] fs = null;
		fs = cls.getDeclaredFields();
		Field.setAccessible(fs, true);
		perClass.put(key, fs);
		for(Field f : fs)
			declared.put(key + ":" + f.getName(), f);
		return fs;
	}
	
}
