/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.client.test.inventory;

import org.hawkular.client.core.ClientResponse;
import org.hawkular.client.test.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"inventory"}, dependsOnGroups = "inventory-bulkcreate", enabled = false)
public class GraphTest extends BaseTest {

    @Test(enabled = false)
    public void getGraph() {
        //TODO: https://gist.github.com/garethahealy/0a7d7403f329d8768ab4cab4dcf2c409

        ClientResponse<String> response = client()
            .inventory()
            .graph()
            .getGraph();

        Assert.assertTrue(response.isSuccess(), response.getErrorMsg());
        Assert.assertNotNull(response.getEntity());
    }
}
