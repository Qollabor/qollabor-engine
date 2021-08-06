/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.definition;

import org.qollabor.cmmn.definition.casefile.CaseFileItemDefinitionDefinition;
import org.qollabor.processtask.definition.ProcessDefinition;
import org.qollabor.util.XMLHelper;
import org.w3c.dom.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

/**
 * Base class for parsing XML elements defined in the CMMN specification.
 */
public abstract class XMLElementDefinition {
    protected final static String NAMESPACE_URI = "org.qollabor";

    private final ModelDefinition definition;
    private final XMLElementDefinition parentElement;
    private final Element element;

    private static final String EXTENSIONELEMENTS = "extensionElements";

    protected XMLElementDefinition(Element element, ModelDefinition definition, XMLElementDefinition parentElement) {
        this.element = element;
        this.definition = definition;
        this.parentElement = parentElement;
    }

    /**
     * Returns a description of the context this element provides to it's children. Can be used e.g. in expressions or on parts
     * to get the description of the parent element when encountering validation errors.
     *
     * @return
     */
    public String getContextDescription() {
        return "";
    }

    /**
     * Returns the top level element within the &lt;definitions&gt; to which this element belongs. This is typically
     * the {@link CaseDefinition}, {@link ProcessDefinition} or {@link CaseFileItemDefinitionDefinition}.
     *
     * @return
     */
    public ModelDefinition getModelDefinition() {
        return definition;
    }

    public CaseDefinition getCaseDefinition() {
        return (CaseDefinition) getModelDefinition();
    }

    public ProcessDefinition getProcessDefinition() {
        return (ProcessDefinition) getModelDefinition();
    }

    /**
     * Casts the parent element to the expected type for convenience.
     *
     * @return
     */
    public <T extends CMMNElementDefinition> T getParentElement() {
        @SuppressWarnings("unchecked")
        T typedParent = (T) parentElement;
        return typedParent;
    }

    public Element getElement() {
        return element;
    }

    public String getType() {
        String simpleName = getClass().getSimpleName();
        if (simpleName.endsWith("Definition")) {
            simpleName = simpleName.substring(0, simpleName.length() - "Definition".length());
        }
        return simpleName;
    }

    /**
     * CMMN.xsd compliant XML documents are parsed in 2 phases.
     * In the first phase, for every element defined in the XSD, a class extending CMMNElementDefinition is instantiated.
     * In the second phase, when all elements have been made available in the definition, they can start referring to
     * each other. This logic can be implemented in the resolveReferences method.
     * Subclasses implementing this method are encouraged not to forget to invoke <code>super.resolveReferences()</code>, especially if they are subclassing
     * a subclass from {@link XMLElementDefinition}.
     */
    protected void resolveReferences() {
    }

