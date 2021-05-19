/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.test.casefile;

import org.qollabor.akka.actor.identity.TenantUser;
import org.qollabor.cmmn.akka.command.StartCase;
import org.qollabor.cmmn.akka.command.casefile.CreateCaseFileItem;
import org.qollabor.cmmn.akka.command.casefile.DeleteCaseFileItem;
import org.qollabor.cmmn.akka.command.casefile.ReplaceCaseFileItem;
import org.qollabor.cmmn.akka.command.casefile.UpdateCaseFileItem;
import org.qollabor.cmmn.definition.CaseDefinition;
import org.qollabor.cmmn.definition.casefile.CaseFileError;
import org.qollabor.akka.actor.serialization.json.Value;
import org.qollabor.akka.actor.serialization.json.ValueList;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.cmmn.instance.casefile.Path;
import org.qollabor.cmmn.test.TestScript;
import org.qollabor.util.Guid;
import org.junit.Test;

public class BasicTypes {
    // This tests a set of basic case file types and properties
    // The test just starts the case and then validates the output, no specific actions are done (no transitions are made)

    private final String caseName = "CaseFileDefinitionTest";
    private final String inputParameterName = "inputCaseFile";
    private final CaseDefinition definitions = TestScript.getCaseDefinition("testdefinition/casefile/basictypes.xml");
    private final TenantUser testUser = TestScript.getTestUser("Anonymous");
    private final Path allPropertyTypesPath = new Path("AllPropertyTypes");
    private final Path childItemPath = new Path("AllPropertyTypes/ChildItem");
    private final Path childArrayPath = new Path("AllPropertyTypes/ArrayOfChildItem");
    private final Path child0 = new Path("AllPropertyTypes/ArrayOfChildItem[0]");
    private final Path child1 = new Path("AllPropertyTypes/ArrayOfChildItem[1]");

    private ValueMap getInputs() {
        ValueMap inputs = new ValueMap();
        inputs.put(inputParameterName, getContents());
        return inputs;
    }

    private ValueMap getContents(ValueMap... inputs) {
        if (inputs.length > 0) {
            return inputs[0].get(inputParameterName).asMap();
        }
        ValueMap contents = new ValueMap();
        // Put some contents in the input parameter
        contents.putRaw("aString", "My favorite first string");
        contents.putRaw("aBoolean", true);
        contents.putRaw("aDate", "2001-10-26");
        contents.putRaw("aDateTime", "2001-10-26T21:32:52");
        contents.putRaw("aDuration", "PT2S");
        contents.putRaw("aGDay", "---03");
        contents.putRaw("aGMonthDay", "--05-01");
        contents.putRaw("aGMonth", "--05");
        contents.putRaw("aGYear", "-2003");
        contents.putRaw("aGYearMonth", "2001-10");
        contents.putRaw("anInteger", 10);
        contents.putRaw("aDouble", 2.0);
        contents.putRaw("aDecimal", 0.1);
        return contents;
    }

    @Test
    public void testBasicCaseFileDefinitionInput() {
        // TODO: the initial command results in an exception on setting the parameter (we do also a NegativeTest).
        // However, internally the engine has already processed the setting of the definition. So this need not be done again.
        // But we do in the next startCase statement. Subsequently the engine complains that the case definition has already been set.
        // We overcome this failure by starting with a new case id.
        // However, instead of that behavior we should make the engine allow for properly restarting the case and allowing us to use the same id.
        // TODO: put this in a different test. Here we're testing case file, not engine robustness

        String caseInstanceId = new Guid().toString();
        TestScript testCase = new TestScript(caseName);

        ValueMap inputs = getInputs();
        StartCase startCase = new StartCase(testUser, caseInstanceId, definitions, inputs.cloneValueNode(), null);
        testCase.addStep(startCase, caseFile -> caseFile.assertCaseFileItem(allPropertyTypesPath).assertValue(getContents(inputs)));

        testCase.runTest();
    }

