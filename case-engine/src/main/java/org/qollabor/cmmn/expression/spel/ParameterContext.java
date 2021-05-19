package org.qollabor.cmmn.expression.spel;

import org.qollabor.akka.actor.ModelActor;
import org.qollabor.akka.actor.serialization.json.Value;
import org.qollabor.cmmn.instance.Case;
import org.qollabor.cmmn.instance.Task;

/**
 * Provides context for input/output transformation of parameters.
 * Can read the parameter name in the expression and resolve it to the parameter value.
 * Contains furthermore a task property, to provide for the task context for which this parameter transformation is being executed.
 */
class ParameterContext extends ExpressionContext {
    private final Object task;

    protected ParameterContext(String parameterName, Value<?> parameterValue, ModelActor caseInstance, Task<?> task) {
        super(caseInstance);
        // Note: task can point to ProcessTaskActor and also to ProcessTask, CaseTask or HumanTask
        if (caseInstance instanceof Case) {
            this.task = task;
        } else {
            this.task = caseInstance;
        }
        addPropertyReader(parameterName, () -> parameterValue);
        addPropertyReader("task", () -> this.task);
    }
}
