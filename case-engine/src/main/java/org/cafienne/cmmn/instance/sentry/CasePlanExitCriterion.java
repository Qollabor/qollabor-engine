package org.cafienne.cmmn.instance.sentry;

import org.cafienne.cmmn.akka.event.debug.SentryEvent;
import org.cafienne.cmmn.definition.sentry.CasePlanExitCriterionDefinition;
import org.cafienne.cmmn.instance.PlanItem;
import org.cafienne.cmmn.instance.Stage;
import org.cafienne.cmmn.instance.Transition;

public class CasePlanExitCriterion extends ExitCriterion {
    private final PlanItem casePlan;
    
    public CasePlanExitCriterion(Stage<?> stage, CasePlanExitCriterionDefinition definition) {
        super(stage, definition);
        this.casePlan = stage.getPlanItem();
    }
    
    @Override
    protected void satisfy() {
        addDebugInfo(SentryEvent.class, event -> event.addMessage("Case plan exit criterion is satisfied and will terminate the plan", this.sentry));
        casePlan.makeTransition(Transition.Terminate);
    }
}
