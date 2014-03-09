package org.purplejrank;

public class JrankContext {
	public static final JrankContext NO_CONTEXT = new JrankContext();
	
	private JrankClass type;
	private Object object;
	private JrankPutFields putFields;
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
