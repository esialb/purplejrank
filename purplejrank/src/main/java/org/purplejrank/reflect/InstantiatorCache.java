package org.purplejrank.reflect;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.purplejrank.JrankConstants;

public class InstantiatorCache {
	private Map<Class<?>, Instantiator> cache = new HashMap<Class<?>, Instantiator>();
	
	public Instantiator get(Class<?> cls, byte flags) throws NoSuchMethodException {
		Instantiator i = null;
		if(cache.containsKey(cls)) {
			i = cache.get(cls);
			if(i == null)
				throw new NoSuchMethodException();
			return i;
		}
		if((flags & JrankConstants.SC_WRITE_EXTERNAL) != 0)
			i = new ConstructorInstantiator(cls);
		else if(Proxy.isProxyClass(cls))
			i = new ProxyInstantiator(cls);
		else if(Serializable.class.isAssignableFrom(cls))
			i = new SerializableInstantiator(cls);
		cache.put(cls, i);
		if(i == null)
			throw new NoSuchMethodException();
		return i;
	}

	
}
