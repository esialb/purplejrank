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

	private Map<String, Method> cache = new HashMap<String, Method>();
	
	public Method get(Class<?> cls, String name, Class<?>... parameterTypes) {
		String key = cls.getName() + ":" + name + Arrays.toString(parameterTypes);
		if(cache.containsKey(key))
			return cache.get(key);
		Method m = null;
		try {
			m = cls.getDeclaredMethod(name, parameterTypes);
			m.setAccessible(true);
		} catch(NoSuchMethodException e) {
		}
		cache.put(key, m);
		return m;
	}
	
}
