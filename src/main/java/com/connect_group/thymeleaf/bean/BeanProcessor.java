package com.connect_group.thymeleaf.bean;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Macro;
import org.thymeleaf.dom.Node;
import org.thymeleaf.dom.Text;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.util.StringUtils;

/**
 * The bean processor will react to a th:bean attribute.
 * 
 * The attribute must evaluate to a Java Object.
 * 
 * Using the 'bean' guidelines, any "getters" of the bean will be treated as attributes.
 * Alternatively if the Object is a Map, the map entries will be translated into attributes.
 * 
 * For example,
 *  
 *     class MyBean {
 *         String getAlt();
 *         String getHref();
 *         boolean getDisabled();
 *         List&lt;String&gt; getCssClass();
 *     }
 * 
 * The above bean would generate an 'alt' and a 'href' tag respectively.
 * 
 * If a 'getter' returns a Boolean, then the attribute value will be set to equal the attribute name; e.g.
 * 
 *     disabled="disabled"
 *     
 * Because the method 'getClass' is part of the Object, a method named getCssClass is required to set the Class.
 * 
 * The return value of each getter will be converted to a string.  
 * Collections will be converted to a single string with each value separated by a single space.
 * So a List&lt;String&gt; which contains ["abc","def"] would become "abc def".
 * 
 * Special Case: getText and getUText
 * These methods will modify the content of an attribute in the same way as th:text and th:utext respectively.
 * 
 * Special Case: getData()
 * If a bean has a method called getData which returns a Map&lt;String,?&lt; then these will be converted into data- attributes.
 * 
 * E.g.
 *   {  "mobile-url" : "http://example.com/example.jpg" }
 * ... will become data-mobile-url="http://example.com/example.jpg"
 * 
 * Special Case: getDataXxxXxx()
 * If a bean has a property whose name begins with "getData" then the method name indicates a data attribute.
 * The "camel case" of the property name will be processed such that a hyphen is inserted before each capital letter.
 * It will then convert to lowercase.
 * 
 *  E.g.
 *     String getDataMobileUrl()  will become data-mobile-url
 * 
 * @author adam
 *
 */
public class BeanProcessor extends BaseAttributeProcessor {

	public BeanProcessor() {
		super("use");
	}
	

	@Override
	protected ProcessorResult doProcess(Arguments arguments, AttributeData data) {
		Map<String,String> modifiedAttributes = getProperties(data.evaluatedAttributeValue);
		ProcessorResult result = updateAttributes(data.element, modifiedAttributes);
		
		if(result.isOK()) {
	    	List<Node> modifiedChildren = getModifiedChildren(modifiedAttributes);
	    	
	    	if(modifiedChildren!=null) {
	    		data.element.clearChildren();
	    		data.element.setChildren(modifiedChildren);
	    	}

		}
		return result;
	}
	
	private List<Node> getModifiedChildren(final Map<String, String> modifiedAttributes) {
		if(modifiedAttributes.containsKey("text")) {
			Node node = new Text(modifiedAttributes.get("text"));
			node.setProcessable(false);
			return Collections.singletonList(node);
		}
		
		if(modifiedAttributes.containsKey("utext")) {
			Node node = new Macro(modifiedAttributes.get("utext"));
			node.setProcessable(false);
			return Collections.singletonList(node);
		}
		
		return null;
	}


	@Override
	public int getPrecedence() {
		return 200;
	}
	
	protected Map<String,String> getProperties(final Object obj) {
		HashMap<String,String> map = new HashMap<String,String>();
		
		map.putAll(getMapProperties(obj));
		
		try {
			BeanInfo info = Introspector.getBeanInfo(obj.getClass());
			PropertyDescriptor[] pds = info.getPropertyDescriptors();
			
			if(pds!=null) {
				for(PropertyDescriptor pd : pds) {
					String name = pd.getName();
					
					if(!"class".equals(name)) {
						name = processName(name);
						
						String result = null;
						if(pd instanceof IndexedPropertyDescriptor) {
							// Ignore indexed methods.
						} else {
							if("data".equals(name) && isInstanceofMap(pd.getReadMethod().getReturnType())) {
								map.putAll(extractDataAttributes(pd, obj));
							} else {
								result = getResult(pd, obj);
							}
						}
						
						if(!StringUtils.isEmpty(result)) {
							map.put(name,result);
						}
					}
				}
			}
		} catch (IntrospectionException e) {
		}
		
		return map;
	}
	
	private Map<? extends String, ? extends String> extractDataAttributes(PropertyDescriptor pd, Object obj) {

		HashMap<String,String> result = new HashMap<String,String>();
		
		try {
			Map<?,?> map = (Map<?,?>)pd.getReadMethod().invoke(obj);
			for(Entry<?,?> entry : map.entrySet()) {
				if(entry.getKey() instanceof String) {
					String name="data-" + (String)entry.getKey();
					String value=asString(entry.getValue(), "true", "false");
					result.put(name, value);
				}
			}
		} catch (Exception e) {
		
		}
		return result;
	}


