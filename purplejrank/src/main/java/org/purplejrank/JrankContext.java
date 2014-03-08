package org.purplejrank;

public class JrankContext {
	public static final JrankContext NO_CONTEXT = new JrankContext();
	
	private JrankClass type;
	private Object object;
	
	private JrankContext() {}
	
	public JrankContext(JrankClass type, Object object) {
		this.type = type;
		this.object = object;
	}

	public JrankClass getType() {
		return type;
	}

	public Object getObject() {
		return object;
	}
}
