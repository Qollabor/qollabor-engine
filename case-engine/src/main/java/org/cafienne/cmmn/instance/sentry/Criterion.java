package org.cafienne.cmmn.instance.sentry;

import org.cafienne.cmmn.definition.CMMNElementDefinition;
import org.cafienne.cmmn.definition.sentry.CriterionDefinition;
import org.cafienne.cmmn.definition.sentry.OnPartDefinition;
import org.cafienne.cmmn.instance.*;
import org.cafienne.cmmn.instance.casefile.ValueList;
import org.cafienne.cmmn.instance.casefile.ValueMap;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Criterion<D extends CriterionDefinition> extends CMMNElement<D> {
    protected final Stage<?> stage;

    // On parts are stored by their source for easy lookup.
    // The source can only be a PlanItemDefinition or a CaseFileItemDefinition. We have taken the first
    // level parent class (CMMNElementDefinition) for this. So, technically we might also store on parts
    // with a different type of key ... but the logic prevents this from happening.
    private final Map<CMMNElementDefinition, OnPart> onParts = new LinkedHashMap();

    /**
     * Simple set to be able to quickly check whether the criterion may become active
     */
    private final Set<OnPart> inactiveOnParts = new HashSet();

    boolean isActive;

    protected Criterion(Stage stage, D definition) {
        super(stage, definition);
        this.stage = stage;
        for (OnPartDefinition onPartDefinition : getDefinition().getSentryDefinition().getOnParts()) {
            OnPart onPart = onPartDefinition.createInstance(this);
            onParts.put(onPartDefinition.getSourceDefinition(), onPart);
            inactiveOnParts.add(onPart);
        }
    }

    public Stage getStage() {
        return stage;
    }
    private boolean evaluateIfPart() {
        addDebugInfo(() -> "Evaluating if part in " + this);
        boolean ifPartOutcome = getDefinition().getSentryDefinition().getIfPart().evaluate(this);
        // TODO: make sure to store the outcome of the ifpart evaluation?
        return ifPartOutcome;
    }

    /**
     * Whenever an on part is satisfied, it will try
     * to activate the Sentry.
     */
    void activate(OnPart<?, ?> activator) {
        inactiveOnParts.remove(activator);
        if (inactiveOnParts.isEmpty()) {
            addDebugInfo(() -> this + " has become active");
        } else {
            addDebugInfo(() -> this + " has "+inactiveOnParts.size()+" remaining inactive on parts", this);
        }
        if (isSatisfied()) {
            isActive = true;
            satisfy(activator);
            // isActive = false;
        }
    }

    /**
     * If the on part is no longer satisfied,
     * it will dissatisfy the criterion too.
     */
    void deactivate(OnPart<?, ?> activator) {
        isActive = false;
        inactiveOnParts.add(activator);
        addDebugInfo(() -> this + " now has "+inactiveOnParts.size()+" inactive on parts", this);
    }

    protected abstract void satisfy(OnPart<?, ?> activator);

    public abstract void addPlanItem(PlanItem planItem);

    public boolean isActive() {
        return isActive;
    }

    public boolean isSatisfied() {
        // A Criterion is satisfied when one of the following conditions is satisfied:
        // 1 All of the onParts are satisfied AND the ifPart condition evaluates to "true".
        // 2 All of the onParts are satisfied AND there is no ifPart.
        // 3 The ifPart condition evaluates to "true" AND there are no onParts.

        if (onParts.isEmpty()) { // number 3
            return evaluateIfPart();
        } else { // numbers 1 and 2
            return inactiveOnParts.isEmpty() && evaluateIfPart();
        }
    }

    @Override
    public D getDefinition() {
        return super.getDefinition();
    }

    /**
     * Whether this is an activating or terminating criterion. This information is important for the order in which sentries are triggered.
     *
     * @return
     */
    public abstract boolean isEntryCriterion();

    @Override
    public String toString() {
        boolean activated = isActive();

        String listeners = onParts.values().stream().map(part -> part.toString()).collect(Collectors.joining(","));
        return getDefinition().getType() + " for " + getDefinition().getPlanItemName() + " on " + "[" + listeners + "] - " + (activated ? "active" : "inactive");
    }

    public void connectToSentryNetwork() {
        // Make ourselves known to the global case so that other plan items can start informing us.
        getCaseInstance().getSentryNetwork().add(this);
        // Tell our onparts to connect to the case network
        onParts.values().forEach(onPart -> onPart.connectToCase());
    }

    /**
     * Connects to the plan item if there is an on part in the criterion that matches the plan item definition.
     * Skips plan items that belong to sibling stages.
     *
     * @param planItem
     */
    void establishPotentialConnection(PlanItem planItem) {
        PlanItemOnPart onPart = (PlanItemOnPart) onParts.get(planItem.getItemDefinition());
        if (onPart != null) {
            onPart.connect(planItem);
        }
    }

    /**
     * Connects to the case file item if there is an on part in the criterion that matches the case file item definition.
     */
    void establishPotentialConnection(CaseFileItem caseFileItem) {
        CaseFileItemOnPart onPart = (CaseFileItemOnPart) onParts.get(caseFileItem.getDefinition());
        if (onPart != null) {
            onPart.connect(caseFileItem);
        }
    }


    public Element dumpMemoryStateToXML(Element parentElement, boolean showConnectedPlanItems) {
        Element sentryXML = parentElement.getOwnerDocument().createElement("Sentry");
        parentElement.appendChild(sentryXML);
        sentryXML.setAttribute("name", getDefinition().getName());
        sentryXML.setAttribute("id", getDefinition().getId());
        sentryXML.setAttribute("active", "" + inactiveOnParts.isEmpty());
        if (!showConnectedPlanItems) {
            String targetPlanItemName = getDefinition().getTarget();
            Transition targetTransition = getDefinition().getTransition();
            if (targetPlanItemName == null) {
                sentryXML.setAttribute("target", stage.getItemDefinition().getName() + "." + targetTransition);
            } else {
                sentryXML.setAttribute("target", targetPlanItemName + "." + targetTransition);
            }
        } else {
            sentryXML.setAttribute("stage", this.stage.getItemDefinition().getName());
        }

        this.onParts.forEach((d, onPart) -> {
            onPart.dumpMemoryStateToXML(sentryXML, showConnectedPlanItems);
        });

        return sentryXML;
    }

    public ValueMap toJson() {
        ValueList onPartsJson = new ValueList();
        this.onParts.forEach((d, onPart) -> onPartsJson.add(onPart.toJson()));
        return new ValueMap(
                "target", this.toString(),
                "active", inactiveOnParts.isEmpty(),
                "name", this.getDefinition().getName(),
                "id", this.getDefinition().getId(),
                "on-parts", onPartsJson);
    }

}