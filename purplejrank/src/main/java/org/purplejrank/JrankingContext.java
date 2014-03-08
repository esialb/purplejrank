package org.purplejrank;

public class JrankingContext {
	public static final JrankingContext NO_CONTEXT = new JrankingContext();
	
	private Jranklass type;
	private Object object;
	
	private JrankingContext() {}
	
	public JrankingContext(Jranklass type, Object object) {
		this.type = type;
		this.object = object;
	}

	public Jranklass getType() {
		return type;
	}

	public Object getObject() {
		return object;
	}
}
