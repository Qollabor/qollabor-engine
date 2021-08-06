/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.akka.command;

import org.qollabor.akka.actor.identity.TenantUser;
import org.qollabor.cmmn.akka.command.response.CaseResponse;
import org.qollabor.cmmn.akka.command.response.GetDiscretionaryItemsResponse;
import org.qollabor.akka.actor.serialization.Manifest;
import org.qollabor.cmmn.definition.DiscretionaryItemDefinition;
import org.qollabor.cmmn.instance.Case;
import org.qollabor.cmmn.instance.DiscretionaryItem;
import org.qollabor.akka.actor.serialization.json.StringValue;
import org.qollabor.akka.actor.serialization.json.ValueList;
import org.qollabor.akka.actor.serialization.json.ValueMap;

import java.util.ArrayList;
import java.util.List;

/**
 */
@Manifest
public class GetDiscretionaryItems extends CaseCommand {
    /**
     * This will retrieve a list of valid discretionary items
     * 
     * @param caseInstanceId
     *          The id of the case instance to get the discretionary items for
     */
    public GetDiscretionaryItems(TenantUser tenantUser, String caseInstanceId) {
        super(tenantUser, caseInstanceId);
    }

    public GetDiscretionaryItems(ValueMap json) {
        super(json);
    }

    @Override
    public CaseResponse process(Case caseInstance) {
        // Get the list of valid items
        List<DiscretionaryItem> discretionaryItems = new ArrayList();
        caseInstance.getDiscretionaryItems().stream().distinct().forEach(d -> discretionaryItems.add(d));

        // Convert the response to JSON
        ValueMap node = new ValueMap();
        node.put("caseInstanceId", new StringValue(getCaseInstanceId()));
        node.put("name", new StringValue(caseInstance.getDefinition().getName()));
        ValueList items = node.withArray("discretionaryItems");

        // discretionaryItems.stream().forEach(i -> items.put(new JSONObject().put("name", i.getDefinition().getName())));
        discretionaryItems.stream().forEach(i -> {
            DiscretionaryItemDefinition definition = i.getDefinition();
            String name = definition.getName();
            String definitionId = definition.getId();
            String type = i.getType();
            String parentName = definition.getPlanItemDefinition().getParentElement().getName();
            String parentType = definition.getPlanItemDefinition().getParentElement().getType();
            String parentId = i.getParentId();

            items.add(new ValueMap("name", name, "definitionId", definitionId, "type", type, "parentName", parentName, "parentType", parentType, "parentId", parentId));
//
//            ValueMap di = new ValueMap();
//
//
//            di.put("name", new StringValue(definition.getName()));
//            di.put("definitionId", new StringValue(definition.getId()));
//            di.put("id", new StringValue(definition.getId()));
//            di.put("type", new StringValue(i.getType()));
//            di.put("parentName", new StringValue(definition.getPlanItemDefinition().getParentElement().getName()));
//            di.put("parentType", new StringValue(definition.getPlanItemDefinition().getParentElement().getType()));
//            di.put("parentId", new StringValue(i.getParentId()));
//            items.add(di);
        });

        return new GetDiscretionaryItemsResponse(this, node);
    }

}
