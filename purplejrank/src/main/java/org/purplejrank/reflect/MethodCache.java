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

	private Map<Class<?>, Map<String, Method>> cache = new HashMap<Class<?>, Map<String,Method>>();
	
	private String cname(Method m) {
		return cname(m.getName(), m.getParameterTypes());
	}
	
	private String cname(String name, Class<?>[] parameterTypes) {
		return name + Arrays.toString(parameterTypes);
	}
	
	private void put(Method m) {
		if(!cache.containsKey(m.getDeclaringClass()))
			cache.put(m.getDeclaringClass(), new HashMap<String, Method>());
		cache.get(m.getDeclaringClass()).put(cname(m), m);
	}

	public Method get(Class<?> cls, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
		String cn = cname(name, parameterTypes);
		if(cache.containsKey(cls) && cache.get(cls).containsKey(cn)) {
			Method m = cache.get(cls).get(cn);
			if(m == null)
				throw new NoSuchMethodException(cn);
			return m;
		}
		if(!cache.containsKey(cls))
			cache.put(cls, new HashMap<String, Method>());
		if(!cache.get(cls).containsKey(cn)) {
			Method m;
			try {
				m = cls.getDeclaredMethod(name, parameterTypes);
			} catch(NoSuchMethodException e) {
				cache.get(cls).put(cn, null);
				throw e;
			}
			m.setAccessible(true);
			put(m);
			return m;
		}
		Method m = cache.get(cls).get(cn);
		if(m == null)
			throw new NoSuchMethodException(cn);
		return m;
	}
	
}
