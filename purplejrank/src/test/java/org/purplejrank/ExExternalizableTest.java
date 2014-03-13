package org.purplejrank;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;

public class ExExternalizableTest {
	public static class A implements Externalizable {
		private static final long serialVersionUID = 0;
		
		public String foo = "bar";
		
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(foo);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			foo = (String) in.readObject();
		}
	}
	
	public static class B implements Serializable {
		private static final long serialVersionUID = 0;
		
		public String foo = "qux";
		
		protected void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			foo = (String) in.readObject();
		}
	}
	
	@Test
	public void testExExternalizable() throws Exception {
		MappingClassLoader cl = new MappingClassLoader(ExExternalizableTest.class.getClassLoader());
		cl.map(A.class, B.class);
		
		B b = (B) Util.cycle(new A(), cl);
		
		Assert.assertEquals("bar", b.foo);
	}
}
