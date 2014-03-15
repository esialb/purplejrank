package org.purplejrank.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple cache for methods
 * @author robin
 *
 */
public class MethodCache {

	private Map<String, Method> declared = new HashMap<String, Method>();
	private Map<String, Method> found = new HashMap<String, Method>();
	
	/**
	 * Get a declared method for a class
	 * @param cls
	 * @param name
	 * @param parameterTypes
	 * @return
	 */
	public Method declared(Class<?> cls, String name, Class<?>... parameterTypes) {
		if(cls == null)
			return null;
		String key = cls.getName() + ":" + name + Arrays.toString(parameterTypes);
		if(declared.containsKey(key))
			return declared.get(key);
		Method m = null;
		try {
			m = cls.getDeclaredMethod(name, parameterTypes);
			m.setAccessible(true);
		} catch(NoSuchMethodException e) {
		}
		declared.put(key, m);
		return m;
	}
	
	/**
	 * Get a declared method for a class or any superclass
	 * @param cls
	 * @param name
	 * @param parameterTypes
	 * @return
	 */
	public Method find(Class<?> cls, String name, Class<?>... parameterTypes) {
		if(cls == null)
			return null;
		String key = cls.getName() + ":" + name + Arrays.toString(parameterTypes);
		if(found.containsKey(key))
			return found.get(key);
		Method m = declared(cls, name, parameterTypes);
		if(m == null)
			m = find(cls.getSuperclass(), name, parameterTypes);
		found.put(key, m);
		return m;
	}
	
}
