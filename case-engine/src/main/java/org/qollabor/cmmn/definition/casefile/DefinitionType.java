package org.qollabor.cmmn.definition.casefile;

import org.qollabor.cmmn.definition.casefile.definitiontype.UnknownType;
import org.qollabor.cmmn.definition.casefile.definitiontype.UnspecifiedType;
import org.qollabor.cmmn.definition.casefile.definitiontype.XMLComplexType;
import org.qollabor.cmmn.definition.casefile.definitiontype.XMLElementType;
import org.qollabor.cmmn.definition.casefile.definitiontype.XMLSimpleType;
import org.qollabor.akka.actor.serialization.json.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of CMMN spec 5.1.4
 */
public class DefinitionType {
    private final static Logger logger = LoggerFactory.getLogger(DefinitionType.class);

    public static DefinitionType resolveDefinitionType(String uri) {
        if (uri.equals("http://www.omg.org/spec/CMMN/DefinitionType/CMISFolder")) {
            // Not implemented
        } else if (uri.equals("http://www.omg.org/spec/CMMN/DefinitionType/CMISDocument")) {
            // Not implemented
        } else if (uri.equals("http://www.omg.org/spec/CMMN/DefinitionType/CMISRelationship")) {
            // Not implemented
        } else if (uri.equals("http://www.omg.org/spec/CMMN/DefinitionType/XSDElement")) {
            return new XMLElementType();
        } else if (uri.equals("http://www.omg.org/spec/CMMN/DefinitionType/XSDComplexType")) {
            return new XMLComplexType();
        } else if (uri.equals("http://www.omg.org/spec/CMMN/DefinitionType/XSDSimpleType")) {
            return new XMLSimpleType();
        } else if (uri.equals("http://www.omg.org/spec/CMMN/DefinitionType/WSDLMessage")) {
            // Not implemented
        } else if (uri.equals("http://www.omg.org/spec/CMMN/DefinitionType/UMLClass")) {
            // Not implemented
        } else if (uri.equals("http://www.omg.org/spec/CMMN/DefinitionType/Unknown")) {
            return new UnknownType();
        } else if (uri.equals("http://www.omg.org/spec/CMMN/DefinitionType/Unspecified")) {
            return new UnspecifiedType();
        }
        logger.debug("Creating UnknownType wrapper for unrecognized type "+uri);
        return new UnknownType(uri);
    }
    
    /**
     * Validate the given value for the case file item. DefinitionType may use the case file item's definition
     * to determine whether the value can be set within the case file item.
     * The default implementation is empty - anything goes. Subclasses can override this to do type specific validations.
     * @param item
     * @param value
     * @param onlyProperties
     */
    public void validate(CaseFileItemDefinition item, Value value, boolean onlyProperties) throws CaseFileError {
    }
}