    @Test
    public void testInvalidInputParameterName() {
        TestScript testCase = new TestScript(caseName);
        String caseInstanceId = "CaseFileDefinitionTest";

        // First we do a test with wrong input parameter names
        String wrongParameterName = "wrong-inputCaseFile";

        ValueMap inputs = getInputs();
        Value<?> contents = inputs.getValue().remove(inputParameterName);
        inputs.put(wrongParameterName, contents);

        StartCase startCase = new StartCase(testUser, caseInstanceId, definitions, inputs.cloneValueNode(), null);
        testCase.assertStepFails(startCase, action -> action.assertException(Exception.class, "An input parameter with name " + wrongParameterName + " is not defined in the case"));

        testCase.runTest();
    }

    @Test
    public void testInvalidInputProperties() {
        TestScript testCase = new TestScript(caseName);

        // Now test a whole series of invalid values for the properties.
        // Most notably this ought to check the StringValue match validations for date/time.
        // But furthermore also number validations.
        testInvalidValueForProperty(testCase, "aBoolean", "string that is not a boolean");
        testInvalidValueForProperty(testCase, "aDate", "2001-10-32"); // Putting a wrong date
        testInvalidValueForProperty(testCase, "aDate", "2001-10-31T"); // Putting a wrong date
        testInvalidValueForProperty(testCase, "aDateTime", "2001-10-26T"); // Putting a wrong date, nothing behind the T
        testInvalidValueForProperty(testCase, "aDuration", "PT2MS");
        testInvalidValueForProperty(testCase, "aGDay", "---32"); // Putting invalid day
        testInvalidValueForProperty(testCase, "aGMonthDay", "--13-01"); // Putting invalid month
        testInvalidValueForProperty(testCase, "aGMonth", "---13"); // Putting a day instead of a month
        testInvalidValueForProperty(testCase, "aGYear", "---03"); // Putting a day instead of a year
        testInvalidValueForProperty(testCase, "aGYearMonth", "--05"); // Putting a month instead of year+month
        testInvalidValueForProperty(testCase, "anInteger", 2.0); // Putting a decimal instead of int

        testCase.runTest();
    }

    private void testInvalidValueForProperty(TestScript testCase, String propertyName, Object propertyValue) {
        ValueMap inputs = getInputs();
        ValueMap contents = getContents(inputs);
        // Make a clone of contents and enter the wrong property
        ValueMap wrongContents = contents.cloneValueNode();
        wrongContents.putRaw(propertyName, propertyValue);
        // And now clone the inputs and put the wrong parameter in it
        ValueMap wrongInputs = inputs.cloneValueNode();
        wrongInputs.put(inputParameterName, wrongContents);

        String caseInstanceId = "TestingWrongProperty" + propertyName + "_" + new Guid(); // Make a new CaseInstanceId to overcome framework
        // limitation.
        StartCase startCase = new StartCase(testUser, caseInstanceId, definitions, wrongInputs.cloneValueNode(), null);
        testCase.assertStepFails(startCase, action -> action.assertException(CaseFileError.class, "Property '" + propertyName + "' has wrong type"));
    }

    @Test
    public void testComplexInputParameter() {
        TestScript testCase = new TestScript(caseName);

        ValueMap inputs = getInputs();
        ValueMap contents = getContents(inputs);

        // Now start a case with a child being set within the JSON input
        ValueMap childItem = new ValueMap();
        childItem.putRaw("aChildString", "child-string");
        childItem.putRaw("aSecondChildString", "child-string-2");

        contents.put("ChildItem", childItem);
        String caseInstanceId = new Guid().toString();
        StartCase startCase = new StartCase(testUser, caseInstanceId, definitions, inputs.cloneValueNode(), null);
        testCase.addStep(startCase, caseFile -> caseFile.assertCaseFileItem(childItemPath).assertValue(childItem));

        testCase.runTest();
    }

