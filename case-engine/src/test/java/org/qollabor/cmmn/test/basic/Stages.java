/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.test.basic;

import org.qollabor.akka.actor.identity.TenantUser;
import org.qollabor.cmmn.akka.command.MakePlanItemTransition;
import org.qollabor.cmmn.akka.command.StartCase;
import org.qollabor.cmmn.definition.CaseDefinition;
import org.qollabor.cmmn.instance.State;
import org.qollabor.cmmn.instance.Transition;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.cmmn.test.TestScript;
import org.qollabor.cmmn.test.assertions.StageAssertion;
import org.junit.Test;

public class Stages {
    @Test
    public void testStages() {
        String caseName = "stages";
        TestScript testCase = new TestScript(caseName);

        ValueMap inputs = null;
        String caseInstanceId = caseName;
        CaseDefinition definitionsDocument = TestScript.getCaseDefinition("testdefinition/stages.xml");
        TenantUser user = TestScript.getTestUser("Anonymous");

        testCase.addStep(new StartCase(user, caseInstanceId, definitionsDocument, inputs, null), casePlan -> {
            casePlan.print();
            casePlan.assertPlanItem("Item1").assertState(State.Available);
            StageAssertion item4 = casePlan.assertStage("Item4");
            item4.assertState(State.Active);
            StageAssertion stage1_1 = item4.assertStage("Stage1.1");
            stage1_1.assertState(State.Active);
            stage1_1.assertHumanTask("Task1 in Stage1.1").assertState(State.Active);
            StageAssertion stage1_1_2 = stage1_1.assertStage("Stage1.1.2");
            stage1_1_2.assertHumanTask("Task1 in Stage 1.2").assertState(State.Active);
            StageAssertion stage1_2 = item4.assertStage("Stage1.2");
            stage1_2.assertState(State.Available);
            item4.assertHumanTask("Task1.1").assertState(State.Active);
        });

        testCase.addStep(new MakePlanItemTransition(user, caseInstanceId, "Task1.1", Transition.Complete), casePlan -> {
            casePlan.print();
            casePlan.assertPlanItem("Item1").assertState(State.Active);
            StageAssertion item4 = casePlan.assertStage("Item4");
            item4.assertState(State.Active);
            StageAssertion stage1_1 = item4.assertStage("Stage1.1");
            stage1_1.assertState(State.Active);
            stage1_1.assertHumanTask("Task1 in Stage1.1").assertState(State.Active);
            StageAssertion stage1_1_2 = stage1_1.assertStage("Stage1.1.2");
            stage1_1_2.assertHumanTask("Task1 in Stage 1.2").assertState(State.Active);
            StageAssertion stage1_2 = item4.assertStage("Stage1.2");
            stage1_2.assertState(State.Active);
            stage1_2.assertHumanTask("Task1 in Stage 1.2").assertState(State.Active);
            item4.assertHumanTask("Task1.1").assertState(State.Completed);
        });

        testCase.runTest();
    }
}
