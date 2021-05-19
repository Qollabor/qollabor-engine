package org.qollabor.cmmn.test.casefile;

import org.qollabor.akka.actor.identity.TenantUser;
import org.qollabor.cmmn.akka.command.MakePlanItemTransition;
import org.qollabor.cmmn.akka.command.StartCase;
import org.qollabor.cmmn.akka.event.plan.PlanItemTransitioned;
import org.qollabor.cmmn.definition.CaseDefinition;
import org.qollabor.cmmn.instance.State;
import org.qollabor.cmmn.instance.Transition;
import org.qollabor.akka.actor.serialization.json.ValueList;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.cmmn.test.TestScript;
import org.qollabor.cmmn.test.assertions.HumanTaskAssertion;
import org.qollabor.humantask.akka.command.CompleteHumanTask;
import org.qollabor.util.Guid;
import org.junit.Test;

public class TestTaskInputMapping {
    private final String caseName = "TaskInputMapping";
    private final CaseDefinition definitions = TestScript.getCaseDefinition("testdefinition/casefile/taskinputmapping.xml");
    private final TenantUser testUser = TestScript.getTestUser("Anonymous");

    @Test
    public void testContextSettingsFromTasks() {

        // Basically this tests input parameter mapping
        String caseInstanceId = new Guid().toString();
        TestScript testCase = new TestScript(caseName);
        ValueMap caseInput = new ValueMap();

        testCase.addStep(new StartCase(testUser, caseInstanceId, definitions, caseInput.cloneValueNode(), null), startPlan -> {
            startPlan.print();
            String taskAddChild = startPlan.assertTask("Task.AddChild").assertState(State.Active).getId();
            startPlan.assertTask("TaskWithContainer").assertState(State.Available);
            startPlan.assertTask("TaskWithChild").assertState(State.Available);

            TestScript.debugMessage("taskAddChild - id: " + taskAddChild);


            ValueMap child1 = new ValueMap("arrayProp1", "string");
            ValueMap child2 = new ValueMap("arrayProp1", "string2");

            // Now create a new task output, and complete the task with it
            // Completing the task must lead to a new task of the same kind, and we will also complete that one
            testCase.insertStep(new CompleteHumanTask(testUser, caseInstanceId, taskAddChild, new ValueMap("Result", child1)), result -> {
                result.print();
                HumanTaskAssertion casePlan = new HumanTaskAssertion(result);
                TestScript.debugMessage("taskAddChild - id: " + taskAddChild);

                // Check that one is completed
                testCase.getEventListener().awaitPlanItemState(taskAddChild, State.Completed);

//                casePlan.assertPlanItems("Task.AddChild").filter(State.Completed).assertSize(1);
                // Fetch the active one, and complete that one with the some different output.
                PlanItemTransitioned event = testCase.getEventListener().awaitPlanItemEvent("Task.AddChild", PlanItemTransitioned.class,
                        e -> !e.getPlanItemId().equals(taskAddChild) && e.getCurrentState().equals(State.Active));
                String secondTaskAddChild = event.getPlanItemId();
                testCase.insertStep(new CompleteHumanTask(testUser, caseInstanceId, secondTaskAddChild, new ValueMap("Result", child2)), secondResult -> {
                    secondResult.print();

                    // Now trigger the event and check the input of the new TaskWithChild
                    testCase.insertStep(new MakePlanItemTransition(testUser, caseInstanceId, "Trigger.TaskWithChild", Transition.Occur), planAfterEvent -> {
                        planAfterEvent.print();
                        testCase.getEventListener().awaitTaskInputFilled("TaskWithChild", taskEvent -> {
                            ValueMap expectedInput = new ValueMap("Input", child2.cloneValueNode());
                            if (taskEvent.getMappedInputParameters().equals(expectedInput)) {
                                return true;
                            } else {
                                throw new AssertionError("Unexpected task input:\n"+taskEvent.getMappedInputParameters());
                            }
                        });
                    });

                    // Now trigger the other event and check the input of the new TaskWithChild
                    testCase.insertStep(new MakePlanItemTransition(testUser, caseInstanceId, "Trigger.TaskWithContainer", Transition.Occur), planAfterEvent -> {
                        planAfterEvent.print();
                        testCase.getEventListener().awaitTaskInputFilled("TaskWithContainer", taskEvent -> {
                            ValueMap expectedInput = new ValueMap("Input", new ValueList(child1.cloneValueNode(), child2.cloneValueNode()));
                            if (taskEvent.getMappedInputParameters().equals(expectedInput)) {
                                return true;
                            } else {
                                throw new AssertionError("Unexpected task input:\n"+taskEvent.getMappedInputParameters());
                            }
                        });
                    });
                });
            });
        });

        testCase.runTest();
    }
}