    @Test
    public void testBasicOperations() {
        TestScript testCase = new TestScript(caseName);

        ValueMap inputs = getInputs();
        String caseInstanceId = new Guid().toString();
        StartCase startCase = new StartCase(testUser, caseInstanceId, definitions, inputs.cloneValueNode(), null);
        testCase.addStep(startCase, caseFile -> caseFile.assertCaseFileItem(childItemPath).assertValue(Value.NULL));

        // Now start a case with a child being set within the JSON input
        ValueMap childItem = new ValueMap();
        childItem.putRaw("aChildString", "child-string");
        childItem.putRaw("aSecondChildString", "child-string-2");
        CreateCaseFileItem createChild = new CreateCaseFileItem(testUser, caseInstanceId, childItem.cloneValueNode(), childItemPath);
        testCase.addStep(createChild, caseFile -> caseFile.assertCaseFileItem(childItemPath).assertValue(childItem));

        // Now update the child item with new content: one property has a new value, another property is not updated, and a third property is newly
        // inserted
        // Effectively this should merge old and new content.
        ValueMap updatedContent = new ValueMap();
        updatedContent.putRaw("aChildString", "aNewChildString");
        updatedContent.putRaw("aChildInteger", 9);

        // Create a clone an merge old and new content to create a new object that is expected to be the result of the UpdateCaseFileItem operation
        ValueMap childItemClone = childItem.cloneValueNode();
        final ValueMap expectedChildContent = childItemClone.merge(updatedContent).asMap();

        UpdateCaseFileItem updateChild = new UpdateCaseFileItem(testUser, caseInstanceId, updatedContent.cloneValueNode(), childItemPath);
        testCase.addStep(updateChild, caseFile -> caseFile.assertCaseFileItem(childItemPath).assertValue(expectedChildContent));

        // Now replace the child item.
        ValueMap replacedChildItem = new ValueMap();
        replacedChildItem.putRaw("aChildInteger", 3);
        ReplaceCaseFileItem replaceChild = new ReplaceCaseFileItem(testUser, caseInstanceId, replacedChildItem.cloneValueNode(), childItemPath);
        testCase.addStep(replaceChild, caseFile -> caseFile.assertCaseFileItem(childItemPath).assertValue(replacedChildItem));

        // Now delete the child item.
        DeleteCaseFileItem deleteChild = new DeleteCaseFileItem(testUser, caseInstanceId, childItemPath);
        testCase.addStep(deleteChild, caseFile -> caseFile.assertCaseFileItem(childItemPath).assertValue(Value.NULL));

        testCase.runTest();
    }

