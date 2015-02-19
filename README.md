thymeleaf-bean
==============

Thymeleaf dialect which converts object properties to tag attributes.


## Maven

Include the latest release from Maven,

		<dependency>
			<groupId>com.connect-group</groupId>
			<artifactId>thymesheet-spring3</artifactId>
			<version>2.1.0</version>
		</dependency>

## Getting Started

    <a bean:use="${linkBean}">Hello!</a>
    
Attributes such as href, title, class etc will be set according to the properties of the object, "linkBean".

The bean attribute also supports a Map<String,String> e.g.

    <a bean:use="${linkMap}">Hello!</a>
    
## A Bean example

    class LinkBean {
        String getAlt();
        String getHref();
        boolean getDisabled();
        List<String> getCssClass();
    }
     
The above bean would generate an 'alt' and a 'href' tag respectively.
 
If a 'getter' returns a Boolean, then the attribute value will be set to equal the attribute name; e.g.

    disabled="disabled"

### Special Case: Class and CssClass
Because the method 'getClass' is part of the Java Object, we can't use that to specify class.  
Instead a method named getCssClass() is required to set the Class.

### Collection return types
The return value of each getter will be converted to a string.  Collections will be converted to a single string with each value separated by a single space.

So a List<String> which contains ["abc","def"] would become "abc def".

    class="abc def"

## Text and Unescaped Text
If the map has a text or utext key; or a bean has a getText or getUtext() method, these will be treated as th:text and th:utext respectively.

## Data Attributes
For maps, simply map data-attr-name to a value and these will become data attributes.  E.g.

    map.put("href", "http://www.example.com");
    map.put("text", "link to example");
    map.put("data-some-stuff", "a data attribute");
    
For beans there are two ways to populate data attributes;

### Map<String,?> getData()
If a method named getData() is found on the bean, and it returns a map, then the properties of the map are treated as data attributs.

For example,

    class Bean {
        Map<String,String> getData() {
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("x","y");
            return map;
        }
    }

... will generate something like,

    <div data-x="y">...</div>

### String getDataMyCustomAttribute()
Any property whose name starts with "data" is treated as a data- attribute.  The "camel-case" of the property name is converted into hyphens.  
So for example, getDataMyCustomAttribute() will generate data-my-custom-attribute.




