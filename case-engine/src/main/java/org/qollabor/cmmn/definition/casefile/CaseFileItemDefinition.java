/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.definition.casefile;

import java.util.Collection;

import org.qollabor.cmmn.definition.CMMNElementDefinition;
import org.qollabor.cmmn.definition.ModelDefinition;
import org.qollabor.cmmn.definition.Multiplicity;
import org.qollabor.cmmn.instance.Case;
import org.qollabor.cmmn.instance.casefile.CaseFileItem;
import org.qollabor.cmmn.instance.casefile.CaseFileItemArray;
import org.qollabor.cmmn.instance.casefile.CaseFileItemCollection;
import org.qollabor.cmmn.instance.casefile.Path;
import org.qollabor.akka.actor.serialization.json.Value;
import org.w3c.dom.Element;

public class CaseFileItemDefinition extends CaseFileItemCollectionDefinition {
    private final Multiplicity multiplicity;
    private final String definitionRef;
    private final String sourceRef;
    private final String targetRefs;
    private CaseFileItemDefinitionDefinition typeDefinition;

    public CaseFileItemDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement);
        this.multiplicity = readMultiplicity();
        this.definitionRef = parseAttribute("definitionRef", true, "");
        this.sourceRef = parseAttribute("sourceRef", false, "");
        this.targetRefs = parseAttribute("targetRefs", false, "");
        parseGrandChildren("children", "caseFileItem", CaseFileItemDefinition.class, getChildren());
    }

    private Multiplicity readMultiplicity() {
        String multiplicityString = parseAttribute("multiplicity", false, "Unspecified");
        try {
            return Multiplicity.valueOf(multiplicityString);
        } catch (IllegalArgumentException iae) {
            getCaseDefinition().addDefinitionError(multiplicityString + " is not a valid multiplicity (in CaseFileItem " + getName() + ")");
            return null;
        }
    }

    @Override
    protected void resolveReferences() {
        super.resolveReferences();

        this.typeDefinition = getCaseDefinition().getDefinitionsDocument().getCaseFileItemDefinition(this.definitionRef);
        if (this.typeDefinition == null) {
            getModelDefinition().addReferenceError("The case file item '" + this.getName() + "' refers to a definition named " + definitionRef + ", but that definition is not found");
            return; // Avoid further checking on this element
        }

        // Resolve source ...
        if (!sourceRef.isEmpty()) {
        }

        // Resolve targets ...
        if (!targetRefs.isEmpty()) {
        }
    }

    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    public CaseFileItemDefinitionDefinition getCaseFileItemDefinition() {
        return typeDefinition;
    }


    @Override
    public boolean isUndefined(String identifier) {
        return super.isUndefined(identifier) && !getCaseFileItemDefinition().getProperties().containsKey(identifier);
    }

    /**
     * Returns a path to this case file item definition.
     *
     * @return
     */
    public Path getPath() {
        return new Path(this);
    }

    /**
     * Creates a new case file item, based on this definition.
     *
     * @param caseInstance
     * @param parent
     * @return
     */
    public CaseFileItem createInstance(Case caseInstance, CaseFileItemCollection<?> parent) {
        if (multiplicity.isIterable()) {
            return new CaseFileItemArray(caseInstance, this, parent);
        } else {
            return new CaseFileItem(caseInstance, this, parent);
        }
    }

    /**
     * Recursively validates the potential value against this definition;
     * Checks whether the potential value matches the CaseFileItemDefinitionDefinition;
     * and, if there are children in the value, then also matches those children against our children.
     * @param value
     */
    public void validate(Value value) throws CaseFileError {
        getCaseFileItemDefinition().getDefinitionType().validate(this, value, false);
    }

    public void validatePropertyTypes(Value value) throws CaseFileError {
        getCaseFileItemDefinition().getDefinitionType().validate(this, value, true);
    }

    /**
     * Returns a collection with the business identifiers of this case file item. Can be empty.
     * @return
     */
    public Collection<PropertyDefinition> getBusinessIdentifiers() {
        return getCaseFileItemDefinition().getBusinessIdentifiers();
    }
}