    @Test
    public void testBasicOperationFailures() {
        TestScript testCase = new TestScript(caseName);

        ValueMap inputs = getInputs();
        String caseInstanceId = new Guid().toString();
        StartCase startCase = new StartCase(testUser, caseInstanceId, definitions, inputs.cloneValueNode(), null);
        testCase.addStep(startCase, caseFile -> caseFile.assertCaseFileItem(this.childItemPath).assertValue(Value.NULL));

        // Create a child item; child item contains an invalid value, so it should reject the operation, and child should still be null
        ValueMap childItem = new ValueMap();
        childItem.putRaw("aChildString", "child-string");
        childItem.putRaw("aChildInteger", "I should be an integer");
        CreateCaseFileItem createChild = new CreateCaseFileItem(testUser, caseInstanceId, childItem.cloneValueNode(), childItemPath);
        testCase.assertStepFails(createChild);

        // Update the child item; Should fail because child is still not created
        ValueMap updatedContent = new ValueMap();
        updatedContent.putRaw("aChildString", "aNewChildString");
        updatedContent.putRaw("aChildInteger", "I should be an integer");
        
        UpdateCaseFileItem updateChild = new UpdateCaseFileItem(testUser, caseInstanceId, updatedContent.cloneValueNode(), childItemPath);
        testCase.assertStepFails(updateChild);

        // Replace the child item; Should fail because child is still not created
        ValueMap replacedChildItem = new ValueMap();
        replacedChildItem.putRaw("aChildInteger", "I should be an integer");
        ReplaceCaseFileItem replaceChild = new ReplaceCaseFileItem(testUser, caseInstanceId, replacedChildItem.cloneValueNode(), childItemPath);
        testCase.assertStepFails(replaceChild);

        // Now delete the child item.
        DeleteCaseFileItem deleteChild = new DeleteCaseFileItem(testUser, caseInstanceId, childItemPath);
        testCase.assertStepFails(deleteChild);

        // Now create a proper child and then we should fail in updating and replacing it

        // Create a child item; child item contains an invalid value, so it should reject the operation, and child should still be null
        childItem.putRaw("aChildString", "child-string");
        childItem.putRaw("aChildInteger", 9);
        createChild = new CreateCaseFileItem(testUser, caseInstanceId, childItem.cloneValueNode(), childItemPath);
        testCase.addStep(createChild, caseFile -> caseFile.assertCaseFileItem(this.childItemPath).assertValue(childItem));

        // Again try to update the child item; Should fail because command has invalid content
        updateChild = new UpdateCaseFileItem(testUser, caseInstanceId, updatedContent.cloneValueNode(), this.childItemPath);
        testCase.assertStepFails(updateChild);

        replaceChild = new ReplaceCaseFileItem(testUser, caseInstanceId, replacedChildItem.cloneValueNode(), this.childItemPath);
        // And again try to replace the child item; Should fail because command has invalid content
        testCase.assertStepFails(replaceChild);

        deleteChild = new DeleteCaseFileItem(testUser, caseInstanceId, this.childItemPath);
        // Now delete the child item again. That should work.
        testCase.addStep(deleteChild, casePlan -> casePlan.assertCaseFileItem(this.childItemPath).assertValue(Value.NULL));

        testCase.runTest();
    }

    @Test
    public void testArrayOperations() {
        TestScript testCase = new TestScript(caseName);
        ValueMap inputs = getInputs();
        String caseInstanceId = new Guid().toString();
        StartCase startCase = new StartCase(testUser, caseInstanceId, definitions, inputs.cloneValueNode(), null);
        testCase.addStep(startCase, caseFile -> caseFile.assertCaseFileItem(childItemPath).assertValue(Value.NULL));

        // Now start a case with a child being set within the JSON input
        ValueMap childItem = new ValueMap();
        childItem.putRaw("aChildString", "child-string");
        childItem.putRaw("aSecondChildString", "child-string-2");

        ValueList arrayOfChildItem = new ValueList();
        arrayOfChildItem.add(childItem.cloneValueNode());

        CreateCaseFileItem createChild = new CreateCaseFileItem(testUser, caseInstanceId, childItem.cloneValueNode(), childArrayPath);
        testCase.addStep(createChild, caseFile -> caseFile.assertCaseFileItem(childArrayPath).assertValue(arrayOfChildItem).assertArrayElement(0).assertValue(childItem));

        // Now update the child item with new content: one property has a new value, another property is not updated, and a third property is newly
        // inserted
        // Effectively this should merge old and new content.
        ValueMap updatedContent = new ValueMap();
        updatedContent.putRaw("aChildString", "aNewChildString");
        updatedContent.putRaw("aChildInteger", 9);

        // Create a clone an merge old and new content to create a new object that is expected to be the result of the UpdateCaseFileItem operation
        ValueMap childItemClone = childItem.cloneValueNode();
        final ValueMap expectedChildContent = childItemClone.merge(updatedContent);

        UpdateCaseFileItem updateChild = new UpdateCaseFileItem(testUser, caseInstanceId, updatedContent.cloneValueNode(), child0);
        testCase.addStep(updateChild, caseFile -> caseFile.assertCaseFileItem(child0).assertValue(expectedChildContent));

        // Now replace the child item.
        ValueMap replacedChildItem = new ValueMap();
        replacedChildItem.putRaw("aChildInteger", 3);
        ReplaceCaseFileItem replaceChild = new ReplaceCaseFileItem(testUser, caseInstanceId, replacedChildItem.cloneValueNode(), child0);
        testCase.addStep(replaceChild, caseFile -> caseFile.assertCaseFileItem(child0).assertValue(replacedChildItem));

        // Now delete the child item.
        DeleteCaseFileItem deleteChild = new DeleteCaseFileItem(testUser, caseInstanceId, child0);
        testCase.addStep(deleteChild, caseFile -> caseFile.assertCaseFileItem(child0).assertValue(Value.NULL));

        testCase.runTest();
    }

