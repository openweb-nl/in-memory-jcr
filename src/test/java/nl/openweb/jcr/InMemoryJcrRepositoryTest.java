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

import nl.openweb.jcr.utils.NodeTypeDefUtils;
import org.junit.jupiter.api.Test;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.io.IOException;
import java.net.URISyntaxException;

import static javax.jcr.query.Query.XPATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Ebrahim Aharpour
 * @since 12/1/2017
 */
public class InMemoryJcrRepositoryTest {

    private static final String NODE_TYPE = "ns:MyNodeType";
    private static final String NODE_NAME = "mynode";

    @Test
    public void creatingRepository() throws Exception {
        try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
            Session session = repository.login(
                new SimpleCredentials("admin", "admin".toCharArray())
            );
            addSampleNode(session);
        }
    }

    @Test
    public void search() throws IOException, RepositoryException, URISyntaxException {
        try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
            Session session = repository.login(
                new SimpleCredentials("admin", "admin".toCharArray())
            );
            addSampleNode(session);

            QueryManager queryManager = session.getWorkspace().getQueryManager();
            Query query = queryManager.createQuery("//element(*,ns:MyNodeType)", XPATH);
            QueryResult execute = query.execute();
            NodeIterator nodes = execute.getNodes();

            assertEquals(1, nodes.getSize());
            assertEquals(NODE_NAME, nodes.nextNode().getName());
        }
    }

    private void addSampleNode(Session session) throws RepositoryException {
        Node rootNode = session.getRootNode();
        NodeTypeDefUtils.createNodeType(session, NODE_TYPE);
        rootNode.addNode(NODE_NAME, NODE_TYPE);
        session.save();
    }

}
