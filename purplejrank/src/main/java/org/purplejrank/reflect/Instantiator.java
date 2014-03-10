package org.purplejrank.reflect;

import java.io.IOException;

/**
 * Object used to instantiate other objects
 * @author robin
 *
 */
public interface Instantiator {
	public Object newInstance() throws IOException, ClassNotFoundException;
}
