/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.test.casefile;

import org.qollabor.cmmn.akka.command.MakeCaseTransition;
import org.qollabor.cmmn.akka.command.MakePlanItemTransition;
import org.qollabor.cmmn.akka.command.StartCase;
import org.qollabor.cmmn.akka.command.casefile.CreateCaseFileItem;
import org.qollabor.cmmn.akka.command.casefile.ReplaceCaseFileItem;
import org.qollabor.cmmn.akka.command.casefile.UpdateCaseFileItem;
import org.qollabor.cmmn.definition.CaseDefinition;
import org.qollabor.cmmn.instance.State;
import org.qollabor.cmmn.instance.Transition;
import org.qollabor.akka.actor.serialization.json.StringValue;
import org.qollabor.akka.actor.serialization.json.ValueList;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.cmmn.instance.casefile.Path;
import org.qollabor.cmmn.test.TestScript;
import org.qollabor.cmmn.test.assertions.TaskAssertion;
import org.qollabor.akka.actor.identity.TenantUser;
import org.qollabor.util.Guid;
import org.junit.Test;

public class CaseFileTransitionTest {

    private static final String REVIEW_STAGE = "ReviewStage";
    private static final String REVIEW_REQUEST = "ReviewRequest";
    private final String inputParameterName = "inputCaseFile";
    private final CaseDefinition definitions = TestScript.getCaseDefinition("testdefinition/repetitivefileitems.xml");
    private final TenantUser testUser = TestScript.getTestUser("Anonymous");
    private final Path requestPath = new Path("Request");
    private final Path helperPath = new Path("Request/Helper");
    private final Path customerPath = new Path("Request/Customer");
    private final Path customer0 = new Path("Request/Customer[0]");

    private ValueList getCustomers(String... customers) {
        ValueList customerList = new ValueList();
        for (String customer : customers) {
            customerList.add(getCustomer(customer));
        }
        return customerList;
    }

    private StringValue getCustomer(String customer) {
        return new StringValue(customer);
    }

