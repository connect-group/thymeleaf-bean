thymeleaf-bean
==============

Thymeleaf standard dialect extension that converts object properties to tag attributes.

    <a th:bean="${linkBean}">Hello!</a>
    
Attributes such as href, title, class etc will be set according to the properties of the object, "linkBean".

The bean attribute also supports a Map<String,String> e.g.

    <a th:bean="${linkMap}">Hello!</a>
    
## A Bean For example

    class MyBean {
        String getAlt();
        String getHref();
        boolean getDisabled();
        List<String> getCssClass();
    }
     
The above bean would generate an 'alt' and a 'href' tag respectively.
 
If a 'getter' returns a Boolean, then the attribute value will be set to equal the attribute name; e.g.

    disabled="disabled"
   
Because the method 'getClass' is part of the Object, a method named getCssClass is required to set the Class.

The return value of each getter will be converted to a string.  Collections will be converted to a single string with each value separated by a single space.
