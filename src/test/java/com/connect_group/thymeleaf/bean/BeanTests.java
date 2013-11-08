package com.connect_group.thymeleaf.bean;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.connect_group.thymeleaf.bean.BeanProcessor;

public class BeanTests {
	BeanProcessor use = new BeanProcessor();
	
	@Test
	public void testBeanWithStrings() {
		
		Object bean = new Object() {
			public String getTitle() { return "thetitle"; }
			public String getAlt() { return "some alternative"; }
		};
		
		Map<String,String> map = use.getProperties(bean);
		
		assertEquals(2, map.size());
		assertEquals("thetitle", map.get("title"));
		assertEquals("some alternative", map.get("alt"));
	}
	
	@Test
	public void testBeanWithBoolean() {
		Object bean = new Object() {
			public boolean isElephant() { return true; }
			public boolean getBanana() { return true; }
			public boolean isFalseSoWontReturnAProperty() { return false; }
		};
		
		Map<String,String> map = use.getProperties(bean);
		
		assertEquals(2, map.size());
		assertEquals("elephant", map.get("elephant"));
		assertEquals("banana", map.get("banana"));
	}
	
	@Test
	public void testNonBeanMethods() {
		Object bean = new Object() {
			public String get() { return "not a bean getter"; }
			public String banana() { return "also not a bean getter"; }
		};
		
		Map<String,String> map = use.getProperties(bean);
		
		assertEquals(0, map.size());
	}
	
	@Test
	public void testBeanWithEmptyCollection() {
		
		Object bean = new Object() {
			public List<String> getList() { return Arrays.asList(new String[] {}); }
		};
		
		Map<String,String> map = use.getProperties(bean);
		
		assertEquals(0, map.size());
	}
	
	@Test
	public void testBeanWithCollectionOfStrings() {
		
		Object bean = new Object() {
			public List<String> getList() { return Arrays.asList(new String[] {"a","b","c"}); }
		};
		
		Map<String,String> map = use.getProperties(bean);
		
		assertEquals(1, map.size());
		assertEquals("a b c", map.get("list"));
	}
	
	@Test
	public void testBeanWithArrayOfChar() {
		Object bean = new Object() {
			public char[] getChars() { return new char[] {'a','b','c'}; }
		};
		
		Map<String,String> map = use.getProperties(bean);
		
		assertEquals(1, map.size());
		assertEquals("a b c", map.get("chars"));
	}
	
	@Test
	public void testBeanWithArrayOfDouble() {
		Object bean = new Object() {
			public double[] getDoubles() { return new double[] {1.0D,2.7D}; }
		};
		
		Map<String,String> map = use.getProperties(bean);
		
		assertEquals(1, map.size());
		assertEquals("1.0 2.7", map.get("doubles"));

	}
	
	@Test
	public void testBeanWithArrayOfArrayOfDouble() {
		Object bean = new Object() {
			public double[][] getDoubles() { return new double[][] { new double[] {1.0D,2.7D}, new double[] {3.99D, 77.8D}}; }
		};
		
		Map<String,String> map = use.getProperties(bean);
		
		assertEquals(1, map.size());
		assertEquals("1.0 2.7 3.99 77.8", map.get("doubles"));
	}

	@Test
	public void testBeanWithMap() {
		Object bean = new Object() {
			public Map<String,String> getMap() {
				HashMap<String,String> map = new HashMap<String,String>();
				map.put("X","Y");
				return map;
			}
		};
		
		Map<String,String> map = use.getProperties(bean);
		
		assertEquals(1, map.size());
		assertEquals("{X=Y}", map.get("map"));

	}
	
	@Test
	public void testNonBean() {
		Object bean = new Object();
		Map<String,String> map = use.getProperties(bean);
		assertEquals(0, map.size());
	}
	
	@Test
	public void testMap() {
		HashMap<String,String> bean = new HashMap<String,String>();
		bean.put("left","right");
		bean.put("up", "down");
		
		Map<String,String> map = use.getProperties(bean);
		assertEquals(2, map.size());
		assertEquals("right", map.get("left"));
		assertEquals("down", map.get("up"));
		
	}
	
}