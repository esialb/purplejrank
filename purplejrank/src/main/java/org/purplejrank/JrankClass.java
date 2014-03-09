package org.purplejrank;

import java.io.Externalizable;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class JrankClass {
	private boolean proxy;
	private String[] proxyInterfaceNames;
	private byte flags;
	private String name;
	private String[] fieldNames;
	private String[] fieldTypes;
	private JrankClass parent;
	
	private Class<?> type;
	private Field[] fields;
	
	JrankClass() {}
	
	public JrankClass(Class<?> cls) {
		this.type = cls;
		
		if(cls.isPrimitive()) {
			name = className(cls);
		} else if(Proxy.isProxyClass(cls)) {
			proxy = true;
			Class<?>[] ifcs = cls.getInterfaces();
			proxyInterfaceNames = new String[ifcs.length];
			for(int i = 0; i < ifcs.length; i++)
				proxyInterfaceNames[i] = ifcs[i].getName();
		} else if(cls.isArray()) {
			name = className(cls);
			setFieldFields(cls);
		} else {
			name = "L" + cls.getName() + ";";
			setFieldFields(cls);
		}
	}
	
	private void setFieldFields(Class<?> cls) {
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
		fields = cls.getDeclaredFields();
		Field.setAccessible(fields, true);
		fieldNames = new String[fields.length];
		fieldTypes = new String[fields.length];
		int fi = 0;
		for(int i = 0; i < fields.length; i++) {
			if(Modifier.isStatic(fields[i].getModifiers()) || Modifier.isTransient(fields[i].getModifiers()))
				continue;
			fieldNames[fi] = fields[i].getName();
			fieldTypes[fi] = className(fields[i].getType());
			fields[fi] = fields[i];
			fi++;
		}
		fieldNames = Arrays.copyOf(fieldNames, fi);
		fieldTypes = Arrays.copyOf(fieldTypes, fi);
		fields = Arrays.copyOf(fields, fi);
	}
	
	@Override
	public String toString() {
		if(proxy)
			return "proxy" + Arrays.toString(proxyInterfaceNames);
		return name + Arrays.toString(fieldNames);
	}
	
	private String className(Class<?> fc) {
		if(fc == byte.class) return "B";
		else if(fc == char.class) return "C";
		else if(fc == double.class) return "D";
		else if(fc == float.class) return "F";
		else if(fc == int.class) return "I";
		else if(fc == long.class) return "J";
		else if(fc == short.class) return "S";
		else if(fc == boolean.class) return "Z";
		else if(fc.isArray())
			return arrayBrackets(fc) + ";";
		else
			return "L" + fc.getName() + ";";
	}
	
	private String arrayBrackets(Class<?> ac) {
		if(!ac.isArray())
			return ac.getName();
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

	void setProxy(boolean proxy) {
		this.proxy = proxy;
	}

	void setProxyInterfaceNames(String[] proxyInterfaceNames) {
		this.proxyInterfaceNames = proxyInterfaceNames;
	}

	void setFlags(byte flags) {
		this.flags = flags;
	}

	void setName(String name) {
		this.name = name;
	}

	void setFieldNames(String[] fieldNames) {
		this.fieldNames = fieldNames;
	}

	void setFieldTypes(String[] fieldTypes) {
		this.fieldTypes = fieldTypes;
	}

	void setParent(JrankClass parent) {
		this.parent = parent;
	}

	void setType(Class<?> type) {
		this.type = type;
	}

	void setFields(Field[] fields) {
		this.fields = fields;
	}
}
