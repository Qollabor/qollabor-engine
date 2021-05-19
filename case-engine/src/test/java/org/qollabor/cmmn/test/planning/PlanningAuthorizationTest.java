/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.qollabor.cmmn.test.planning;

import org.qollabor.akka.actor.identity.TenantUser;
import org.qollabor.cmmn.akka.command.AddDiscretionaryItem;
import org.qollabor.cmmn.akka.command.GetDiscretionaryItems;
import org.qollabor.cmmn.akka.command.StartCase;
import org.qollabor.cmmn.akka.command.response.GetDiscretionaryItemsResponse;
import org.qollabor.cmmn.akka.command.team.CaseTeam;
import org.qollabor.cmmn.definition.CaseDefinition;
import org.qollabor.cmmn.test.TestScript;
import org.qollabor.cmmn.test.assertions.DiscretionaryItemAssertion;
import org.qollabor.cmmn.test.assertions.PlanningTableAssertion;
import org.junit.Test;

public class PlanningAuthorizationTest {

    private final String testName = "authorization-test";
    private final String caseInstanceId = testName;
    private final TenantUser anonymous = TestScript.getTestUser("Anonymous");
    private final TenantUser planner = TestScript.getTestUser("Planner", "planner");
    private final CaseDefinition definitions = TestScript.getCaseDefinition("testdefinition/planning/authorization.xml");

    @Test
    public void testPlanningAuthorization() {
        TestScript testCase = new TestScript(testName);
        CaseTeam caseTeam = TestScript.getCaseTeam(TestScript.getOwner(anonymous), planner);

        testCase.addStep(new StartCase(anonymous, caseInstanceId, definitions, null, caseTeam), casePlan -> {
            casePlan.print();

            final String discretionaryTaskName = "PlanMe";

            testCase.insertStep(new GetDiscretionaryItems(anonymous, caseInstanceId), step -> {
                PlanningTableAssertion pta = new PlanningTableAssertion(step);

                // Now add discretionary task to the plan
                DiscretionaryItemAssertion discretionaryTask = pta.assertItem(discretionaryTaskName);
                String stageId = discretionaryTask.getParentId();
                String definitionId = discretionaryTask.getDefinitionId();
                testCase.insertStepFails(new AddDiscretionaryItem(anonymous, caseInstanceId, "PlanMe", definitionId, stageId, "planned-item"), failure -> {
                    // Planning by anonymous should fail, but by planner it should succeed.
                    testCase.insertStep(new AddDiscretionaryItem(planner, caseInstanceId, "PlanMe", definitionId, stageId, "planned-item"), lastPlan -> lastPlan.print());
                });
            });


        });


        testCase.runTest();
    }

    @Test
    public void testGetDiscretionaryItems() {
        TestScript testCase = new TestScript(testName);
        CaseTeam caseTeam = TestScript.getCaseTeam(TestScript.getOwner(anonymous), planner);

        testCase.addStep(new StartCase(anonymous, caseInstanceId, definitions, null, caseTeam), casePlan -> casePlan.print());

        testCase.addStep(new GetDiscretionaryItems(anonymous, caseInstanceId), action -> {

            final String discretionaryTaskName = "PlanMe";

            PlanningTableAssertion pta = new PlanningTableAssertion(action);
            TestScript.debugMessage("Items: "+pta);
            pta.assertItems();
            DiscretionaryItemAssertion discItem = pta.assertItem(discretionaryTaskName);

            TestScript.debugMessage("PlanMe looks like "+discItem);

            // Now add discretionary task to the plan
            DiscretionaryItemAssertion discretionaryTask = discItem;
            String stageId = discretionaryTask.getParentId();
            String definitionId = discretionaryTask.getDefinitionId();
            testCase.insertStepFails(new AddDiscretionaryItem(anonymous, caseInstanceId, "PlanMe", definitionId, stageId, "planned-item"), failure -> {
                // Planning by anonymous should fail, but by planner it should succeed.
                testCase.insertStep(new AddDiscretionaryItem(planner, caseInstanceId, "PlanMe", definitionId, stageId, "planned-item"), lastPlan -> lastPlan.print());
            });
        });

        testCase.addStep(new GetDiscretionaryItems(anonymous, caseInstanceId), response -> {
            new PlanningTableAssertion(response).assertNoItems();
            GetDiscretionaryItemsResponse items = response.getTestCommand().getActualResponse();
            TestScript.debugMessage(items.getItems());
        });

        testCase.runTest();
    }
}