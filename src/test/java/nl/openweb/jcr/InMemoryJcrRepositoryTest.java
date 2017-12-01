/*
 * Copyright 2017 Open Web IT B.V. (https://www.openweb.nl/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.openweb.jcr;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.junit.Test;

import nl.openweb.jcr.utils.NodeTypeDefUtils;

/**
 * @author Ebrahim Aharpour
 * @since 12/1/2017
 */
public class InMemoryJcrRepositoryTest {

    private static final String NODE_TYPE = "ns:MyNodeType";

    @Test
    public void creatingRepository() throws Exception {
        try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
            Session session = repository.login(
                    new SimpleCredentials("admin", "admin".toCharArray())
            );
            Node rootNode = session.getRootNode();
            NodeTypeDefUtils.createNodeType(session, NODE_TYPE);
            rootNode.addNode("mynode", NODE_TYPE);
            session.save();
        }

    }

}