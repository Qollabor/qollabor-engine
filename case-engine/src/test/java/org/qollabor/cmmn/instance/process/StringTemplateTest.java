/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.instance.process;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.cmmn.test.TestScript;
import org.qollabor.util.StringTemplate;
import org.junit.Test;

public class StringTemplateTest {

    public StringTemplateTest() {
        TestScript.debugMessage("Running StringTemplateTest");
    }

    @Test
    public void testURL() throws MalformedURLException {
        ValueMap parameters = new ValueMap();
        parameters.putRaw("caseInstanceId", "12");

        StringTemplate stringTemplate = new StringTemplate("http://localhost/cases/${caseInstanceId}");
        URL actualURL = stringTemplate.resolveParameters(parameters).toURL();
        assertEquals(new URL("http://localhost/cases/12"), actualURL);
    }


    @Test
    public void test() throws MalformedURLException {
        ValueMap parameters = new ValueMap();
        parameters.putRaw("httpMethod", "POST");

        StringTemplate stringTemplate = new StringTemplate("-${httpMethod}-");
        String result = stringTemplate.resolveParameters(parameters).toString();
        assertEquals("-POST-", result);
    }
}
