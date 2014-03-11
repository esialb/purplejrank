package org.purplejrank;

import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.junit.Test;

public class BlockModeTest {
	public static class A implements Externalizable {
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(Color.blue);
			out.writeBoolean(true);
		}
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			in.readObject();
			in.readBoolean();
		}
	}
	
	@Test
	public void testBlockBoundaries() throws Exception {
		Util.cycle(new A());
	}
}
