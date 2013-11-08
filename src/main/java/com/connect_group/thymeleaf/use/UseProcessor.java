package com.connect_group.thymeleaf.use;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

// TODO: support for text and utext.

public class UseProcessor extends AbstractAttributeModifierAttrProcessor {

	public UseProcessor() {
		super("use");
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
						
						if(result!=null) {
							map.put(name,result);
						}
					}
				}
			}
		} catch (IntrospectionException e) {
		}
		
		return map;
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
		
		return obj.toString();
	}
	
	private String asString(Object[] arr) {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for(Object o : arr) {
			if(first) str.append(" ");
			str.append(asString(o, "true", "false"));
		}
		return str.toString();
	}
	
	private String asString(byte[] arr) {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for(byte b : arr) {
			if(first) str.append(" ");
			str.append(b);
		}
		return str.toString();		
	}

	private String asString(short[] arr) {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for(short s : arr) {
			if(first) str.append(" ");
			str.append(s);
		}
		return str.toString();		
	}
	
	private String asString(int[] arr) {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for(int i : arr) {
			if(first) str.append(" ");
			str.append(i);
		}
		return str.toString();		
	}

	private String asString(long[] arr) {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for(long l : arr) {
			if(first) str.append(" ");
			str.append(l);
		}
		return str.toString();		
	}

	private String asString(float[] arr) {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for(float f : arr) {
			if(first) str.append(" ");
			str.append(f);
		}
		return str.toString();		
	}

	private String asString(double[] arr) {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for(double d : arr) {
			if(first) str.append(" ");
			str.append(d);
		}
		return str.toString();		
	}
	
	private String asString(char[] arr) {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for(char c : arr) {
			if(first) str.append(" ");
			str.append(c);
		}
		return str.toString();		
	}

}
