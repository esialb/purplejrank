package org.purplejrank;

import java.util.HashMap;
import java.util.Map;

public class MappingClassLoader extends ClassLoader {

	private ClassLoader parent;
	private Map<String, Class<?>> map;
	
	public MappingClassLoader(ClassLoader parent) {
		this(parent, new HashMap<String, Class<?>>());
	}
	
	public MappingClassLoader(ClassLoader parent, Map<String, Class<?>> map) {
		super(parent);
		this.parent = parent;
		this.map = map;
	}
	
	public void map(Class<?> from, Class<?> to) {
		map.put(from.getName(), to);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> cls = parent.loadClass(name);
		if(!map.containsKey(name))
			return cls;
		cls = map.get(name);
		if(cls == null)
			throw new ClassNotFoundException(name);
		return cls;
	}
	
}
