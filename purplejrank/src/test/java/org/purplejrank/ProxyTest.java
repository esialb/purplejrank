package org.purplejrank;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.Assert;
import org.junit.Test;

public class ProxyTest {
	public static interface A {
		public String foo();
	}
	public static interface B {
		public String bar();
	}
	public static class IH implements InvocationHandler, Serializable {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			return method.getName();
		}
	}
	
	@Test
	public void testProxy() throws Exception {
		Object proxy = Proxy.newProxyInstance(
				ProxyTest.class.getClassLoader(), 
				new Class<?>[] {A.class, B.class}, 
				new IH());
		Object proxy2 = Util.cycle(proxy);
		
		Assert.assertEquals("foo", ((A) proxy2).foo());
		Assert.assertEquals("bar", ((B) proxy2).bar());
	}
}
