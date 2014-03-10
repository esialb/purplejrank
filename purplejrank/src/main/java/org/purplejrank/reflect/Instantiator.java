package org.purplejrank.reflect;

import java.io.IOException;

public interface Instantiator {
	public Object newInstance() throws IOException, ClassNotFoundException;
}