    @Test
    public void testRepetitiveTaskIfExpressionAndTransition() {

        String caseName = "casefile";
        String caseInstanceId = new Guid().toString();
        TestScript testCase = new TestScript(caseName);

        ValueMap content = new ValueMap();
        content.put("Customer", getCustomers("Joost", "Piet", "Joop"));
        content.putRaw("Description", "Help me out");
        ValueMap inputs = new ValueMap();
        inputs.put(inputParameterName, content);

        StartCase startCase = new StartCase(testUser, caseInstanceId, definitions, inputs.cloneValueNode(), null);
        testCase.addStep(startCase, casePlan -> {
            casePlan.print();
            casePlan.assertCaseFileItem(requestPath).assertValue(content).assertCaseFileItem(new Path("/Customer")).assertState(State.Null);
        });

        // Create the CaseFileItem Request/Helper
        ValueMap helper = new ValueMap();
        helper.putRaw("Name", "Piet");
        helper.putRaw("Description", "Piet is a nice guy");

        testCase.addStep(new CreateCaseFileItem(testUser, caseInstanceId, helper, helperPath), casePlan -> {
            casePlan.print();
            casePlan.assertCaseFileItem(helperPath).assertValue(helper);


            // 2 repeating ReviewRequest task in state Active and Available.
            casePlan.assertPlanItems(REVIEW_STAGE).assertSize(1).assertStates(State.Active).assertNoMoreRepetition();
//            casePlan.assertPlanItems(REVIEW_REQUEST).assertSize(1).assertStates(State.Active).assertRepeats();

        });

        // Completing the task ReviewRequest
        testCase.addStep(new MakePlanItemTransition(testUser, caseInstanceId, REVIEW_REQUEST, Transition.Complete), casePlan -> {
            casePlan.print();
            casePlan.assertLastTransition(Transition.Create, State.Active, State.Null);

            // 3 repeating ReviewRequest task in state Completed,Active and Available.
            casePlan.assertStage(REVIEW_STAGE).assertPlanItems(REVIEW_REQUEST).assertSize(2).assertStates(State.Completed, State.Active).assertRepeats();

        });

        // Completing the task ReviewRequest
        testCase.addStep(new MakePlanItemTransition(testUser, caseInstanceId, REVIEW_REQUEST, Transition.Complete), casePlan -> {
            casePlan.print();
            casePlan.assertLastTransition(Transition.Create, State.Active, State.Null);

            // 4 repeating ReviewRequest task in state Completed,Completed,Active and Available.
            casePlan.assertStage(REVIEW_STAGE).assertPlanItems(REVIEW_REQUEST).assertSize(3).assertStates(State.Completed, State.Active).assertRepeats();

        });

        // Suspend the case
        testCase.addStep(new MakeCaseTransition(testUser, caseInstanceId, Transition.Suspend), casePlan -> {
            casePlan.print();
            casePlan.assertLastTransition(Transition.Suspend, State.Suspended, State.Active);

            TaskAssertion item1 = casePlan.assertStage(REVIEW_STAGE).assertTask(REVIEW_REQUEST);
            item1.assertLastTransition(Transition.Complete, State.Completed, State.Active);

            // 2 repeating ReviewRequest task in state Suspended.
            casePlan.assertStage(REVIEW_STAGE).assertPlanItems(REVIEW_REQUEST).assertSize(3).assertStates(State.Completed, State.Suspended).assertRepeats();

        });

        // Reactivate the case
        testCase.addStep(new MakeCaseTransition(testUser, caseInstanceId, Transition.Reactivate), casePlan -> {
            casePlan.print();
            casePlan.assertLastTransition(Transition.Reactivate, State.Active, State.Suspended);

            // After reactivating it should return to the previous state
            casePlan.assertStage(REVIEW_STAGE).assertPlanItems(REVIEW_REQUEST).assertSize(3).assertStates(State.Completed, State.Active).assertRepeats();

        });

        StringValue piet = getCustomer("Piet");
        testCase.addStep(new ReplaceCaseFileItem(testUser, caseInstanceId, piet, customer0), casePlan -> {
            casePlan.print();
            // After changing customer, task should still be repeating, although value is now different.
            casePlan.assertStage(REVIEW_STAGE).assertPlanItems(REVIEW_REQUEST).assertSize(3).assertStates(State.Completed, State.Active).assertRepeats();
        });

        // Completing the task ReviewRequest; now the task should no longer be repeating
        testCase.addStep(new MakePlanItemTransition(testUser, caseInstanceId, REVIEW_REQUEST, Transition.Complete), casePlan -> {
            casePlan.print();

            // All tasks should be completed, and no more repetition.
            casePlan.assertStage(REVIEW_STAGE).assertPlanItems(REVIEW_REQUEST).assertSize(3).assertStates(State.Completed).assertNoMoreRepetition();
        });

        // Complete the case, by completing JustAnotherTask
        testCase.addStep(new MakePlanItemTransition(testUser, caseInstanceId, "JustAnotherTask", Transition.Complete), casePlan -> {
            casePlan.assertLastTransition(Transition.Complete, State.Completed, State.Active);

            casePlan.assertStage(REVIEW_STAGE).assertPlanItems(REVIEW_REQUEST).assertSize(3).assertStates(State.Completed);
            casePlan.assertTask("JustAnotherTask").assertState(State.Completed);

            TestScript.debugMessage(casePlan);
        });

        ValueList customers = getCustomers("Piet", "Joop");
        ReplaceCaseFileItem customerReplace = new ReplaceCaseFileItem(testUser, caseInstanceId, customers, customerPath);
        testCase.addStep(customerReplace, action -> {
            TestScript.debugMessage(action);
            TestScript.debugMessage("Found these events:\n"  + action.getEvents().enumerateEventsByType());
            action.getEvents().assertSize(3);
        });

        ValueMap newRequestContent = new ValueMap("Customer", getCustomers("Klaas", "Henk"));
        testCase.addStep(new UpdateCaseFileItem(testUser, caseInstanceId, newRequestContent, requestPath), action -> {
            TestScript.debugMessage(action);
            TestScript.debugMessage("Found these events:\n"  + action.getEvents().enumerateEventsByType());
            action.getEvents().assertSize(3);
        });

        testCase.addStep(new UpdateCaseFileItem(testUser, caseInstanceId, new ValueMap("Customer", getCustomers("Klaas", "Henk")), requestPath), action -> {
            TestScript.debugMessage(action);
            TestScript.debugMessage("Found these events:\n"  + action.getEvents().enumerateEventsByType());
            action.getEvents().assertSize(0);
        });

        testCase.addStep(new ReplaceCaseFileItem(testUser, caseInstanceId, newRequestContent, requestPath), action -> {
            TestScript.debugMessage(action);
            TestScript.debugMessage("Found these events:\n"  + action.getEvents().enumerateEventsByType());
            action.getEvents().assertSize(4);
        });

        ValueMap singularRequestCustomerContent = new ValueMap("Customer", getCustomer("Loner"));
        testCase.addStep(new ReplaceCaseFileItem(testUser, caseInstanceId, singularRequestCustomerContent, requestPath), action -> {
            TestScript.debugMessage(action);
            TestScript.debugMessage("Found these events:\n"  + action.getEvents().enumerateEventsByType());
            action.getEvents().assertSize(3);
        });

        testCase.runTest();

    }

}
