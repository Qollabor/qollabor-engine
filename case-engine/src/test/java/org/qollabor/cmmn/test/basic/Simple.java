/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.test.basic;

import org.qollabor.cmmn.akka.command.MakeCaseTransition;
import org.qollabor.cmmn.akka.command.MakePlanItemTransition;
import org.qollabor.cmmn.akka.command.StartCase;
import org.qollabor.cmmn.definition.CaseDefinition;
import org.qollabor.cmmn.instance.State;
import org.qollabor.cmmn.instance.Transition;
import org.qollabor.cmmn.test.TestScript;
import org.qollabor.cmmn.test.assertions.PlanItemAssertion;
import org.qollabor.cmmn.test.assertions.StageAssertion;
import org.qollabor.akka.actor.identity.TenantUser;
import org.junit.Test;

public class Simple {
    private final CaseDefinition definitions = TestScript.getCaseDefinition("testdefinition/simple.xml");
    private final TenantUser testUser = TestScript.getTestUser("Anonymous");

    @Test
    public void testSimple() {
        String caseInstanceId = "Simple";
        TestScript testCase = new TestScript(caseInstanceId);

        StartCase startCase = new StartCase(testUser, caseInstanceId, definitions, null, null);
        testCase.addStep(startCase, casePlan -> {
            casePlan.assertLastTransition(Transition.Create, State.Active, State.Null);
            StageAssertion stage1 = casePlan.assertStage("Stage1");
            stage1.assertLastTransition(Transition.Start, State.Active, State.Available);
            PlanItemAssertion item1dot1 = stage1.assertPlanItem("Item1.1");
            item1dot1.assertLastTransition(Transition.Start, State.Active, State.Available);
            PlanItemAssertion item1 = casePlan.assertPlanItem("Item1");
            item1.assertLastTransition(Transition.Start, State.Active, State.Available);
        });

        // Completing Item1 should make it go to state Completed, others remain in same state
        testCase.addStep(new MakePlanItemTransition(testUser, caseInstanceId, "Item1", Transition.Complete), casePlan -> {
            casePlan.assertLastTransition(Transition.Create, State.Active, State.Null);
            StageAssertion stage1 = casePlan.assertStage("Stage1");
            stage1.assertLastTransition(Transition.Start, State.Active, State.Available);
            PlanItemAssertion item1dot1 = stage1.assertPlanItem("Item1.1");
            item1dot1.assertLastTransition(Transition.Start, State.Active, State.Available);
            PlanItemAssertion item1 = casePlan.assertPlanItem("Item1");
            item1.assertLastTransition(Transition.Complete, State.Completed, State.Active);
        });
        
        // Completing Item1 again should not change state.
        testCase.addStep(new MakePlanItemTransition(testUser, caseInstanceId, "Item1", Transition.Complete), casePlan -> {
            casePlan.assertLastTransition(Transition.Create, State.Active, State.Null);
            StageAssertion stage1 = casePlan.assertStage("Stage1");
            stage1.assertLastTransition(Transition.Start, State.Active, State.Available);
            PlanItemAssertion item1dot1 = stage1.assertPlanItem("Item1.1");
            item1dot1.assertLastTransition(Transition.Start, State.Active, State.Available);
            PlanItemAssertion item1 = casePlan.assertPlanItem("Item1");
            item1.assertLastTransition(Transition.Complete, State.Completed, State.Active);
        });
        
        // Suspend the whole case.
        testCase.addStep(new MakeCaseTransition(testUser, caseInstanceId, Transition.Suspend), casePlan -> {
            casePlan.assertLastTransition(Transition.Suspend, State.Suspended, State.Active);
            StageAssertion stage1 = casePlan.assertStage("Stage1");
            stage1.assertLastTransition(Transition.ParentSuspend, State.Suspended, State.Active);
            PlanItemAssertion item1dot1 = stage1.assertPlanItem("Item1.1");
            item1dot1.assertLastTransition(Transition.ParentSuspend, State.Suspended, State.Active);
            PlanItemAssertion item1 = casePlan.assertPlanItem("Item1");
            item1.assertLastTransition(Transition.Complete, State.Completed, State.Active);
        });
        
        
        // And re-activate it again
        testCase.addStep(new MakeCaseTransition(testUser, caseInstanceId, Transition.Reactivate), casePlan -> {
            casePlan.assertLastTransition(Transition.Reactivate, State.Active, State.Suspended);
            StageAssertion stage1 = casePlan.assertStage("Stage1");
            stage1.assertLastTransition(Transition.ParentResume, State.Active, State.Suspended);
            PlanItemAssertion item1dot1 = stage1.assertPlanItem("Item1.1");
            item1dot1.assertLastTransition(Transition.ParentResume, State.Active, State.Suspended);
            PlanItemAssertion item1 = casePlan.assertPlanItem("Item1");
            item1.assertLastTransition(Transition.Complete, State.Completed, State.Active);
        });        

        // Completing Item1.1 should make it go to state Completed, others remain in same state, causing completion check of surrounding stage
        testCase.addStep(new MakePlanItemTransition(testUser, caseInstanceId, "Item1.1", Transition.Complete), casePlan -> {
            casePlan.assertLastTransition(Transition.Complete, State.Completed, State.Active);
            StageAssertion stage1 = casePlan.assertStage("Stage1");
            stage1.assertLastTransition(Transition.Complete, State.Completed, State.Active);
            PlanItemAssertion item1dot1 = stage1.assertPlanItem("Item1.1");
            item1dot1.assertLastTransition(Transition.Complete, State.Completed, State.Active);
            PlanItemAssertion item1 = casePlan.assertPlanItem("Item1");
            item1.assertLastTransition(Transition.Complete, State.Completed, State.Active);
        });
        
        // Complete the whole case
        testCase.addStep(new MakeCaseTransition(testUser, caseInstanceId, Transition.Complete), casePlan -> {
            casePlan.assertLastTransition(Transition.Complete, State.Completed, State.Active);
            StageAssertion stage1 = casePlan.assertStage("Stage1");
            stage1.assertLastTransition(Transition.Complete, State.Completed, State.Active);
            PlanItemAssertion item1dot1 = stage1.assertPlanItem("Item1.1");
            item1dot1.assertLastTransition(Transition.Complete, State.Completed, State.Active);
            PlanItemAssertion item1 = casePlan.assertPlanItem("Item1");
            item1.assertLastTransition(Transition.Complete, State.Completed, State.Active);
        }); 
        
        testCase.runTest();
    }
    
}
