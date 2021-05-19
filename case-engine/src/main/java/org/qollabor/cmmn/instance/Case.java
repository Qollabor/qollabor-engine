/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.instance;

import org.qollabor.akka.actor.ModelActor;
import org.qollabor.cmmn.akka.command.CaseCommand;
import org.qollabor.cmmn.akka.event.CaseEvent;
import org.qollabor.cmmn.akka.event.CaseModified;
import org.qollabor.cmmn.akka.event.DebugDisabled;
import org.qollabor.cmmn.akka.event.DebugEnabled;
import org.qollabor.cmmn.akka.event.plan.PlanItemCreated;
import org.qollabor.cmmn.definition.CaseDefinition;
import org.qollabor.cmmn.definition.CasePlanDefinition;
import org.qollabor.cmmn.definition.ItemDefinition;
import org.qollabor.cmmn.definition.PlanItemDefinitionDefinition;
import org.qollabor.cmmn.definition.parameter.InputParameterDefinition;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.cmmn.instance.casefile.CaseFile;
import org.qollabor.cmmn.instance.parameter.CaseInputParameter;
import org.qollabor.cmmn.instance.parameter.CaseOutputParameter;
import org.qollabor.cmmn.instance.sentry.SentryNetwork;
import org.qollabor.cmmn.instance.team.CurrentMember;
import org.qollabor.cmmn.instance.team.Team;
import org.qollabor.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

public class Case extends ModelActor<CaseCommand, CaseEvent> {

    private final static Logger logger = LoggerFactory.getLogger(Case.class);

    /**
     * The moment at which the case is created
     */
    private Instant createdOn;
    /**
     * List of plan items in the case.
     */
    private Collection<PlanItem> planItems = new ArrayList();
    /**
     * Pointer to the case file instance of the case.
     */
    private CaseFile caseFile;
    /**
     * List of sentries active within the case.
     */
    private final SentryNetwork sentryNetwork;
    /**
     * Case model that is interpreted for this case
     */
    private CaseDefinition definition;
    /**
     * Root level plan item of the case.
     */
    private CasePlan casePlan;

    /**
     * Identifier of parent case that caused this case to start.
     */
    private String parentCaseId;

    /**
     * Identifier of top-most case that caused this case to start.
     */
    private String rootCaseId;

    /**
     * Workers in the case team
     */
    private final Team caseTeam = new Team(this);