    @Test
    public void testMoreArrayOperations() {
        TestScript testCase = new TestScript(caseName);

        ValueMap inputs = getInputs();
        String caseInstanceId = new Guid().toString();
        StartCase startCase = new StartCase(testUser, caseInstanceId, definitions, inputs.cloneValueNode(), null);
        testCase.addStep(startCase, caseFile -> caseFile.assertCaseFileItem(childItemPath).assertValue(Value.NULL));

        // Now start a case with a child being set within the JSON input
        ValueMap childItem = new ValueMap();
        childItem.putRaw("aChildString", "child-string");
        childItem.putRaw("aSecondChildString", "child-string-2");

        ValueList arrayOfChildItem = new ValueList();
        arrayOfChildItem.add(childItem.cloneValueNode());

        CreateCaseFileItem createChild = new CreateCaseFileItem(testUser, caseInstanceId, childItem.cloneValueNode(), childArrayPath);
        testCase.addStep(createChild, caseFile -> caseFile.assertCaseFileItem(childArrayPath).assertValue(arrayOfChildItem).assertArrayElement(0).assertValue(childItem));

        ValueList secondArray = arrayOfChildItem.cloneValueNode();
        secondArray.add(childItem.cloneValueNode());

        // add another child
        testCase.addStep(new CreateCaseFileItem(testUser, caseInstanceId, childItem.cloneValueNode(), childArrayPath), caseFile -> {
            caseFile.assertCaseFileItem(childArrayPath).assertSize(2).assertValue(secondArray);
            caseFile.assertCaseFileItem(child0).assertValue(childItem);
            caseFile.assertCaseFileItem(child1).assertValue(childItem);
            // TODO When try to a access non index value it will return IndexOutOfBoundsException.
            //caseFile.assertCaseFileItems(childArrayPath).assertValue(Value.NULL, 2);
        });


        // Now update the child item with new content: one property has a new value, another property is not updated, and a third property is newly
        // inserted
        // Effectively this should merge old and new content.
        ValueMap updatedContent = new ValueMap();
        updatedContent.putRaw("aChildString", "aNewChildString");
        updatedContent.putRaw("aChildInteger", 9);

        // Create a clone an merge old and new content to create a new object that is expected to be the result of the UpdateCaseFileItem operation
        ValueMap childItemClone = childItem.cloneValueNode();
        final ValueMap expectedChildContent = childItemClone.merge(updatedContent);

        UpdateCaseFileItem updateChild = new UpdateCaseFileItem(testUser, caseInstanceId, updatedContent.cloneValueNode(), child0);
        testCase.addStep(updateChild, caseFile -> caseFile.assertCaseFileItem(child0).assertValue(expectedChildContent));

        // Now replace the child item.
        ValueMap replacedChildItem = new ValueMap();
        replacedChildItem.putRaw("aChildInteger", 3);
        ReplaceCaseFileItem replaceChild = new ReplaceCaseFileItem(testUser, caseInstanceId, replacedChildItem.cloneValueNode(), child0);
        testCase.addStep(replaceChild, caseFile -> caseFile.assertCaseFileItem(child0).assertValue(replacedChildItem));

        // Now delete the child item.
        DeleteCaseFileItem deleteChild = new DeleteCaseFileItem(testUser, caseInstanceId, child0);
        testCase.addStep(deleteChild, caseFile -> caseFile.assertCaseFileItem(child0).assertValue(Value.NULL));

        testCase.runTest();
    }
}
