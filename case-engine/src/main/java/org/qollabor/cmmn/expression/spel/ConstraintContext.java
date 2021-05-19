/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.expression.spel;

import org.qollabor.cmmn.definition.ApplicabilityRuleDefinition;
import org.qollabor.cmmn.definition.ConstraintDefinition;
import org.qollabor.cmmn.definition.DiscretionaryItemDefinition;
import org.qollabor.cmmn.definition.sentry.IfPartDefinition;
import org.qollabor.cmmn.instance.*;
import org.qollabor.cmmn.instance.casefile.CaseFileItem;
import org.qollabor.cmmn.instance.casefile.CaseFileItemArray;
import org.qollabor.cmmn.instance.sentry.Criterion;

/**
 * A ConstraintContext provides named context to expressions that are executed
 * from within Constraints (see {@link ApplicabilityRuleContext}, {@link IfPartContext} and {@link PlanItemContext}).
 * Constraints can take a CaseFileItem as context, and the name of this CaseFileItem can be used in the expression.
 * <p></p>
 * Furthermore we have the following properties available:
 * <ul>
 *     <li><code>[case file item name]</code> - reference to the case file item content; E.g. if the context of an IfPart is a Case File Item named <code>Customer</code>, having a property <code>"name"</code> then the
 *     if part expression can contain something like <code>Customer.name == "Smith"</code></li>
 *     <li><code>caseFileItem</code> - reference to the {@link CaseFileItem} holding the context to the actual value. An example of caseFileItem usage can be an <code>Order</code> with multiple <code>Line</code> items in it. E.g. a sentry that only activates
 *     if there are more than 3 Lines available could look like <code>caseFileItem.current().index &gt; 2</code>. <p></p>
 *     Note that in case of a cardinality * the expression <code>caseFileItem</code> refers to the {@link CaseFileItemArray}, and NOT to the specific instance that is mentioned in the previous bullet.<br/>
 *     This enables expressions like <code>caseFileItem.size()</code> to find out the number of case file items in the array. <br/>
 *     Note further that using <code>[case file item name]</code> in an expression is equivalent to <code>caseFileItem.current.value</code>
 *     </li>
 *     <li><code>definition</code> - reference to the {@link ConstraintDefinition} holding the expression (e.g. a repetition rule or a required rule)</li>
 *     <li><code>caseInstance</code> - direct reference to the case instance, see {@link ExpressionContext} for examples</li>
 * </ul>
 *
 * <p>
 * See also {@link ExpressionContext}
 * @param <T> A generic referencing the definition of the constraint; can be accessed in the expression through <code>definition</code>
 */
class ConstraintContext<T extends ConstraintDefinition> extends ExpressionContext {
    protected ConstraintContext(T constraintDefinition, Case caseInstance) {
        super(caseInstance);
        CaseFileItem item = constraintDefinition.resolveContext(caseInstance);
        if (constraintDefinition.getContext() != null) {
            super.addPropertyReader(constraintDefinition.getContext().getName(), () -> item.getCurrent().getValue());
        }
        super.addPropertyReader("definition", () -> constraintDefinition);
        super.addPropertyReader("caseFileItem", () -> item);
    }
}

/**
 * Context of current plan item. Can be referred to by it's type (task, stage, milestone or event), or by plain "planitem".
 * Used for evalution of item control rules (required, repetition, manual activation), and for
 * custom HumanTask settings on Assignment and DueDate.
 */
class PlanItemContext<T extends ConstraintDefinition> extends ConstraintContext<T> {
    protected PlanItemContext(T constraint, PlanItem planItem) {
        super(constraint, planItem.getCaseInstance());
        String planItemType =
            planItem instanceof Task ? "task" : // It is either a Task (human-, process- or casetask, but in all cases "task")
            planItem instanceof Stage ? "stage" : // or a Stage (caseplan or stage)
            planItem instanceof Milestone ? "milestone" : // or a Milestone
            "event"; // or an event listener (timer event, user event)
        addPropertyReader("planItem", () -> planItem);
        addPropertyReader(planItemType, () -> planItem);
    }
}

/**
 * Applicability rules are executed on discretionary items related to a Stage or HumanTask.
 * This context provides the additional information with the properties <code>planItem</code> and <code>discretionaryItem</code>.
 */
class ApplicabilityRuleContext extends PlanItemContext<ApplicabilityRuleDefinition> {
    protected ApplicabilityRuleContext(PlanItem planItem, DiscretionaryItemDefinition itemDefinition, ApplicabilityRuleDefinition ruleDefinition) {
        super(ruleDefinition, planItem);
        addPropertyReader("discretionaryItem", () -> itemDefinition);
    }
}

/**
 * Context for evaluation of an if part in a criterion.
 */
class IfPartContext extends ConstraintContext<IfPartDefinition> {
    protected IfPartContext(IfPartDefinition ifPartDefinition, Criterion criterion) {
        super(ifPartDefinition, criterion.getCaseInstance());
        addPropertyReader("criterion", () -> criterion);
        addDeprecatedReader("sentry", "criterion", () -> criterion); // Compatibility
    }
}
