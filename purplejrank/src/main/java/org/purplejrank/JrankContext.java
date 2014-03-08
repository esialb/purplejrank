package org.purplejrank;

public class JrankContext {
	public static final JrankContext NO_CONTEXT = new JrankContext();
	
	private JrankClass type;
	private Object object;
	private JrankPutFields putFields;
	
	private JrankContext() {}
	
	public JrankContext(JrankClass type, Object object) {
		this.type = type;
		this.object = object;
		this.putFields = new JrankPutFields();
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
}
