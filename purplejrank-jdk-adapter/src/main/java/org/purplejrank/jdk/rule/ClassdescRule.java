package org.purplejrank.jdk.rule;

import org.purplejrank.jdk.block.ClassdescBlock;

public interface ClassdescRule extends SuperclassdescRule {
	public String getClassName();
	public ClassdescBlock getSuperClassDesc();
}
