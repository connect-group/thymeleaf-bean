package com.connect_group.thymeleaf.bean;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.util.StringUtils;

// TODO: support for text and utext.
// TODO: support for Map<String,Object>
// TODO: support for data- attributes e.g. Map getData(); getDataMobile() --> data-mobile

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
 *         List<String> getCssClass();
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
 * The return value of each getter will be converted to a string.  Collections will be converted to a single string with each value separated by a single space.
 *
 * @author adam
 *
 */
public class BeanProcessor extends AbstractAttributeModifierAttrProcessor {

	public BeanProcessor() {
		super("bean");
	}
	
	@Override
	protected Map<String, String> getModifiedAttributeValues(
			Arguments arguments, Element element, String attributeName) {

		String expression = element.getAttributeValue(attributeName);
		Object object = parseExpression(arguments, expression);
		
		return getProperties(object);
	}

	@Override
	protected ModificationType getModificationType(Arguments arguments,
			Element element, String attributeName, String newAttributeName) {
		return ModificationType.SUBSTITUTION;
	}

	@Override
	protected boolean removeAttributeIfEmpty(Arguments arguments,
			Element element, String attributeName, String newAttributeName) {
		return true;
	}

	@Override
	protected boolean recomputeProcessorsAfterExecution(Arguments arguments,
			Element element, String attributeName) {
		return false;
	}

	@Override
	public int getPrecedence() {
		return 1500;
	}
	
	private Object parseExpression(Arguments arguments, String expressionString) {
        final IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(arguments.getConfiguration());
        final IStandardExpression expression = expressionParser.parseExpression(arguments.getConfiguration(), arguments, expressionString);
        final Object value = expression.execute(arguments.getConfiguration(), arguments);

        return value;
	}
	
	protected Map<String,String> getProperties(Object obj) {
		HashMap<String,String> map = new HashMap<String,String>();
		
		map.putAll(getMapProperties(obj));
		
		try {
			BeanInfo info = Introspector.getBeanInfo(obj.getClass());
			PropertyDescriptor[] pds = info.getPropertyDescriptors();
			
			if(pds!=null) {
				for(PropertyDescriptor pd : pds) {
					String name = pd.getName();
					
					if(!"class".equals(name)) {
						
						if("cssClass".equals(name)) {
							name="class";
						}
						
						String result = null;
						if(pd instanceof IndexedPropertyDescriptor) {
							// Ignore indexed methods.
						} else {
							result = getResult(pd, obj);
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
	
	private Map<String,String> getMapProperties(Object obj) {
		if(obj instanceof Map<?,?>) {
			return getMapProperties((Map<?,?>)obj);
		}
		return Collections.emptyMap();
	}
	
	private Map<String,String> getMapProperties(Map<?,?> map) {
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

	private String getResult(PropertyDescriptor pd, Object obj) {
		String name = pd.getName();
		Method method = pd.getReadMethod();
		if(method!=null) {
			try {
				return asString(method.invoke(obj), name, null);
			} catch (Exception e) {}
		}
		return null;
	}

	private String asString(Object obj, String booleanTrueResult, String booleanFalseResult) {
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
	
	private String asString(Iterable<?> it) {
		StringBuilder str = new StringBuilder();
		
		for(Object o : it) {
			append(str, asString(o, "true", "false"));
		}
		return str.toString();
	}

	private String asString(Object[] arr) {
		StringBuilder str = new StringBuilder();
		for(Object o : arr) {
			append(str, asString(o, "true", "false"));
		}
		return str.toString();
	}
	
	private String asString(byte[] arr) {
		StringBuilder str = new StringBuilder();
		for(byte b : arr) {
			append(str,b);
		}
		return str.toString();		
	}

	private String asString(short[] arr) {
		StringBuilder str = new StringBuilder();
		for(short s : arr) {
			append(str,s);
		}
		return str.toString();		
	}
	
	private String asString(int[] arr) {
		StringBuilder str = new StringBuilder();
		for(int i : arr) {
			append(str, i);
		}
		return str.toString();		
	}

	private String asString(long[] arr) {
		StringBuilder str = new StringBuilder();
		for(long l : arr) {
			append(str,l);
		}
		return str.toString();		
	}

	private String asString(float[] arr) {
		StringBuilder str = new StringBuilder();
		for(float f : arr) {
			append(str,f);
		}
		return str.toString();		
	}

	private String asString(double[] arr) {
		StringBuilder str = new StringBuilder();
		for(double d : arr) {
			append(str,d);
		}
		return str.toString();		
	}
	
	private String asString(char[] arr) {
		StringBuilder str = new StringBuilder();
		for(char c : arr) {
			append(str,c);
		}
		return str.toString();		
	}
	
	private void append(StringBuilder str, Object val) {
		if(str.length()!=0) str.append(" ");
		str.append(val);
	}

}
