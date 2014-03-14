package org.purplejrank.jdk;

import static org.purplejrank.jdk.JdkNFAState.*;
import static org.purplejrank.jdk.JdkNFAToken.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class JdkNFARule implements Comparable<JdkNFARule> {
	
	private static final JdkNFARule[] TC_RULES = new JdkNFARule[] {
		stream.to(stream$magic, STREAM_MAGIC),
		stream$magic.to(stream$magic$version, STREAM_VERSION),
		object.to(object$TC_RESET, TC_RESET),
		newClass.to(newClass$TC_CLASS, TC_CLASS),
		newClassDesc.to(newClassDesc$TC_CLASSDESC, TC_CLASSDESC),
		newClassDesc.to(newClassDesc$TC_PROXYCLASSDESC, TC_PROXYCLASSDESC),
		newArray.to(newArray$TC_ARRAY, TC_ARRAY),
		newObject.to(newObject$TC_OBJECT, TC_OBJECT),
		blockdatashort.to(blockdatashort$TC_BLOCKDATA, TC_BLOCKDATA),
		blockdatalong.to(blockdatalong$TC_BLOCKDATALONG, TC_BLOCKDATALONG),
		endBlockData.to(endBlockData$TC_ENDBLOCKDATA, TC_ENDBLOCKDATA),
		newString.to(newString$TC_STRING, TC_STRING),
		newString.to(newString$TC_LONGSTRING, TC_LONGSTRING),
		newEnum.to(newEnum$TC_ENUM, TC_ENUM),
		prevObject.to(prevObject$TC_REFERENCE, TC_REFERENCE),
		nullReference.to(nullReference$TC_NULL, TC_NULL),
		exception.to(exception$TC_EXCEPTION, TC_EXCEPTION),
	};
	
	private static final JdkNFARule[] RULES;
	static {
		List<JdkNFARule> rules = new ArrayList<JdkNFARule>(Arrays.asList(TC_RULES));
		
		// Add all the rules extractable from state names
		for(JdkNFAState state : JdkNFAState.values()) {
			if(!state.name().contains("$"))
				continue;
			String prefix = state.name().substring(0, state.name().lastIndexOf("$"));
			String suffix = state.name().substring(state.name().lastIndexOf("$") + 1);
			
			JdkNFARule implied = new JdkNFARule(JdkNFAState.valueOf(prefix), state);
			if(!rules.contains(implied))
				rules.add(implied);
			
			JdkNFARule link;
			try {
				link = new JdkNFARule(state, JdkNFAState.valueOf(suffix));
			} catch(IllegalArgumentException e) {
				continue;
			}
			if(!rules.contains(link))
				rules.add(link);
		}
		
		// compute derived vias
		boolean changed;
		do {
			changed = false;
			for(JdkNFARule rule : rules) {
				if(rule.via != null)
					continue;
				for(JdkNFARule r2 : rules) {
					if(rule.to == r2.from)
						changed |= rule.derivedVia.addAll(r2.derivedVia);
				}
			}
		} while(changed);
		
		for(JdkNFARule rule : rules) {
			if(rule.via != null)
				rule.derivedVia.clear();
			else
				rule.via = new JdkNFAToken[0];
		}
		
		Collections.sort(rules);
		
		RULES = rules.toArray(new JdkNFARule[0]);
	}

	public static final JdkNFARule[] rules() {
		return RULES.clone();
	}

	protected JdkNFAState from;
	protected JdkNFAState to;
	protected JdkNFAToken[] via;
	protected Set<JdkNFAToken> derivedVia = EnumSet.noneOf(JdkNFAToken.class);

	JdkNFARule(JdkNFAState from, JdkNFAState to, JdkNFAToken... via) {
		super();
		this.from = from;
		this.to = to;
		if(via.length > 0) {
			this.via = via;
			this.derivedVia.addAll(Arrays.asList(via));
		}
		else
			this.via = null;
	}
	
	@Override
	public String toString() {
		return from + " -> " + Arrays.asList(via) + derivedVia + " -> " + to;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(obj == this)
			return true;
		if(obj instanceof JdkNFARule) {
			return from == ((JdkNFARule) obj).from && to == ((JdkNFARule) obj).to;
		}
		return false;
	}
	
	public JdkNFAState getFrom() {
		return from;
	}

	public JdkNFAToken[] getVia() {
		return via;
	}

	public JdkNFAToken[] getDerivedVia() {
		return derivedVia.toArray(new JdkNFAToken[0]);
	}
	
	public JdkNFAState getTo() {
		return to;
	}

	@Override
	public int compareTo(JdkNFARule o) {
		return from.compareTo(o.from);
	}
}
