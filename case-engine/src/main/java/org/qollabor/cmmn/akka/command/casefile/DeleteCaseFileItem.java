/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.akka.command.casefile;

import org.qollabor.akka.actor.identity.TenantUser;
import org.qollabor.akka.actor.serialization.Manifest;
import org.qollabor.akka.actor.serialization.json.Value;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.cmmn.instance.Case;
import org.qollabor.cmmn.instance.casefile.CaseFileItemCollection;
import org.qollabor.cmmn.instance.casefile.CaseFileItemTransition;
import org.qollabor.cmmn.instance.casefile.Path;

/**
 * Deletes a case file item.
 */
@Manifest
public class DeleteCaseFileItem extends CaseFileItemCommand {
    /**
     * Deletes the case file item.
     *
     * @param caseInstanceId   The id of the case in which to perform this command.
     * @param path Path to the case file item to be created
     */
    public DeleteCaseFileItem(TenantUser tenantUser, String caseInstanceId, Path path) {
        super(tenantUser, caseInstanceId, Value.NULL, path, CaseFileItemTransition.Delete);
    }

    public DeleteCaseFileItem(ValueMap json) {
        super(json, CaseFileItemTransition.Delete);
    }

    @Override
    void apply(Case caseInstance, CaseFileItemCollection<?> caseFileItem, Value<?> content) {
        caseFileItem.deleteContent();
    }
}
