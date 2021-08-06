/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.akka.command;

import com.fasterxml.jackson.core.JsonGenerator;
import org.qollabor.akka.actor.command.exception.InvalidCommandException;
import org.qollabor.akka.actor.identity.TenantUser;
import org.qollabor.akka.actor.serialization.Fields;
import org.qollabor.akka.actor.serialization.Manifest;
import org.qollabor.cmmn.akka.command.response.AddDiscretionaryItemResponse;
import org.qollabor.cmmn.akka.command.response.CaseResponse;
import org.qollabor.cmmn.definition.DiscretionaryItemDefinition;
import org.qollabor.cmmn.instance.Case;
import org.qollabor.cmmn.instance.DiscretionaryItem;
import org.qollabor.cmmn.instance.PlanItem;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.util.Guid;

import java.io.IOException;

/**
 * Adds a discretionary item to the case. This will only have effect if the discretionary item can actually be planned currently in the case.
 */
@Manifest
public class AddDiscretionaryItem extends CaseCommand {
    private final String name;
    private final String planItemId;
    private final String parentId;
    private final String definitionId;
    private transient DiscretionaryItem discretionaryItem;
    private transient PlanItem parentItem;

    /**
     * Create a command to add a new plan item to the case, based on a discretionary item definition with the specified name.
     *
     * @param caseInstanceId The id of the case in which to perform this command.
     * @param name           The name of the plan item to be added. This name must refer to a discretionary item in one of the planning tables of the case.
     * @param newPlanItemId  Optional plan item id with which the new plan item will be created. If the id is null or empty, a new {@link Guid} will be created.
     * @param definitionId The id of the discretionary item; can be used instead of the name.
     * @param parentId The id of the plan item that contains this discretionary item (i.e., the stage or human task in which it is being planned).
     */
    public AddDiscretionaryItem(TenantUser tenantUser, String caseInstanceId, String name, String definitionId, String parentId, String newPlanItemId) {
        super(tenantUser, caseInstanceId);
        this.name = name;
        this.planItemId = (newPlanItemId == null || newPlanItemId.isEmpty()) ? new Guid().toString() : newPlanItemId;
        this.definitionId = definitionId;
        this.parentId = parentId;
    }

    public AddDiscretionaryItem(ValueMap json) {
        super(json);
        this.name = readField(json, Fields.name);
        this.planItemId = readField(json, Fields.planItemId);
        this.definitionId = readField(json, Fields.definitionId);
        this.parentId = readField(json, Fields.parentId);
    }

    @Override
    public void validate(Case caseInstance) {
        super.validate(caseInstance);
        PlanItem alreadyExisting = caseInstance.getPlanItemById(planItemId);
        if (alreadyExisting != null) {
            throw new InvalidCommandException("Cannot plan a discretionary item named '" + name + "' with the specified id " + planItemId + ", because the case already has a plan item with that id");
        }
        parentItem = caseInstance.getPlanItemById(parentId);
        if (parentItem == null) {
            throw new InvalidCommandException("Cannot plan a discretionary item named '" + name + "' because the parent item with id '"+parentId+"' cannot be found in the case");
        }
        DiscretionaryItemDefinition definition = caseInstance.getDefinition().getElement(definitionId);
        discretionaryItem = definition.createInstance(parentItem);
        if (!discretionaryItem.isPlannable()) {
            throw new InvalidCommandException("Cannot plan a discretionary item named '" + name + "'. It may not be applicable at this moment");
        }

        if (!discretionaryItem.isAuthorized()) {
            throw new InvalidCommandException("No authorization available to plan a discretionary item named '" + name + "'");
        }
    }

    @Override
    public CaseResponse process(Case caseInstance) {
        discretionaryItem.plan(planItemId);
        return new AddDiscretionaryItemResponse(this, new ValueMap("planItemId", planItemId));
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.writeModelCommand(generator);
        writeField(generator, Fields.name, name);
        writeField(generator, Fields.planItemId, planItemId);
        writeField(generator, Fields.definitionId, definitionId);
        writeField(generator, Fields.parentId, parentId);
    }
}
