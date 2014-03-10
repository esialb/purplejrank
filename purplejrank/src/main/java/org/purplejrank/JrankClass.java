package org.purplejrank;

import java.io.Externalizable;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.purplejrank.reflect.FieldCache;

/**
 * Representation of a class on the object stream.  Similar to {@link ObjectStreamClass}.
 * @author robin
 *
 */
public class JrankClass {
	protected boolean proxy;
	protected String[] proxyInterfaceNames;
	protected byte flags;
	protected String name;
	protected long serialVersion;
	protected String[] fieldNames;
	protected String[] fieldTypes;
	protected Class<?>[] fieldClasses;
	protected JrankClass parent;
	
	protected Class<?> type;
	protected Field[] fields;
	
	JrankClass() {}
	
	public JrankClass(Class<?> cls, FieldCache fieldCache) {
		this.type = cls;
		
		if(cls.isPrimitive()) {
			name = className(cls);
		} else if(Proxy.isProxyClass(cls)) {
			proxy = true;
			Class<?>[] ifcs = cls.getInterfaces();
			proxyInterfaceNames = new String[ifcs.length];
			for(int i = 0; i < ifcs.length; i++)
				proxyInterfaceNames[i] = ifcs[i].getName();
			setFieldFields(cls, fieldCache);
		} else if(cls.isArray()) {
			name = className(cls);
			serialVersion = ObjectStreamClass.lookupAny(cls).getSerialVersionUID();
			setFieldFields(cls, fieldCache);
		} else {
			name = "L" + cls.getName() + ";";
			serialVersion = ObjectStreamClass.lookupAny(cls).getSerialVersionUID();
			setFieldFields(cls, fieldCache);
		}
	}
	
	protected void setFieldFields(Class<?> cls, FieldCache fieldCache) {
		flags = 0;
		if(Enum.class.isAssignableFrom(cls))
			flags = JrankConstants.SC_WRITE_ENUM;
		else if(Externalizable.class.isAssignableFrom(cls))
			flags = JrankConstants.SC_WRITE_EXTERNAL;
		else {
			try {
				cls.getDeclaredMethod("writeObject", ObjectOutputStream.class);
				flags = JrankConstants.SC_WRITE_OBJECT;
			} catch(NoSuchMethodException e) {
				if(Serializable.class.isAssignableFrom(cls))
					flags = JrankConstants.SC_WRITE_FIELDS;
			}
		}
		fields = fieldCache.get(cls);
		Field spf = null;
		try {
			spf = fieldCache.get(cls, "serialPersistentFields");
			if(!Modifier.isStatic(spf.getModifiers()))
				spf = null;
			if(!spf.getType().equals(ObjectStreamField[].class))
				spf = null;
		} catch(NoSuchFieldException e) {}
		
		if(spf != null) {
			try {
				ObjectStreamField[] pf = (ObjectStreamField[]) spf.get(null);
				fieldNames = new String[pf.length];
				fieldTypes = new String[pf.length];
				fieldClasses = new Class<?>[pf.length];
				fields = new Field[pf.length];
				int fc = 0;
				for(int i = 0; i < pf.length; i++) {
					fieldNames[i] = pf[i].getName();
					fieldTypes[i] = className(pf[i].getType());
					fieldClasses[i] = pf[i].getType();
					try {
						fields[fc] = fieldCache.get(cls, pf[i].getName());
						fc++;
					} catch(NoSuchFieldException e) {}
				}
				fields = Arrays.copyOf(fields, fc);
				return;
			} catch(Exception e) {
			}
		}
		
		fieldNames = new String[fields.length];
		fieldTypes = new String[fields.length];
		fieldClasses = new Class<?>[fields.length];
		int fi = 0;
		for(int i = 0; i < fields.length; i++) {
			if(Modifier.isStatic(fields[i].getModifiers()) || Modifier.isTransient(fields[i].getModifiers()))
				continue;
			fieldNames[fi] = fields[i].getName();
			fieldTypes[fi] = className(fields[i].getType());
			fieldClasses[fi] = fields[i].getType();
			fields[fi] = fields[i];
			fi++;
		}
		fieldNames = Arrays.copyOf(fieldNames, fi);
		fieldTypes = Arrays.copyOf(fieldTypes, fi);
		fields = Arrays.copyOf(fields, fi);
		fieldClasses = Arrays.copyOf(fieldClasses, fi);
	}
	
	@Override
	public String toString() {
		if(proxy)
			return "proxy" + Arrays.toString(proxyInterfaceNames);
		return name + Arrays.toString(fieldNames);
	}
	
	protected String className(Class<?> fc) {
		if(fc == byte.class) return "B";
		else if(fc == char.class) return "C";
		else if(fc == double.class) return "D";
		else if(fc == float.class) return "F";
		else if(fc == int.class) return "I";
		else if(fc == long.class) return "J";
		else if(fc == short.class) return "S";
		else if(fc == boolean.class) return "Z";
		else if(fc.isArray())
			return arrayBrackets(fc);
		else
			return "L" + fc.getName() + ";";
	}
	
	protected String arrayBrackets(Class<?> ac) {
		if(!ac.isArray()) {
			return className(ac);
		}
		return "[" + arrayBrackets(ac.getComponentType());
	}
	
	public boolean isProxy() {
		return proxy;
	}
	public String[] getProxyInterfaceNames() {
		return proxyInterfaceNames;
	}
	public byte getFlags() {
		return flags;
	}
	public String getName() {
		return name;
	}
	public long getSerialVersion() {
		return serialVersion;
	}
	public String[] getFieldNames() {
		return fieldNames;
	}
	public String[] getFieldTypes() {
		return fieldTypes;
	}
	public JrankClass getParent() {
		return parent;
	}
	public Class<?> getType() {
		return type;
	}
	
	public Field[] getFields() {
		return fields;
	}

	public Class<?>[] getFieldClasses() {
		return fieldClasses;
	}
	
	protected void setProxy(boolean proxy) {
		this.proxy = proxy;
	}

	protected void setProxyInterfaceNames(String[] proxyInterfaceNames) {
		this.proxyInterfaceNames = proxyInterfaceNames;
	}

	protected void setFlags(byte flags) {
		this.flags = flags;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setSerialVersion(long serialVersion) {
		this.serialVersion = serialVersion;
	}
	
	protected void setFieldNames(String[] fieldNames) {
		this.fieldNames = fieldNames;
	}

	protected void setFieldTypes(String[] fieldTypes) {
		this.fieldTypes = fieldTypes;
	}

	protected void setParent(JrankClass parent) {
		this.parent = parent;
	}

	protected void setType(Class<?> type) {
		this.type = type;
	}

	protected void setFields(Field[] fields) {
		this.fields = fields;
	}
	
	protected void setFieldClasses(Class<?>[] fieldClasses) {
		this.fieldClasses = fieldClasses;
	}
}