	private boolean isInstanceofMap(Class<?> type) {
		return Map.class.isAssignableFrom(type);
	}


	static String processName(final String name) {
		String newName = name;
		
		if("cssClass".equals(name)) {
			newName="class";
		} else if(isDataAttribute(name)){
			newName = uncamel(name);
		}
		return newName;
	}

	static String uncamel(String name) {
		StringBuilder uncamel = new StringBuilder();
		
		for(int i=0; i<name.length(); i++) {
			char c = name.charAt(i);
			if(isUppercase(c)) {
				uncamel.append("-");
			}
			uncamel.append(c);
		}
		
		return uncamel.toString().toLowerCase();
	}


	static boolean isDataAttribute(String name) {
		return (name.length()>4 && name.startsWith("data") && isUppercase(name.charAt(4)));
	}
	
	static boolean isUppercase(char c) {
		return (c>='A' && c<='Z');
	}

	private Map<String,String> getMapProperties(final Object obj) {
		if(obj instanceof Map<?,?>) {
			return getMapProperties((Map<?,?>)obj);
		}
		return Collections.emptyMap();
	}
	
	private Map<String,String> getMapProperties(final Map<?,?> map) {
		HashMap<String,String> result = new HashMap<String,String>();
		
		for(Entry<?,?> entry : map.entrySet()) {
			if(entry.getKey() instanceof String) {
				String name = (String) entry.getKey();
				String value = asString(entry.getValue(),name,null);
				
				if(value!=null) {
					result.put(name, value);
				}
			}
		}
		
		return result;
	}

	private String getResult(final PropertyDescriptor pd, final Object obj) {
		String name = pd.getName();
		Method method = pd.getReadMethod();
		if(method!=null) {
			try {
				return asString(method.invoke(obj), name, null);
			} catch (Exception e) {}
		}
		return null;
	}

	private String asString(final Object obj, final String booleanTrueResult, final String booleanFalseResult) {
		if(obj==null) {
			return null;
		}
		
		if(obj instanceof String) {
			return (String)obj;
		}
		
		if(obj instanceof Boolean) {
			if(((Boolean)obj).booleanValue()) {
				return booleanTrueResult;
			} else {
				return booleanFalseResult;
			}
		}
		
		if(obj instanceof Object[]) {
			return asString((Object[])obj);
		} else if(obj instanceof byte[]) {
			return asString((byte[])obj);
		} else if(obj instanceof short[]) {
			return asString((short[])obj);
		} else if(obj instanceof int[]) {
			return asString((int[])obj);
		} else if(obj instanceof long[]) {
			return asString((long[])obj);
		} else if(obj instanceof float[]) {
			return asString((float[])obj);
		} else if(obj instanceof double[]) {
			return asString((double[])obj);
		} else if(obj instanceof char[]) {
			return asString((char[])obj);
		}
		
		if(obj instanceof Iterable) {
			return asString((Iterable<?>)obj);
		}
		
		return obj.toString();
	}
	
	private String asString(final Iterable<?> it) {
		StringBuilder str = new StringBuilder();
		
		for(Object o : it) {
			append(str, asString(o, "true", "false"));
		}
		return str.toString();
	}

	private String asString(final Object[] arr) {
		StringBuilder str = new StringBuilder();
		for(Object o : arr) {
			append(str, asString(o, "true", "false"));
		}
		return str.toString();
	}
	
	private String asString(final byte[] arr) {
		StringBuilder str = new StringBuilder();
		for(byte b : arr) {
			append(str,b);
		}
		return str.toString();		
	}

	private String asString(final short[] arr) {
		StringBuilder str = new StringBuilder();
		for(short s : arr) {
			append(str,s);
		}
		return str.toString();		
	}
	
	private String asString(final int[] arr) {
		StringBuilder str = new StringBuilder();
		for(int i : arr) {
			append(str, i);
		}
		return str.toString();		
	}

	private String asString(final long[] arr) {
		StringBuilder str = new StringBuilder();
		for(long l : arr) {
			append(str,l);
		}
		return str.toString();		
	}

	private String asString(final float[] arr) {
		StringBuilder str = new StringBuilder();
		for(float f : arr) {
			append(str,f);
		}
		return str.toString();		
	}

	private String asString(final double[] arr) {
		StringBuilder str = new StringBuilder();
		for(double d : arr) {
			append(str,d);
		}
		return str.toString();		
	}
	
	private String asString(final char[] arr) {
		StringBuilder str = new StringBuilder();
		for(char c : arr) {
			append(str,c);
		}
		return str.toString();		
	}
	
	private void append(final StringBuilder str, final Object val) {
		if(str.length()!=0) str.append(" ");
		str.append(val);
	}


	@Override
	protected boolean isIgnoredAttribute(String modifiedAttributeName) {
		return "text".equals(modifiedAttributeName) || "utext".equals(modifiedAttributeName);
	}


}
