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
		ByteArrayInputStream bin = new ByteArrayInputStream(serialize(obj));
		ObjectInputStream in = new PurpleJrankInput(bin);
		obj = in.readObject();
		in.close();
		return obj;
	}
	
	private Util() {}
}
