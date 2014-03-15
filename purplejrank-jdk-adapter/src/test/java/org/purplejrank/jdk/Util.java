package org.purplejrank.jdk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

public class Util {
	public static JdkStream stream(Object... objs) throws IOException {
		return new JdkStream(input(objs));
	}
	
	public static InputStream input(Object... objs) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		
		for(Object o : objs)
			out.writeObject(o);
		out.close();

		return new ByteArrayInputStream(bout.toByteArray());
	}
	
	public static JdkJrankInputStream jjis(Object... objs) throws IOException {
		return new JdkJrankInputStream(input(objs));
	}
	
	private Util() {}
}
