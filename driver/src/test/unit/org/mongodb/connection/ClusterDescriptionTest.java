/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.connection;

import org.junit.Test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mongodb.connection.ClusterDescription.Mode.Discovering;
import static org.mongodb.connection.ServerType.ReplicaSetPrimary;
import static org.mongodb.connection.ServerType.ShardRouter;

public class ClusterDescriptionTest {
    @Test
    public void testMode() {
        ClusterDescription description = new ClusterDescription(Collections.<ServerDescription>emptyList(), 15, Discovering);
        assertEquals(Discovering, description.getMode());
    }

    @Test
    public void testType() throws UnknownHostException {
        ClusterDescription description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().address(new ServerAddress()).type(ReplicaSetPrimary).build()),
                15, Discovering);
        assertEquals(ClusterDescription.Type.ReplicaSet, description.getType());

        description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().address(new ServerAddress()).type(ShardRouter).build()),
                15, Discovering);
        assertEquals(ClusterDescription.Type.Sharded, description.getType());

        description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().address(new ServerAddress()).type(ServerType.StandAlone).build()),
                15, Discovering);
        assertEquals(ClusterDescription.Type.StandAlone, description.getType());
    }
}