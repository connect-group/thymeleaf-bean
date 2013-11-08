package com.connect_group.thymeleaf.use;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class UseTests {
	UseProcessor use = new UseProcessor();
	
	@Test
	public void testSimpleBean() {
		
		Object bean = new Object() {
			public String getTitle() { return "thetitle"; }
			public String getAlt() { return "some alternative"; }
		};
		
		Map<String,String> map = use.getProperties(bean);
		
		assertEquals(2, map.size());
		assertEquals("thetitle", map.get("title"));
		assertEquals("some alternative", map.get("alt"));
	}
	
}