package org.purplejrank.mods;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.purplejrank.Util;
import org.purplejrank.mods.CollectionsJrankInput;

public class CollectionsJrankInputTest {
	public static class Missing implements Serializable {
		private static final long serialVersionUID = 0;
		
		@SuppressWarnings("unused")
		private int i = 1;
		
		private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();
			out.writeInt(2);
		}
		
	}
	
	private static class MissingMissingClassLoader extends ClassLoader {
		public MissingMissingClassLoader() {
			super(MissingMissingClassLoader.class.getClassLoader());
		}
		
		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			Class<?> cls = super.loadClass(name);
			if(cls == Missing.class)
				throw new ClassNotFoundException();
			return cls;
		}
	}

	private static Object cycle(Object obj) throws IOException, ClassNotFoundException {
		byte[] buf = Util.serialize(obj);
		ObjectInputStream in = new CollectionsJrankInput(
				new ByteArrayInputStream(buf), 
				new MissingMissingClassLoader());
		obj = in.readObject();
		in.close();
		return obj;
	}
	
	@Test
	public void testCollections() throws Exception {
		Assert.assertTrue(cycle(new Missing()) instanceof Map<?, ?>);
		Assert.assertTrue(cycle(new Missing[4]) instanceof List<?>);
	}
}
