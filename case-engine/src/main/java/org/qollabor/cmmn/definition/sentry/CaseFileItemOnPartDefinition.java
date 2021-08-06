/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.definition.sentry;

import org.qollabor.cmmn.definition.CMMNElementDefinition;
import org.qollabor.cmmn.definition.ModelDefinition;
import org.qollabor.cmmn.definition.casefile.CaseFileItemDefinition;
import org.qollabor.cmmn.instance.sentry.CaseFileItemOnPart;
import org.qollabor.cmmn.instance.casefile.CaseFileItemTransition;
import org.qollabor.cmmn.instance.sentry.Criterion;
import org.w3c.dom.Element;

public class CaseFileItemOnPartDefinition extends OnPartDefinition {
    private CaseFileItemTransition standardEvent;
    private final String sourceRef;
    private CaseFileItemDefinition source;

    public CaseFileItemOnPartDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement);
        String standardEventName = parse("standardEvent", String.class, true);
        standardEvent = CaseFileItemTransition.getEnum(standardEventName);
        if (standardEvent == null) {
            getCaseDefinition().addDefinitionError("A standard event named " + standardEventName + " does not exist for case file items");
        }
        sourceRef = parseAttribute("sourceRef", true);
    }

    @Override
    protected void resolveReferences() {
        super.resolveReferences();
        // Add a check that the source only has to be filled if the standardEvent is specified
        source = getCaseDefinition().findCaseFileItem(sourceRef);
        if (source == null) {
            getModelDefinition().addReferenceError("A case file item with name '" + sourceRef + "' is referenced from sentry " + getParentElement().getName() + ", but it does not exist in the case file model");
        }
    }

    @Override
    public String getContextDescription() {
        return source.getType() +"["+ source.getPath()+"]." + standardEvent;
    }

    public CaseFileItemTransition getStandardEvent() {
        return standardEvent;
    }

    @Override
    public CaseFileItemDefinition getSourceDefinition() {
        return source;
    }

    @Override
    public CaseFileItemOnPart createInstance(Criterion criterion) {
        return new CaseFileItemOnPart(criterion, this);
    }
}