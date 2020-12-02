/* 
 * Copyright 2014 - 2019 Cafienne B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cafienne.cmmn.instance.process.http;

import org.cafienne.cmmn.definition.CMMNElementDefinition;
import org.cafienne.cmmn.definition.ModelDefinition;
import org.w3c.dom.Element;

public class HTTPCallDefinition extends org.cafienne.processtask.implementation.http.HTTPCallDefinition {
    public HTTPCallDefinition(Element element, ModelDefinition processDefinition, CMMNElementDefinition parentElement) {
        super(element, processDefinition, parentElement);
    }
}
