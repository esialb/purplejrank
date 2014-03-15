package org.purplejrank;

import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream.PutField;

/**
 * A serialization or deserialization context.
 * @author robin
 *
 */
public class JrankContext {
	/**
	 * Marker for no stream context
	 */
	public static final JrankContext NO_CONTEXT = new JrankContext();
	
	/**
	 * Class descriptor for current stream context
	 */
	private JrankClass type;
	/**
	 * Current stream object
	 */
	private Object object;
	/**
	 * {@link PutField} for the current object
	 */
	private JrankPutFields putFields;
	/**
	 * {@link GetField} for the current object
	 */
	private JrankGetFields getFields;
	
	private JrankContext() {}
	
	public JrankContext(JrankClass type, Object object) {
		this.type = type;
		this.object = object;
		putFields = new JrankPutFields();
		getFields = new JrankGetFields();
	}

	public JrankClass getType() {
		return type;
	}

	public Object getObject() {
		return object;
	}
	
	public JrankPutFields getPutFields() {
		return putFields;
	}
	
	public JrankGetFields getGetFields() {
		return getFields;
	}
	
	@Override
	public String toString() {
		return String.valueOf(type);
	}
}
