package org.purplejrank.reflect;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ProxyInstantiator implements Instantiator {
	private Constructor<?> ctor;
	
	public ProxyInstantiator(Class<?> cls) throws NoSuchMethodException {
		if(!Proxy.isProxyClass(cls))
			throw new IllegalArgumentException();
		ctor = cls.getDeclaredConstructor(InvocationHandler.class);
	}
	
	@Override
	public Object newInstance() throws IOException, ClassNotFoundException {
		try {
			return ctor.newInstance(new Object[] {null});
		} catch(Exception ex) {
			throw new ClassNotFoundException(ex.toString());
		}
	}

}