    /**
     * Creates a new instance of class T based on the XML element. If T is of type {@link String}, then it will simply return the text content of the
     * XML element. Otherwise T is expected to have a constructor with {@link Element}, {@link ModelDefinition} and {@link XMLElementDefinition}.
     *
     * @param xmlElement
     * @param typeClass
     * @return
     */
    private <T> T instantiateT(Element xmlElement, Class<T> typeClass) {
        try {
            if (typeClass.equals(String.class)) {
                @SuppressWarnings("unchecked")
                // we just checked it...
                        T t = (T) xmlElement.getTextContent();
                return t;
            }

            Constructor<T> tConstructor = typeClass.getConstructor(Element.class, ModelDefinition.class, CMMNElementDefinition.class);
            T childDefinition = tConstructor.newInstance(xmlElement, getModelDefinition(), this);
            return childDefinition;
        } catch (InstantiationException e) {
            String msg = "The class " + typeClass.getName() + " cannot be instantiated";
            getModelDefinition().fatalError(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "The class " + typeClass.getName() + " cannot be accessed";
            getModelDefinition().fatalError(msg, e);
        } catch (IllegalArgumentException e) {
            String msg = "The class " + typeClass.getName() + " cannot be instantiated";
            getModelDefinition().fatalError(msg, e);
        } catch (InvocationTargetException e) {
            String msg = "The class " + typeClass.getName() + " cannot be instantiated";
            getModelDefinition().fatalError(msg, e);
        } catch (NoSuchMethodException e) {
            String msg = "The class " + typeClass.getName() + " must have a constructor with 3 arguments: org.w3c.dom.Element, Definition and  CMMNElementDefinition";
            getModelDefinition().fatalError(msg, e);
        } catch (SecurityException e) {
            String msg = "The class " + typeClass.getName() + " cannot be accessed due to a security exception";
            getModelDefinition().fatalError(msg, e);
        }
        return null;
    }

    /**
     * Simple helper function that parses all children of the element that have the specified tag name into an instance of the specified generic T. Additionally adds them to the Collection. In case the
     * generic T is String.class, it will simply add the text content of the specified tag to the list
     *
     * @param childTagName
     * @param typeClass
     * @param tCollection
     * @return
     */
    protected <T> void parse(String childTagName, Class<? extends T> typeClass, Collection<T> tCollection) {
        Collection<Element> namedChildren = XMLHelper.getChildrenWithTagName(element, childTagName);
        for (Element child : namedChildren) {
            T t = instantiateT(child, typeClass);
            tCollection.add(t);
        }
    }

    /**
     * Parses all elements within the extension element that have the specified name, and adds them to the collection.
     *
     * @param childTagName
     * @param typeClass
     * @param tCollection
     */
    protected <T> void parseExtension(String childTagName, Class<? extends T> typeClass, Collection<T> tCollection) {
        parseGrandChildren(EXTENSIONELEMENTS, childTagName, typeClass, tCollection);
    }

    /**
     * Parses the first child element within the extension element that has the specified tagname, and tries to instantiate it into the typeClass.
     *
     * @param childTagName
     * @param typeClass
     * @return
     */
    protected <T> T parseExtension(String childTagName, Class<? extends T> typeClass) {
        return parseGrandChild(EXTENSIONELEMENTS, childTagName, typeClass);
    }

    /**
     * Simple helper function that searches for the child element with the specified tagname, and then finds the first
     * grand child inside that element, and instantiates it into the specified type class
     *
     * @param childTagName
     * @param grandChildName
     * @param typeClass
     */
    private <T> T parseGrandChild(String childTagName, String grandChildName, Class<? extends T> typeClass) {
        Element child = XMLHelper.findElement(element, childTagName);
        if (child != null) {
            Element grandChild = XMLHelper.findElement(element, grandChildName);
            if (grandChild != null) {
                T t = instantiateT(grandChild, typeClass);
                return t;
            }
        }
        return null;
    }

    /**
     * Simple helper function that searches for the child element with the specified tagname, and then parses it's children into the specified type class
     * and adds them to the collection
     *
     * @param childTagName
     * @param grandChildName
     * @param typeClass
     * @param tCollection
     */
    protected <T> void parseGrandChildren(String childTagName, String grandChildName, Class<? extends T> typeClass, Collection<T> tCollection) {
        Element child = XMLHelper.findElement(element, childTagName);
        if (child != null) {
            Collection<Element> namedGrandChildren = XMLHelper.getChildrenWithTagName(child, grandChildName);
            for (Element grandChild : namedGrandChildren) {
                T t = instantiateT(grandChild, typeClass);
                tCollection.add(t);
            }
        }
    }

    /**
     * Simple helper function that parses all children of the element that have the specified tag name into an instance of the specified generic T. Additionally puts them in the Map, based on the
     * &lt;name&gt; element inside the child. E.g., Multiple Task definitions within the case plan, each having their own name, will be put in the Dictionary for easy lookup later.
     *
     * @param childTagName
     * @param typeClass
     * @param tMap
     * @return
     */
    protected <T extends XMLElementDefinition> void parse(String childTagName, Class<? extends T> typeClass, Map<String, T> tMap) {
        Collection<Element> namedChildren = XMLHelper.getChildrenWithTagName(element, childTagName);
        for (Element child : namedChildren) {
            T t = instantiateT(child, typeClass);
            if (t.getName().isEmpty()) {
                getModelDefinition().addDefinitionError("The element does not have a name, but it is required in order to be able to look it up\n" + XMLHelper.printXMLNode(child));
            }
            tMap.put(t.getName(), t);
        }
    }

    /**
     * Simple helper function that parses the first child of element that has the specified tag name into an instance of the specified generic T. Throws an exception if the tag is required, but cannot
     * be found. In case the generic T is String.class, it will simply return the text content of the specified tag.
     *
     * @param childTagName
     * @param typeClass
     * @param presenceRequired
     * @return
     */
    protected <T> T parse(String childTagName, Class<? extends T> typeClass, boolean presenceRequired) {
        if (typeClass.equals(String.class)) {
            String value = XMLHelper.getContent(element, childTagName, null);
            if (presenceRequired && value == null) {
                getModelDefinition().addDefinitionError("A '" + childTagName + "' cannot be found in the element " + printElement() + ", but it is required");
            }
            @SuppressWarnings("unchecked") // well ... we just checked that T is a String, right?
            T t = (T) value;
            return t;
        }
        if (element == null) {
            if (presenceRequired) {
                getModelDefinition().addDefinitionError("A '" + childTagName + "' cannot be found in the class " + getClass().getName() + ", because there is no XML element defined");
            }
            return null;
        }
        Element child = XMLHelper.getElement(element, childTagName);
        if (child != null) {
            return instantiateT(child, typeClass);
        } else if (presenceRequired) {
            getModelDefinition().addDefinitionError("A '" + childTagName + "' cannot be found in the element " + printElement() + ", but it is required");
        }
        return null;
    }

    protected String printElement() {
        String xml = XMLHelper.printXMLNode(element.cloneNode(false));
        xml = xml.replace("\n", "");
        xml = xml.replace("\r", "");
        return xml;
    }

    /**
     * Searches for the extension with the specified name, and parse the extension definition tag <code>&lt;extensionDefinition&gt;<code>.
     *
     * @param elementName
     * @param typeClass
     * @param presenceRequired
     * @return
     */
    public <T> T getExtension(String elementName, Class<? extends T> typeClass, boolean presenceRequired) {
        Element implementationElement = getExtension(elementName, presenceRequired);
        if (implementationElement == null) {
            return null;
        }

        String implementationClassName = implementationElement.getAttribute("class");
        if (implementationClassName.isEmpty()) {
            if (presenceRequired) {
                getModelDefinition().addDefinitionError("A custom " + elementName + " tag does not contain the class attribute in " + printElement() + ", but it is required");
                return null;
            }
            return null;
        }
        try {
            Class<?> implementationClass = Class.forName(implementationClassName);
            if (!typeClass.isAssignableFrom(implementationClass)) {
                throw new RuntimeException("The implementation class " + implementationClassName + " must implement " + typeClass.getName() + ", but it does not");
            }
            @SuppressWarnings("unchecked") // Well, we just did, right?
                    T implementationObject = (T) instantiateT(implementationElement, implementationClass);
            return implementationObject;
        } catch (ClassNotFoundException e) {
            String msg = "Cannot find class to parse the custom " + elementName + " - " + implementationClassName;
            getModelDefinition().fatalError(msg, e);
        } catch (SecurityException e) {
            String msg = "The class " + implementationClassName + " cannot be accessed due to a security exception";
            getModelDefinition().fatalError(msg, e);
        }
        // Means we ran into an exception
        return null;
    }

    /**
     * Returns the content of an extension element that has the specified name and namespace.
     * If namespaceURI is null, then the full tagname will be searched for.
     *
     * @param elementName
     * @param presenceRequired If presence is required, and the extensionElements tag is not available or the
     *                         specific tag is not found under it, then an error will be added to the definitions document.
     * @return
     */
    public Element getExtension(String elementName, boolean presenceRequired) {
        Element extensionsElement = XMLHelper.getElement(element, EXTENSIONELEMENTS);

        if (extensionsElement == null) {
            if (presenceRequired) {
                getModelDefinition().addDefinitionError("'" + EXTENSIONELEMENTS + "' tag is not found in " + printElement() + ", but it is required");
                return null;
            }
            return null;
        }

        Element implementationElement = null;
        if (elementName != null && !elementName.isEmpty()) {
            implementationElement = XMLHelper.getElementNS(extensionsElement, NAMESPACE_URI, elementName);
        }

        if (implementationElement == null && presenceRequired) {
            getModelDefinition().addDefinitionError("A custom " + elementName + " tag is not found in " + printElement() + "/" + EXTENSIONELEMENTS + " in " + NAMESPACE_URI + " namespace, but it is required");
        }

        return implementationElement;
    }

    /**
     * Parses the attribute name into a string, or provides the default value if the attribute is not found.
     *
     * @param attributeName
     * @param presenceRequired
     * @param defaultValue
     * @return
     */
    protected String parseAttribute(String attributeName, boolean presenceRequired, String... defaultValue) {
        if (element != null && element.hasAttribute(attributeName)) {
            return element.getAttribute(attributeName);
        }

        // If the attribute is required, add an error message
        if (presenceRequired) {
            if (element == null) {
                getModelDefinition().addDefinitionError("The attribute " + attributeName + " cannot be found, because an XML element is missing in " + this.getType());
            } else {
                getModelDefinition().addDefinitionError("The attribute " + attributeName + " is missing from the element " + XMLHelper.printXMLNode(element));
            }
        }

        // Return the (optional) default value or an empty string
        if (defaultValue.length > 0) {
            return defaultValue[0];
        } else {
            return "";
        }
    }

    public String getName() {
        return "";
    }

}
