/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.expression.spel;

import org.qollabor.akka.actor.ModelActor;
import org.qollabor.akka.actor.serialization.json.LongValue;
import org.qollabor.akka.actor.serialization.json.StringValue;
import org.qollabor.akka.actor.serialization.json.Value;
import org.qollabor.cmmn.definition.*;
import org.qollabor.cmmn.definition.parameter.InputParameterDefinition;
import org.qollabor.cmmn.definition.parameter.ParameterDefinition;
import org.qollabor.cmmn.definition.sentry.IfPartDefinition;
import org.qollabor.cmmn.definition.task.AssignmentDefinition;
import org.qollabor.cmmn.definition.task.DueDateDefinition;
import org.qollabor.cmmn.expression.CMMNExpressionEvaluator;
import org.qollabor.cmmn.expression.InvalidExpressionException;
import org.qollabor.cmmn.instance.*;
import org.qollabor.cmmn.instance.sentry.Criterion;
import org.qollabor.cmmn.instance.task.humantask.HumanTask;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class ExpressionEvaluator implements CMMNExpressionEvaluator {
    private final ExpressionParser parser;
    private final Expression spelExpression;
    private final CaseFileAccessor caseFileAccessor = new CaseFileAccessor();
    private final String expressionString;
    private final ExpressionDefinition expressionDefinition;

    public ExpressionEvaluator(ExpressionDefinition expressionDefinition) {
        this.parser = new SpelExpressionParser();
        this.expressionString = expressionDefinition.getBody();
        this.expressionDefinition = expressionDefinition;
        this.spelExpression = parseExpression();
    }

    private Expression parseExpression() {
        try {
            return parser.parseExpression(expressionString);
        } catch (SpelParseException spe) {
            expressionDefinition.getModelDefinition().addDefinitionError(expressionDefinition.getContextDescription() + " has an invalid expression:\n" + spe.getMessage());
            return null;
        }
    }

    private <T> T evaluateConstraint(ModelActor caseInstance, Object contextObject, String ruleTypeDescription) {
        // System.out.println("Now evaluating the expression " + definition.getBody());

        StandardEvaluationContext context = new StandardEvaluationContext(contextObject);
        // The case file accessor can be used to dynamically resolve properties that belong to the case file
        context.addPropertyAccessor(caseFileAccessor);

        // TODO: improve the type checking and raise better error message if we're getting back the wrong type.

        try {
            caseInstance.addDebugInfo(() -> "Evaluating " + ruleTypeDescription + ": " + expressionString.trim());
            // Not checking it. If it fails, it really fails.
            @SuppressWarnings("unchecked")
            T value = (T) spelExpression.getValue(context);
            caseInstance.addDebugInfo(() -> "Outcome: " + value);
            return value;
        } catch (SpelEvaluationException invalidExpression) {
            caseInstance.addDebugInfo(() -> "Failure in evaluating "+ruleTypeDescription+", with expression "+expressionString.trim(), invalidExpression);
            throw new InvalidExpressionException("Could not evaluate " + spelExpression.getExpressionString() + "\n" + invalidExpression.getLocalizedMessage(), invalidExpression);
        }
    }

    private Value<?> evaluateParameterTransformation(ModelActor caseInstance, String parameterName, Value<?> parameterValue, Task<?> task) {
        ParameterContext contextObject = new ParameterContext(parameterName, parameterValue, caseInstance, task);
        Object result = evaluateConstraint(caseInstance, contextObject, "parameter transformation");
        return Value.convert(result);
    }

    @Override
    public Value<?> evaluateInputParameterTransformation(Case caseInstance, Parameter<InputParameterDefinition> from, ParameterDefinition to, Task<?> task) {
        return evaluateParameterTransformation(caseInstance, from.getDefinition().getName(), from.getValue(), task);
    }

    @Override
    public Value<?> evaluateOutputParameterTransformation(Case caseInstance, Value<?> value, ParameterDefinition rawOutputParameterDefinition, ParameterDefinition targetOutputParameterDefinition, Task<?> task) {
        return evaluateParameterTransformation(caseInstance, rawOutputParameterDefinition.getName(), value, task);
    }

    @Override
    public Value<?> evaluateOutputParameterTransformation(ModelActor caseInstance, Value<?> value, ParameterDefinition rawOutputParameterDefinition, ParameterDefinition targetOutputParameterDefinition, Task<?> task) {
//        System.err.println("Running an expression  on  " + expressionString);
        return evaluateParameterTransformation(caseInstance, rawOutputParameterDefinition.getName(), value, task);
    }

    @Override
    public Duration evaluateTimerExpression(TimerEvent timerEvent, TimerEventDefinition definition) {
        try {
            return Duration.parse(definition.getTimerExpression().getBody().trim());
        } catch (DateTimeParseException dtpe) {
            // Failed to do default parsing. Let's try to put a SPEL onto it....
        }
        // If the result is an actual Duration instance we are done. Otherwise we will try to parse the result as Duration
        Object result = evaluateConstraint(timerEvent.getCaseInstance(), timerEvent, "timer event duration");
        if (result instanceof Duration) {
            return (Duration) result;
        }
        // Apparently something else than a Duration, so we will just try to parse the string of it
        try {
            return Duration.parse(String.valueOf(result).trim());
        } catch (DateTimeParseException dtpe) {
            throw new InvalidExpressionException("The timer expression " + definition.getTimerExpression().getBody() + " in " + definition.getName() + " cannot be parsed into a Duration", dtpe);
        }
    }

    @Override
    public boolean evaluateItemControl(PlanItem planItem, ConstraintDefinition ruleDefinition) {
        return evaluateConstraint(planItem.getCaseInstance(), new PlanItemContext(ruleDefinition, planItem), ruleDefinition.getContextDescription());
    }

    @Override
    public boolean evaluateIfPart(Criterion criterion, IfPartDefinition ifPartDefinition) {
        Object outcome = evaluateConstraint(criterion.getCaseInstance(), new IfPartContext(ifPartDefinition, criterion), "ifPart in sentry");
        if (outcome instanceof Boolean) {
            return (boolean) outcome;
        } else {
            throw new InvalidExpressionException("The if part must result in true or false, however, it returns something of type " + outcome.getClass().getName(), new ClassCastException());
        }
    }

    @Override
    public boolean evaluateApplicabilityRule(PlanItem containingPlanItem, DiscretionaryItemDefinition discretionaryItemDefinition, ApplicabilityRuleDefinition ruleDefinition) {
        String description = "applicability rule '" + ruleDefinition.getName() + "' for discretionary item " + discretionaryItemDefinition;
        return evaluateConstraint(containingPlanItem.getCaseInstance(), new ApplicabilityRuleContext(containingPlanItem, discretionaryItemDefinition, ruleDefinition), description);
    }

    @Override
    public Instant evaluateDueDate(HumanTask task, DueDateDefinition definition) throws InvalidExpressionException {
        Object outcome = evaluateConstraint(task.getCaseInstance(), new PlanItemContext(definition, task), "due date expression");
        if (outcome == null || outcome == Value.NULL) {
            return null;
        }
        if (outcome instanceof Instant) {
            return (Instant) outcome;
        }
        if (outcome instanceof java.util.Date) {
            return ((java.util.Date) outcome).toInstant();
        }
        if (outcome instanceof java.sql.Date) {
            return ((java.sql.Date) outcome).toInstant();
        }
        if (outcome instanceof Long) {
            return Instant.ofEpochMilli((Long)outcome);
        }
        if (outcome instanceof LongValue) {
            return Instant.ofEpochMilli(((LongValue)outcome).getValue());
        }
        if (outcome instanceof String) {
            try {
                return Instant.parse(outcome.toString());
            } catch (DateTimeParseException e) {
                throw new InvalidExpressionException("Cannot parse string '"+outcome+"' to Instant: " + e.getMessage(), e);
            }
        }
        if (outcome instanceof StringValue) {
            try {
                return Instant.parse(((StringValue) outcome).getValue());
            } catch (DateTimeParseException e) {
                throw new InvalidExpressionException("Cannot parse string '"+outcome+"' to Instant: " + e.getMessage(), e);
            }
        }
        throw new InvalidExpressionException("Outcome of due date expression cannot be interpreted as a due date, it is of type "+outcome.getClass().getName());
    }

    @Override
    public String evaluateAssignee(HumanTask task, AssignmentDefinition definition) throws InvalidExpressionException {
        Object outcome = evaluateConstraint(task.getCaseInstance(), new PlanItemContext(definition, task), "assignment expression");
        if (outcome == null || outcome == Value.NULL) {
            return null;
        }
        return outcome.toString();
    }
}
