package org.purplejrank.jdk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Util {
	public static JdkStream stream(Object... objs) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		
		for(Object o : objs)
			out.writeObject(o);
		out.close();

		return new JdkStream(new ByteArrayInputStream(bout.toByteArray()));
	}
	
	private Util() {}
}
