package org.purplejrank;

import java.io.IOException;

public interface Jrankternalizable extends Jrankable {
	public void writeExternal(PurpleJrankOutput out) throws IOException;
	public void readExternal(PurpleJrankInput in) throws IOException, ClassNotFoundException;
}
