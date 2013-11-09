/*
 * =============================================================================
 *
 *   Copyright (c) 2013, Connect Group (http://www.connect-group.com)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package com.connect_group.thymeleaf.bean;


import java.util.Map;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.AbstractProcessor;
import org.thymeleaf.processor.AttributeNameProcessorMatcher;
import org.thymeleaf.processor.IAttributeNameProcessorMatcher;
import org.thymeleaf.processor.IProcessorMatcher;
import org.thymeleaf.processor.ProcessorMatchingContext;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

/**
 * A processor base class that can handle both attribute and DOM manipulation.
 * @author adam
 *
 */
public abstract class BaseAttributeProcessor extends AbstractProcessor {
	
	private final IAttributeNameProcessorMatcher matcher;
	
	public BaseAttributeProcessor(String attributeNameWithoutNamespace) {
		matcher = new AttributeNameProcessorMatcher(attributeNameWithoutNamespace);
	}
	
	@Override
	protected final ProcessorResult doProcess(final Arguments arguments, final ProcessorMatchingContext processorMatchingContext, final Node node) {
		Element element = (Element)node;
		String attributeName = getAttributeName(processorMatchingContext, element);

		Object evaluated = parseExpression(arguments, element.getAttributeValue(attributeName));
		String contextRoot = getContextRoot(arguments);
		
		AttributeData attrData = new AttributeData(attributeName, element, evaluated, contextRoot);
		
		ProcessorResult result = doProcess(arguments, attrData);
		attrData.element.removeAttribute(attrData.attributeName);
		return result;
	}
	

	public final IProcessorMatcher<? extends Node> getMatcher() {
		return matcher;
	}
	

    protected final String defaultToEmpty(final String str) {
        return (str == null? "" : str);
    }
    
    protected final String defaultToNull(final String str) {
        return ((str != null && str.length() == 0)? null : str);
    }
	
	protected abstract ProcessorResult doProcess(final Arguments arguments, AttributeData data);
	
	protected String getAttributeName(final ProcessorMatchingContext processorMatchingContext, Element element) {
		String result = "";
		String[] attributeNames = this.matcher.getAttributeNames(processorMatchingContext);
		if(attributeNames!=null && attributeNames.length>0) {
	        for (final String attributeName : attributeNames) {
	            if (element.hasNormalizedAttribute(attributeName)) {
	                result = attributeName;
	                break;
	            }
	        }
		}
		return result;
	}
	
	private String getContextRoot(Arguments arguments) {
		String root=(String) parseExpression(arguments, "@{'/'}");
		return root;
	}

	protected Object parseExpression(final Arguments arguments, final String expressionString) {
        final IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(arguments.getConfiguration());
        final IStandardExpression expression = expressionParser.parseExpression(arguments.getConfiguration(), arguments, expressionString);
        final Object value = expression.execute(arguments.getConfiguration(), arguments);

        return value;
	}
	
    protected final ProcessorResult updateAttributes(Element element, Map<String,String> modifiedAttributeValues) {
        
        for (final Map.Entry<String,String> modifiedAttributeEntry : modifiedAttributeValues.entrySet()) {

            
            final String modifiedAttributeName = modifiedAttributeEntry.getKey();
            if(!isIgnoredAttribute(modifiedAttributeName)) {
	            String newAttributeValue = modifiedAttributeEntry.getValue();
	            
	            newAttributeValue = defaultToNull(newAttributeValue);
	
	            final boolean removeAttributeIfEmpty =
	                removeAttributeIfEmpty(modifiedAttributeName);
	            
	            // Do NOT use trim() here! Non-thymeleaf attributes set to ' ' could have meaning!
	            if (removeAttributeIfEmpty && newAttributeValue == null) {
	                element.removeAttribute(modifiedAttributeName);
	            } else {
	                element.setAttribute(modifiedAttributeName, defaultToEmpty(newAttributeValue));
	            }
            }            
        }
        
        return ProcessorResult.OK;
        
    }

	protected abstract boolean isIgnoredAttribute(String modifiedAttributeName);

	protected boolean removeAttributeIfEmpty(String modifiedAttributeName) {
		return true;
	}
}

