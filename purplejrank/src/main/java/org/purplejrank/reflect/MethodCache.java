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
	
	public Method get(Class<?> cls, String name, Class<?>... parameterTypes) {
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
	
	public Method find(Class<?> cls, String name, Class<?>... parameterTypes) {
		if(cls == null)
			return null;
		String key = cls.getName() + ":" + name + Arrays.toString(parameterTypes);
		if(found.containsKey(key))
			return found.get(key);
		Method m = get(cls, name, parameterTypes);
		if(m == null)
			m = find(cls.getSuperclass(), name, parameterTypes);
		found.put(key, m);
		return m;
	}
	
}
