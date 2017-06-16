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


import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nl.openweb.jcr.utils.NodeTypeDefUtils;


import static org.junit.Assert.*;

public class InMemoryJcrRepositoryTest {

    private Node rootNode;
    private InMemoryJcrRepository inMemoryJcrRepository;

    @Before
    public void init() throws IOException, RepositoryException, URISyntaxException {
        inMemoryJcrRepository = new InMemoryJcrRepository();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("node.json")) {
            Importer importer = new Importer.Builder(() -> {
                Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                return session.getRootNode();
            }).addMixins(true).addUuid(false).addUnknownTypes(true).build();

            rootNode = importer.createNodesFromJson(inputStream);
        }
    }

    @After
    public void shutdown() throws IOException {
        inMemoryJcrRepository.shutdown();
    }

    @Test
    public void nodeTypeTest() throws RepositoryException {
        assertTrue(rootNode.getNode("content").isNodeType("hippostd:folder"));
    }

    @Test
    public void identifierTest() throws RepositoryException {
        assertEquals("cafebabe-cafe-babe-cafe-babecafebabe", rootNode.getIdentifier());
    }

    @Test
    public void pathTest() throws RepositoryException {
        Node node = rootNode.getNode("content/assets");
        assertTrue(rootNode.hasNode("content/assets"));
        assertEquals("/content/assets", node.getPath());
    }

    @Test
    public void iterationTest() throws RepositoryException {
        Node content = rootNode.getNode("content");

        for (NodeIterator nodes = content.getNodes(); nodes.hasNext(); ) {
            Node node = nodes.nextNode();
            MatcherAssert.assertThat("Path is not what we expect!", node.getPath(),
                    Matchers.is(Matchers.oneOf("/content/documents", "/content/gallery", "/content/assets",
                            "/content/sameNameSiblings", "/content/sameNameSiblings[2]")));
        }
    }

    @Test
    public void sessionTest() throws RepositoryException {
        assertNotNull(rootNode.getSession());
        assertNotNull(rootNode.getNode("content").getSession());
    }

    @Test
    public void workspaceTest() throws RepositoryException {
        assertNotNull(rootNode.getSession().getWorkspace());
    }

    @Test
    public void queryManagerTest() throws RepositoryException {
        assertNotNull(rootNode.getSession().getWorkspace().getQueryManager());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void xpathQueryTest() throws RepositoryException {
        QueryManager queryManager = rootNode.getSession().getWorkspace().getQueryManager();
        Query query = queryManager.createQuery("//element(*, hippostd:folder)", Query.XPATH);
        assertNotNull(query);
        QueryResult execute = query.execute();
        List<Node> results = new ArrayList<>();
        for (NodeIterator nodes = execute.getNodes(); nodes.hasNext(); ) {
            results.add(nodes.nextNode());
        }
        assertEquals(2, results.size());
    }

    @Test
    public void SQL2QueryTest() throws RepositoryException {
        QueryManager queryManager = rootNode.getSession().getWorkspace().getQueryManager();
        Query query = queryManager.createQuery("SELECT * FROM [hippostd:folder]", Query.JCR_SQL2);
        assertNotNull(query);
        QueryResult execute = query.execute();
        List<Node> results = new ArrayList<>();
        for (NodeIterator nodes = execute.getNodes(); nodes.hasNext(); ) {
            results.add(nodes.nextNode());
        }
        assertEquals(2, results.size());
    }

    @Test
    public void mixinTest() throws RepositoryException {
        Node node = rootNode.getNode("content/documents");
        Property property = node.getProperty("jcr:mixinTypes");
        for (Value v : property.getValues()) {
            MatcherAssert.assertThat("Unexpected mixin", v.getString(),
                    Matchers.is(Matchers.oneOf("hippo:named", "mix:referenceable")));
        }
        assertTrue(node.isNodeType("hippo:named"));
        assertNotNull(node.getMixinNodeTypes());

        assertTrue(rootNode.getNode("content").isNodeType("mix:referenceable"));
    }

    @Test
    public void addMixinTest() throws RepositoryException {
        Node node = rootNode.getNode("content/documents");
        NodeTypeDefUtils.createMixin(rootNode.getSession(), "my:mixin");
        node.addMixin("my:mixin");
        assertTrue(node.isNodeType("my:mixin"));
    }

    @Test
    public void propertyTest() throws RepositoryException {
        Node node = rootNode.getNode("content/documents/subnode");
        propertyBasicValidationForSingleValue(node, "singleValueString", PropertyType.STRING, "stringValue", Value::getString);
        propertyBasicValidationForMultiValue(node, "multivalueString", PropertyType.STRING, new Object[]{"stringValue01", "stringValue02", "stringValue03"}, Value::getString);
        propertyBasicValidationForSingleValue(node, "singleValueBoolean", PropertyType.BOOLEAN, true, Value::getBoolean);
        propertyBasicValidationForMultiValue(node, "multivalueBoolean", PropertyType.BOOLEAN, new Object[]{true, false, true}, Value::getBoolean);
        propertyBasicValidationForSingleValue(node, "singleValueLong", PropertyType.LONG, 20L, Value::getLong);
        propertyBasicValidationForMultiValue(node, "multivalueLong", PropertyType.LONG, new Object[]{2L, 6L, 232L}, Value::getLong);
    }

    @Test
    public void addPropertyTest() throws RepositoryException {
        Node node = rootNode.getNode("content/documents/subnode");
        node.setProperty("newProperty", "value");
        assertEquals("value", node.getProperty("newProperty").getString());
    }

    @Test
    public void addSubnodeTest() throws RepositoryException {
        Node node = rootNode.getNode("content/documents/subnode");
        Node subsubnode = node.addNode("subsubnode", "nt:unstructured");
        assertNotNull(subsubnode);
        assertEquals("/content/documents/subnode/subsubnode", subsubnode.getPath());
        assertTrue(subsubnode.isNodeType("nt:unstructured"));
    }

    @Test
    public void sameNameSiblingsTest() throws RepositoryException {
        Node sameNameSibling1 = rootNode.getNode("content/sameNameSiblings");
        Assert.assertNotNull(sameNameSibling1);
        propertyBasicValidationForSingleValue(sameNameSibling1, "singleValueString", PropertyType.STRING, "stringValue1", Value::getString);
        Node sameNameSibling2 = rootNode.getNode("content/sameNameSiblings[1]");
        Assert.assertNotNull(sameNameSibling2);
        propertyBasicValidationForSingleValue(sameNameSibling2, "singleValueString", PropertyType.STRING, "stringValue1", Value::getString);
    }

    private void propertyBasicValidationForSingleValue(Node node, String propertyName, int propertyType, Object expectedValue, ExtractValue extractor) throws RepositoryException {
        Property property = node.getProperty(propertyName);
        assertFalse(property.isMultiple());
        assertEquals(propertyType, property.getType());
        Value value = property.getValue();
        Object actualValue = extractor.apply(value);
        if (propertyType == PropertyType.DATE) {
            assertEquals(((Calendar) expectedValue).getTime().getTime(), ((Calendar) actualValue).getTime().getTime());
        } else {
            assertEquals(expectedValue, actualValue);
        }
    }

    private void propertyBasicValidationForMultiValue(Node node, String propertyName, int propertyType, Object[] expectedValues, ExtractValue extractor) throws RepositoryException {
        Property property = node.getProperty(propertyName);
        assertTrue(property.isMultiple());
        assertEquals(propertyType, property.getType());
        Value[] values = property.getValues();
        for (int i = 0; i < expectedValues.length; i++) {
            if (propertyType == PropertyType.DATE) {
                assertEquals(((Calendar) expectedValues[i]).getTime().getTime(), ((Calendar) extractor.apply(values[i])).getTime().getTime());
            } else {
                assertEquals(expectedValues[i], extractor.apply(values[i]));
            }
        }
    }

    interface ExtractValue {
        Object apply(Value value) throws RepositoryException;
    }
}
