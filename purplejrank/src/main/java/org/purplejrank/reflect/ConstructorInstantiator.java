package org.purplejrank.reflect;

import java.io.IOException;
import java.lang.reflect.Constructor;

public class ConstructorInstantiator implements Instantiator {
	private Constructor<?> ctor;
	
	public ConstructorInstantiator(Class<?> cls) throws NoSuchMethodException {
		this.ctor = cls.getConstructor();
	}

	@Override
	public Object newInstance() throws IOException, ClassNotFoundException {
		try {
			return ctor.newInstance();
		} catch(Exception ex) {
			throw new ClassNotFoundException(ex.toString());
		}
	}
}
