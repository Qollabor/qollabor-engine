/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.definition;

import org.qollabor.cmmn.definition.sentry.EntryCriterionDefinition;
import org.qollabor.cmmn.definition.sentry.ExitCriterionDefinition;
import org.qollabor.cmmn.instance.DiscretionaryItem;
import org.qollabor.cmmn.instance.PlanItem;
import org.qollabor.cmmn.instance.Stage;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;

public class DiscretionaryItemDefinition extends TableItemDefinition implements ItemDefinition {
    private ItemControlDefinition planItemControl;
    private PlanItemDefinitionDefinition definition;
    private final Collection<EntryCriterionDefinition> entryCriteria = new ArrayList();
    private final Collection<ExitCriterionDefinition> exitCriteria = new ArrayList();
    private final String planItemDefinitionRefValue;

    public DiscretionaryItemDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement);
        this.planItemDefinitionRefValue = parseAttribute("definitionRef", true);

        parse("entryCriterion", EntryCriterionDefinition.class, this.entryCriteria);
        parse("exitCriterion", ExitCriterionDefinition.class, this.exitCriteria);

        planItemControl = parse("itemControl", ItemControlDefinition.class, false);

        // CMMN 1.0 spec page 32:
        // A DiscretionaryItem that is defined by a Task that is non-blocking (isBlocking set to "false") MUST NOT have exitCreteriaRefs.
        if (this.definition instanceof TaskDefinition) {
            if (!((TaskDefinition<?>) this.definition).isBlocking()) {
                if (!this.exitCriteria.isEmpty()) {
                    getCaseDefinition().addDefinitionError("The plan item " + getName() + " has exit sentries, but these are not allowed for a non blocking task");
                    return;
                }
            }
        }
    }

    public ItemControlDefinition getPlanItemControl() {
        return planItemControl;
    }

    public PlanItemDefinitionDefinition getPlanItemDefinition() {
        return definition;
    }

    public Collection<EntryCriterionDefinition> getEntryCriteria() {
        return entryCriteria;
    }

    public Collection<ExitCriterionDefinition> getExitCriteria() {
        return exitCriteria;
    }

    @Override
    public boolean isDiscretionary() {
        return true;
    }

    @Override
    protected void resolveReferences() {
        super.resolveReferences();
        this.definition = getCaseDefinition().findPlanItemDefinition(planItemDefinitionRefValue);
        if (this.definition == null) {
            getCaseDefinition().addReferenceError("The discretionary item '" + getName() + "' refers to a definition named '" + planItemDefinitionRefValue + "', but that definition is not found");
            return;
        }
        // If the discretionary item has no name, it has to be taken from the definition
        if (getName().isEmpty()) {
            setName(definition.getName());
        }
        if (planItemControl == null && this.definition != null) {
            // Create a default ItemControl
            planItemControl = this.definition.getDefaultControl();
        }
    }

    /**
     * Calculates whether the discretionary item is currently applicable within the context of 
     * the containing plan item. Note: the containing plan item must be of type Stage or HumanTask.
     *
     * @param containingPlanItem
     * @return
     */
    public boolean isApplicable(PlanItem containingPlanItem) {
        if (isAlreadyPlanned(containingPlanItem)) {
            return false;
        }
        if (getApplicabilityRules().isEmpty()) {
            containingPlanItem.getCaseInstance().addDebugInfo(() -> this + ": item is applicable because rules are not defined");
            return true;
        } else {
            containingPlanItem.getCaseInstance().addDebugInfo(() -> this + ": checking " + getApplicabilityRules().size() + " applicability rule(s)");
            for (ApplicabilityRuleDefinition rule : getApplicabilityRules()) {
                // If any of the rules evaluates to false, the discretionary item is not allowed
                if (!rule.evaluate(containingPlanItem, rule, this)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Checks if the discretionary item is already planned
     * @param containingPlanItem
     * @return
     */
    private boolean isAlreadyPlanned(PlanItem containingPlanItem) {
        // Go through all plan items in the containing stage, check if there is one with our name
        // and then check if it is not repeating. If not, then there is one, and we cannot add more, so then we are "already planned".
        Stage containingStage = containingPlanItem instanceof Stage ? (Stage) containingPlanItem : containingPlanItem.getStage();
        Collection<PlanItem> currentPlanItemsInStage = containingStage.getPlanItems();
        for (PlanItem planItem : currentPlanItemsInStage) {
            if (planItem.getName().equals(this.getName())) {
                if (!planItem.repeats()) {
                    return true;
                }
            }
        }
        // Not yet planned.
        return false;
    }

    @Override
    public Element dumpMemoryStateToXML(Element parentElement, Stage stage) {
        Element discretionaryXML = parentElement.getOwnerDocument().createElement("discretionaryItem");

        discretionaryXML.setAttribute("name", getName());
        // System.out.println("Dumping memory state for table item "+getName()+", having "+getApplicabilityRules().size()+" rules");
//        discretionaryXML.setAttribute("applicable", "" + isApplicable(stage.getPlanItem()));

        // Also print the roles.
        super.dumpMemoryStateToXML(discretionaryXML, stage);
        parentElement.appendChild(discretionaryXML);

        return discretionaryXML;
    }

    @Override
    public void evaluate(PlanItem containingPlanItem, Collection<DiscretionaryItem> items) {
        if (isApplicable(containingPlanItem)) {
            items.add(createInstance(containingPlanItem));
        }
    }

    /**
     * Create a new instance of this definition inside the specified parent.
     * @param parent The stage or task in which the discretionary item can be planned
     * @return
     */
    public DiscretionaryItem createInstance(PlanItem parent) {
        return new DiscretionaryItem(parent, this);
    }

    @Override
    protected DiscretionaryItemDefinition getDiscretionaryItem(String identifier) {
        if (getName().equals(identifier) || getId().equals(identifier)) {
            return this; // We're the one.
        }
        return null;
    }

    @Override
    public String toString() {
        if (definition == null) return super.toString();
        return "DiscretionaryItem[" + definition.getType() + " '" + getName() + "']";
    }
}
