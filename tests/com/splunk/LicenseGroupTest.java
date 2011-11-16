/*
 * Copyright 2011 Splunk, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.splunk.sdk.tests.com.splunk;

import com.splunk.*;
import com.splunk.sdk.Command;
import com.splunk.Service;

import java.util.Arrays;
import java.util.List;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.*;

public class LicenseGroupTest extends TestCase {
    Command command;

    public LicenseGroupTest() {}

    Service connect() {
        return Service.connect(command.opts);
    }

    @Before public void setUp() {
        command = Command.splunk(); // Pick up .splunkrc settings
    }

    @Test public void testLicenseGroup() throws Exception {
        Service service = connect();

        EntityCollection<LicenseGroup> ds = service.getLicenseGroups();

        // list of stackids, empirically created
        List<String> stacks = Arrays.asList("forwarder", "enterprise", "free");
        for (LicenseGroup entity: ds.values()) {
            // enterprise, forwarder, free
            entity.get(); // force a read
            for (String id: entity.getStackIds()) {
                Assert.assertTrue(stacks.contains(id));
            }
            entity.isActive();
        }
    }
}
