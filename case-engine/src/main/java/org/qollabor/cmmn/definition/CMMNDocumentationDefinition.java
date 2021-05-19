package org.qollabor.cmmn.definition;

import org.w3c.dom.Element;

/**
 * Basic implementation of &lt;cmmn:documentation&gt; element
 */
public class CMMNDocumentationDefinition extends XMLElementDefinition {
    public final String textFormat;
    public final String text;

    public CMMNDocumentationDefinition(Element element, ModelDefinition definition, CMMNElementDefinition parentElement) {
        super(element, definition, parentElement);
        this.textFormat = parseAttribute("textFormat", false, "text/plain");
        String text = parse("text", String.class, false);
        this.text = text == null ? "" : text;
    }

    /**
     * Constructor used to create an instance if the documentation node does not exist in the definition.
     * Also converts a potential CMMN1.0 description into the new documentation element.
     * Note: this does not modify the underlying XML!
     * @param definition
     * @param parentElement
     */
    CMMNDocumentationDefinition(ModelDefinition definition, CMMNElementDefinition parentElement) {
        super(null, definition, parentElement);
        this.textFormat = "text/plain";
        this.text = parentElement.parseAttribute("description", false, "");
    }

    /**
     * Returns text format
     * @return
     */
    public String getTextFormat() {
        return textFormat;
    }

    /**
     * Returns text
     * @return
     */
    public String getText() {
        return text;
    }
}
