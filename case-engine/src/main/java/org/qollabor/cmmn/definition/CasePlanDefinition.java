/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.definition;

import org.qollabor.cmmn.definition.sentry.ExitCriterionDefinition;
import org.qollabor.cmmn.instance.Case;
import org.qollabor.cmmn.instance.CasePlan;
import org.qollabor.cmmn.instance.Stage;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;

public class CasePlanDefinition extends StageDefinition implements ItemDefinition {
    private final Collection<ExitCriterionDefinition> exitCriteria = new ArrayList(); // Only in the root stage

    public CasePlanDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement);
        super.parse("exitCriterion", ExitCriterionDefinition.class, this.exitCriteria);
    }

    @Override
    public CasePlan createInstance(String id, int index, ItemDefinition itemDefinition, Stage stage, Case caseInstance) {
        return new CasePlan(id, this, caseInstance);
    }

    @Override
    public ItemControlDefinition getPlanItemControl() {
        return getDefaultControl();
    }

    @Override
    public PlanItemDefinitionDefinition getPlanItemDefinition() {
        return this;
    }

    @Override
    public Collection<ExitCriterionDefinition> getExitCriteria() {
        return exitCriteria;
    }
}
