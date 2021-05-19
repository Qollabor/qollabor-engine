/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.definition;

import org.qollabor.cmmn.definition.casefile.CaseFileItemDefinition;
import org.qollabor.cmmn.expression.DefaultValueEvaluator;
import org.qollabor.cmmn.instance.Case;
import org.qollabor.cmmn.instance.casefile.CaseFileItem;
import org.qollabor.cmmn.instance.casefile.Path;
import org.qollabor.cmmn.instance.PlanItem;
import org.w3c.dom.Element;

public class ConstraintDefinition extends CMMNElementDefinition {
    private final ExpressionDefinition expression;
    private final String contextRef;
    private CaseFileItemDefinition context;
    private Path pathToContext;

    protected ConstraintDefinition(ModelDefinition definition, CMMNElementDefinition parentElement, boolean defaultValue) {
        super(null, definition, parentElement);
        this.expression = new ExpressionDefinition(definition, parentElement, defaultValue);
        this.contextRef = "";
    }

    public ConstraintDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement);
        expression = parse("condition", ExpressionDefinition.class, true);
        this.contextRef = parseAttribute("contextRef", false);
    }

    @Override
    protected void resolveReferences() {
        super.resolveReferences();
        if (contextRef.isEmpty()) {
            return;
        }
        context = getCaseDefinition().findCaseFileItem(contextRef);
        if (context == null) {
            getCaseDefinition().addReferenceError(getContextDescription() + " refers to a Case File item with id '" + contextRef + "', but the corresponding Case File item cannot be found.");
        } else {
            pathToContext = context.getPath();
        }
    }

    @Override
    public String getContextDescription() {
        String parentType = getParentElement().getType();
        String parentId = getParentElement().getId();
        // This will return something like "The required rule in HumanTask 'abc'
        return "The " + getType() + " in " + parentType + " '" + parentId + "'";
    }

    /**
     * Returns the definition of the case file item context (if any) for this constraint.
     *
     * @return
     */
    public CaseFileItemDefinition getContext() {
        return context;
    }

    /**
     * Resolves the case file item context on the specified case instance. Returns null if the context is not specified.
     *
     * @param caseInstance
     * @return
     */
    public CaseFileItem resolveContext(Case caseInstance) {
        if (context == null) {
            return null;
        }
        CaseFileItem caseFileContainer = pathToContext.resolve(caseInstance);
        return caseFileContainer.getCurrent();
    }

    public ExpressionDefinition getExpressionDefinition() {
        return expression;
    }

    public boolean evaluate(PlanItem planItem) {
        return expression.getEvaluator().evaluateItemControl(planItem, this);
    }

    public boolean isDefault() {
        return expression.getEvaluator() instanceof DefaultValueEvaluator;
    }

    /**
     * Returns the type of constraint, e.g. applicabilityRule, ifPart, repetitionRule, etc.
     *
     * @return
     */
    public String getType() {
        return getElement().getTagName();
    }
}
