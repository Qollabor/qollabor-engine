/*
 * Copyright 2014 - 2019 Cafienne B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cafienne.cmmn.akka.command.casefile;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.akka.actor.identity.TenantUser;
import org.cafienne.akka.actor.serialization.Fields;
import org.cafienne.akka.actor.serialization.json.Value;
import org.cafienne.akka.actor.serialization.json.ValueMap;
import org.cafienne.cmmn.akka.command.CaseCommand;
import org.cafienne.cmmn.akka.command.response.CaseResponse;
import org.cafienne.cmmn.instance.Case;
import org.cafienne.cmmn.instance.casefile.CaseFile;
import org.cafienne.cmmn.instance.casefile.CaseFileItemCollection;
import org.cafienne.cmmn.instance.casefile.CaseFileItemTransition;
import org.cafienne.cmmn.instance.casefile.Path;

import java.io.IOException;

/**
 * Holds some generic validation and processing behavior for CaseFile operations.
 */
abstract public class CaseFileCommand extends CaseCommand {
    /**
     * Determine path and content for the CaseFileItem to be touched.
     *
     * @param caseInstanceId     The id of the case in which to perform this command.
     */
    protected CaseFileCommand(TenantUser tenantUser, String caseInstanceId) {
        super(tenantUser, caseInstanceId);
    }

    protected CaseFileCommand(ValueMap json) {
        super(json);
    }

    @Override
    public void validate(Case caseInstance) {
        // First do the validation in the super class. Then only our own, but also only if there were no validation errors from the super class.
        super.validate(caseInstance);
    }

    @Override
    public CaseResponse process(Case caseInstance) {
        return apply(caseInstance, caseInstance.getCaseFile());
    }

    abstract protected CaseResponse apply(Case caseInstance, CaseFile caseFile);

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
    }
}
