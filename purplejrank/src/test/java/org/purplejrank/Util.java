package org.purplejrank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Util {
	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new PurpleJrankOutput(bout);
		out.writeObject(obj);
		out.close();
		return bout.toByteArray();
	}
	
	public static Object cycle(Object obj) throws IOException, ClassNotFoundException {
		return cycle(obj, Util.class.getClassLoader());
	}
	
	public static Object cycle(Object obj, ClassLoader cl) throws IOException, ClassNotFoundException {
		return deserialize(serialize(obj), cl);
	}
	
	public static Object deserialize(byte[] buf) throws IOException, ClassNotFoundException {
		return deserialize(buf, Util.class.getClassLoader());
	}
	
	public static Object deserialize(byte[] buf, ClassLoader cl) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bin = new ByteArrayInputStream(buf);
		ObjectInputStream in = new PurpleJrankInput(bin, cl);
		Object obj = in.readObject();
		in.close();
		return obj;
	}
	
	private Util() {}
}
