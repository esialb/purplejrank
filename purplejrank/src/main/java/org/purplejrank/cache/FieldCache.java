package org.purplejrank.cache;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FieldCache {

	private Map<Class<?>, Map<String, Field>> cache = new HashMap<Class<?>, Map<String,Field>>();
	
	private void put(Field f) {
		if(!cache.containsKey(f.getDeclaringClass()))
			cache.put(f.getDeclaringClass(), new HashMap<String, Field>());
		cache.get(f.getDeclaringClass()).put(f.getName(), f);
	}
	
	public Field get(Class<?> cls, String field) throws NoSuchFieldException {
		if(cache.containsKey(cls) && cache.get(cls).containsKey(field))
			return cache.get(cls).get(field);
		if(!cache.containsKey(cls))
			cache.put(cls, new HashMap<String, Field>());
		Field[] fields = cls.getDeclaredFields();
		Field.setAccessible(fields, true);
		for(Field f : fields)
			put(f);
		Field f = cache.get(cls).get(field);
		if(f == null)
			throw new NoSuchFieldException();
		return f;
	}
	
	public Field[] get(Class<?> cls) {
		if(cache.containsKey(cls))
			return cache.get(cls).values().toArray(new Field[0]);
		cache.put(cls, new HashMap<String, Field>());
		Field[] fields = cls.getDeclaredFields();
		Field.setAccessible(fields, true);
		for(Field f : fields)
			put(f);
		return fields;
	}

}