    public Case() {
        super(CaseCommand.class, CaseEvent.class);
        this.createdOn = getTransactionTimestamp();
        this.sentryNetwork = new SentryNetwork(this);

        logger.info("Recovering/creating case " + this.getId() + " with path " + self().path());
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getParentActorId() {
        return this.getParentCaseId();
    }

    @Override
    public String getRootActorId() {
        return this.getRootCaseId();
    }

    @Override
    public CaseModified createTransactionEvent() {
        int numFailedPlanItems = Long.valueOf(planItems.stream().filter(p -> p.getState() == org.qollabor.cmmn.instance.State.Failed).count()).intValue();
        return new CaseModified(this, getTransactionTimestamp(), numFailedPlanItems);
    }

    /**
     * Returns the user context under which the current actions in the engine are being processed
     *
     * @return
     */
    public CurrentMember getCurrentTeamMember() {
        return getCaseTeam().getTeamMember(getCurrentUser());
    }

    /**
     * Internal engine method to register any newly created plan items in the global plan item collection
     *
     * @param planItem
     */
    void registerPlanItem(PlanItem planItem) {
        planItems.add(planItem);
    }

    /**
     * Helper method to dump the state of the case into an XML document.
     *
     * @return
     */
    public String stateToXMLString() {
        Document xmlDocument;
        try {
            if (definition == null) {
                xmlDocument = XMLHelper.loadXML("<Case id=\"" + getId() + "\"/>");
            } else {
                xmlDocument = XMLHelper.loadXML("<Case name=\"" + definition.getName() + "\" id=\"" + getId() + "\"/>");
                getCaseTeam().dumpMemoryStateToXML(xmlDocument.getDocumentElement());
                getCaseFile().dumpMemoryStateToXML(xmlDocument.getDocumentElement());
                getCasePlan().dumpMemoryStateToXML(xmlDocument.getDocumentElement());
            }
            return XMLHelper.printXMLNode(xmlDocument);
        } catch (SAXException | ParserConfigurationException | IOException willNotOccur) {
            throw new RuntimeException("Cannot parse xml???", willNotOccur);
        }
    }

    /**
     * Returns the moment of case creation.
     *
     * @return
     */
    public Instant getCreatedOn() {
        return createdOn;
    }

    /**
     * Returns the definition according to which this case behaves
     *
     * @return
     */
    public CaseDefinition getDefinition() {
        return definition;
    }

    /**
     * Returns a plan item based on it's id, or null if no plan item with the specified id was found.
     *
     * @param id
     * @return
     */
    public <T extends PlanItem> T getPlanItemById(String id) {
        return (T) planItems.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * Returns the plan item with the corresponding name, or null if none is found.
     * If there are multiple plan items with the same name, the last one that has been added to the case is returned.
     *
     * @param name
     * @return
     */
    public PlanItem getPlanItemByName(String name) {
        PlanItem lastWithThisName = null;
        for (PlanItem planItem : planItems) {
            if (planItem.getName().equals(name)) {
                lastWithThisName = planItem;
            }
        }
        return lastWithThisName;
    }

    /**
     * Returns a collection with the currently available plan items of the case
     *
     * @return
     */
    public Collection<PlanItem> getPlanItems() {
        return planItems;
    }

    /**
     * Returns a collection of plan items with the specified identifier.
     * This can either be an ID (in which case obviously there may be only 1 plan item in the collection),
     * or it can be the plan item name, in which case multiple (e.g. repeating) plan items are in the collection.
     * The collection is sorted reversely, i.e., the last plan item that was created, is at the front of the collection.
     *
     * @param identifier
     * @return
     */
    public Collection<PlanItem> getPlanItems(String identifier) {
        ArrayList<PlanItem> list = new ArrayList();
        for (PlanItem planItem : getPlanItems()) {
            if (planItem.getName().equals(identifier) || planItem.getId().equals(identifier)) {
                list.add(0, planItem);
            }
        }
        return list;
    }

    /**
     * Returns the sentries currently instantiated within this case
     *
     * @return
     */
    public SentryNetwork getSentryNetwork() {
        return sentryNetwork;
    }

    void setCasePlan(CasePlan casePlan) {
        this.casePlan = casePlan;
    }

    /**
     * Returns the root plan item
     *
     * @return
     */
    public CasePlan getCasePlan() {
        return casePlan;
    }

    /**
     * Creates a PlanItem within the Case based on the information in the event.
     * @param event
     * @return
     */
    public PlanItem add(PlanItemCreated event) {
        String stageId = event.getStageId();
        if (stageId.isEmpty()) {
            CasePlanDefinition definition = this.getDefinition().getCasePlanModel();
            return definition.createInstance(event.planItemId, 0, definition, null, this);
        } else {
            // Lookup the stage to which the plan item belongs,
            // then lookup the definition for the plan item
            // and then instantiate it.
            Stage<?> stage = this.getPlanItemById(stageId);
            if (stage == null) {
                logger.error("MAJOR ERROR: we cannot find the stage with id " + stageId + ", and therefore cannot recover plan item " + event);
                return null;
            }

            ItemDefinition itemDefinition = stage.getDefinition().getPlanItem(event.planItemName);
            // If definition == null, try to see if it's a discretionaryItem
            if (itemDefinition == null) {
                itemDefinition = stage.getDefinition().getDiscretionaryItem(event.planItemName);
                if (itemDefinition == null) {
                    logger.error("MAJOR ERROR: we cannot find a plan item definition named '" + event.planItemName + "' in stage " + event.getStageId() + ", and therefore cannot recover plan item " + event);
                }
            }

            PlanItemDefinitionDefinition reference = itemDefinition.getPlanItemDefinition();
            return reference.createInstance(event.getPlanItemId(), event.getIndex(), itemDefinition, stage, this);
        }
    }

    /**
     * Returns the case file. Note that this is a wrapper structure around the actual data.
     *
     * @return
     */
    public CaseFile getCaseFile() {
        if (caseFile == null) {
            // Let's create the case file. It is empty in the beginning, without any data.
            this.caseFile = new CaseFile(this, getDefinition().getCaseFileModel());
        }
        return caseFile;
    }

    /**
     * Returns a collection of all discretionary items currently applicable
     *
     * @return
     */
    public Collection<DiscretionaryItem> getDiscretionaryItems() {
        addDebugInfo(() -> "Retrieving discretionary items of " + this);
        Collection<DiscretionaryItem> items = new ArrayList();
        getCasePlan().retrieveDiscretionaryItems(items);
        addDebugInfo(() -> {
            StringBuilder itemsString = new StringBuilder();
            items.forEach(item -> itemsString.append("\n\t" + item.getDefinition() + " in " + item.getParentId()));
            return "Discretionary items of " + this + itemsString;
        });
        return items;
    }

    /**
     * Internal framework method to support Akka command handling and recovery
     */
    public void applyCaseDefinition(CaseDefinition definition, String parentCaseId, String rootCaseId) {
        // Set the definition and tenant.
        this.definition = definition;
        // Link the (optional) ancestor information
        this.parentCaseId = parentCaseId;
        this.rootCaseId = rootCaseId;
    }

    public void setInputParameters(ValueMap inputParameters) {
        // TODO: figure out whether we should actually re-use the CaseParameter objects, or
        // copy them. Currently we copy the parameters.
        CaseDefinition definition = getDefinition();
        inputParameters.getValue().forEach((name, externalParameter) -> {
            InputParameterDefinition inputParameterDefinition = definition.getInputParameters().get(name);
            CaseInputParameter parameter = new CaseInputParameter(inputParameterDefinition, this, externalParameter);
            parameter.bindParameterToCaseFile();
        });
    }

    public void createCasePlan() {
        PlanItemCreated pic = new PlanItemCreated(this);
        addEvent(pic);
        pic.getCreatedPlanItem().makeTransition(Transition.Create);
    }

    public void releaseBootstrapCaseFileEvents() {
        addDebugInfo(() -> "Releasing potentially delayed Case File Events");
        getCaseFile().releaseBootstrapEvents();
    }

    /**
     * Returns a map with output parameters by name with their "raw" value
     *
     * @return
     */
    ValueMap getOutputParameters() {
        ValueMap outputParameters = new ValueMap();
        getDefinition().getOutputParameters().forEach((name, outputParameterDefinition) -> {
            CaseOutputParameter outputParameter = new CaseOutputParameter(outputParameterDefinition, this);
            // Doing getValue() forces the parameter to take it's value from the case file (if bound to it)
            outputParameters.put(name, outputParameter.getValue());
        });
        return outputParameters;
    }

    /**
     * Internal framework method to support Akka command handling and recovery
     */
    public void makePlanItemTransition(PlanItem planItem, Transition transition) {
        planItem.makeTransition(transition);
    }

    /**
     * Returns the id of the case that started this case (if at all)
     *
     * @return
     */
    public String getParentCaseId() {
        return parentCaseId;
    }

    /**
     * Returns the id of the outer-most case that caused this case to start (could be the id of this case itself)
     *
     * @return
     */
    public String getRootCaseId() {
        return rootCaseId;
    }

    /**
     * Returns the team with the workers for this case.
     *
     * @return
     */
    public Team getCaseTeam() {
        return caseTeam;
    }

    public void upsertDebugMode(boolean newDebugMode) {
        // TODO: this belongs in ModelActor, but then the debugenabled/disabled events need to take ModelActor and should no longer extend CaseEvent
        if (newDebugMode!=this.debugMode()) {
            if (newDebugMode) addEvent(new DebugEnabled(this));
            else addEvent(new DebugDisabled(this));
        }
    }
}
