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

package nl.openweb.jcr.utils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.junit.Test;

import nl.openweb.jcr.InMemoryJcrRepository;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Ebrahim Aharpour
 * @since 9/20/2017
 */
public class NodeTypeDefUtilsTest {
    private static final String NODE_TYPE = "mysn:type";
    private static final String MIXIN_TYPE = "mysn:mixin";
    private static final String PARENT_MIXIN_TYPE = "mysn:parentMixin";
    private static final String PARENT_NODE_TYPE = "mysn:parentType";
    private static final String OTHER_NAMESPACE_PARENT_MIXIN_TYPE = "otherns:parentMixin";
    private static final String OTHER_NAMESPACE_PARENT_NODE_TYPE = "otherns:parentType";

    @Test
    public void createNodeType() throws Exception {
        try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            NodeTypeDefUtils.createNodeType(session, NODE_TYPE);
            Node node = session.getRootNode().addNode("test", NODE_TYPE);
            assertTrue(node.isNodeType(NODE_TYPE));
        }
    }

    @Test
    public void createNodeTypeWithSuperType() throws Exception {
        try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            testCreatingNodeTypeWithSuperType(PARENT_NODE_TYPE, session);
        }
    }

    @Test
    public void createNodeTypeWithRegisteredSuperType() throws Exception {
        try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            NodeTypeDefUtils.createNodeType(session, OTHER_NAMESPACE_PARENT_NODE_TYPE);
            testCreatingNodeTypeWithSuperType(OTHER_NAMESPACE_PARENT_NODE_TYPE, session);
        }
    }

    @Test
    public void createNodeTypeWithSuperTypeOfAnotherNamespace() throws Exception {
        try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            testCreatingNodeTypeWithSuperType(OTHER_NAMESPACE_PARENT_NODE_TYPE, session);
        }
    }

    @Test
    public void createMixin() throws Exception {
        try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            NodeTypeDefUtils.createNodeType(session, NODE_TYPE);
            NodeTypeDefUtils.createMixin(session, MIXIN_TYPE);
            Node node = session.getRootNode().addNode("test", NODE_TYPE);
            node.addMixin(MIXIN_TYPE);
            assertTrue(node.isNodeType(NODE_TYPE));
            assertTrue(node.isNodeType(MIXIN_TYPE));
            session.save();
        }
    }

    @Test
    public void createMixinWithSuperType() throws Exception {
        try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            testCreatingMixinWithSuperType(PARENT_MIXIN_TYPE, session);
        }
    }
    @Test
    public void createMixinWithRegisteredSuperType() throws Exception {
        try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            NodeTypeDefUtils.createMixin(session, PARENT_MIXIN_TYPE);
            testCreatingMixinWithSuperType(PARENT_MIXIN_TYPE, session);
        }
    }

    @Test
    public void createMixinWithSuperTypeOfAnotherNamespace() throws Exception {
        try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            testCreatingMixinWithSuperType(OTHER_NAMESPACE_PARENT_MIXIN_TYPE, session);
        }
    }

    @Test
    public void getOrRegisterNamespace() throws Exception {
        try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            NodeTypeDefUtils.createNodeType(session, NODE_TYPE);
            Node node = session.getRootNode().addNode("test", NODE_TYPE);
            NodeTypeDefUtils.getOrRegisterNamespace(session, "test:test");
            node.setProperty("test:test", "test");
        }
    }

    private void testCreatingMixinWithSuperType(String parentMixinType, Session session) throws RepositoryException {
        NodeTypeDefUtils.createNodeType(session, NODE_TYPE);
        NodeTypeDefUtils.createMixin(session, MIXIN_TYPE, parentMixinType);
        Node node = session.getRootNode().addNode("test", NODE_TYPE);
        node.addMixin(MIXIN_TYPE);
        assertTrue(node.isNodeType(NODE_TYPE));
        assertTrue(node.isNodeType(MIXIN_TYPE));
        assertTrue(node.isNodeType(parentMixinType));
        session.save();
    }

    private void testCreatingNodeTypeWithSuperType(String parentNodeType, Session session) throws RepositoryException {
        NodeTypeDefUtils.createNodeType(session, NODE_TYPE, parentNodeType);
        Node node = session.getRootNode().addNode("test", NODE_TYPE);

        // making sure that new time is unstructured.
        node.setProperty("propertyName", "propertyValue");

        assertTrue(node.isNodeType(NODE_TYPE));
        assertTrue(node.isNodeType(parentNodeType));
        Node nodeOfParentType = session.getRootNode().addNode("test2", parentNodeType);
        assertFalse(nodeOfParentType.isNodeType(NODE_TYPE));
        assertTrue(nodeOfParentType.isNodeType(parentNodeType));
        session.save();
    }

}