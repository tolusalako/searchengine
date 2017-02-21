package net.csthings.mail.utils;

import javax.naming.Context;

import cs121.utils.ModelKeys;
import net.csthings.common.context.CommonThreadContext;

public final class TestProperties {
	public static final String TEST_USER_ID = "5d94cb31-c4a1-11e6-9f8f-117edfedddf4";
	
	static {
		CommonThreadContext.put(ModelKeys.userId, TEST_USER_ID);
	}
	
	public TestProperties(){
		
	}

}
