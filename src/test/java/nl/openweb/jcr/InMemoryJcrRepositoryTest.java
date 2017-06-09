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
import org.junit.Before;
import org.junit.Test;

import nl.openweb.jcr.utils.NodeTypeUtils;


import static org.junit.Assert.*;

public class InMemoryJcrRepositoryTest {

    private Node rootNode;
    private InMemoryJcrRepository inMemoryJcrRepository;

    @Before
    public void init() throws IOException, RepositoryException, URISyntaxException {
        inMemoryJcrRepository = new InMemoryJcrRepository();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("node.json")) {
            Importer importer = new Importer.Builder(() -> {
                try {
                    Session session = inMemoryJcrRepository.login(new SimpleCredentials("admin", "admin".toCharArray()));
                    NodeTypeUtils.createNodeType(session, "hippostd:folder");
                    NodeTypeUtils.createNodeType(session, "hippogallery:stdImageGallery");
                    NodeTypeUtils.createNodeType(session, "hippogallery:stdAssetGallery");
                    NodeTypeUtils.createMixin(session, "hippo:named");
                    NodeTypeUtils.createMixin(session, "mix:referenceable");
                    NodeTypeUtils.createMixin(session, "my:mixin");
                    return session.getRootNode();
                } catch (RepositoryException e) {
                    throw new RuntimeException();
                }
            }).setAddMixins(true).setAddUuid(false).build();

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
                    Matchers.is(Matchers.oneOf("/content/documents", "/content/gallery", "/content/assets")));
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
    }

    @Test
    public void addMixinTest() throws RepositoryException {
        Node node = rootNode.getNode("content/documents");
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
