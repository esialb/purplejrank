package org.purplejrank;

import java.io.Externalizable;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

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
	
	public JrankClass(Class<?> cls) {
		this.type = cls;
		
		if(Proxy.isProxyClass(cls)) {
			proxy = true;
			Class<?>[] ifcs = cls.getInterfaces();
			proxyInterfaceNames = new String[ifcs.length];
			for(int i = 0; i < ifcs.length; i++)
				proxyInterfaceNames[i] = ifcs[i].getName();
		} else {
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
			name = cls.getName();
			fields = cls.getDeclaredFields();
			Field.setAccessible(fields, true);
			fieldNames = new String[fields.length];
			fieldTypes = new String[fields.length];
			for(int i = 0; i < fields.length; i++) {
				fieldNames[i] = fields[i].getName();
				fieldTypes[i] = className(fields[i].getType());
			}
		}
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
	void setParent(JrankClass parent) {
		this.parent = parent;
	}

	public Class<?> getType() {
		return type;
	}
	
	public Field[] getFields() {
		return fields;
	}
}
