package nl.openweb.jcr.utils;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeDefinitionImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.QNodeDefinition;
import org.apache.jackrabbit.spi.QPropertyDefinition;
import org.apache.jackrabbit.spi.commons.QNodeTypeDefinitionImpl;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;

public class NodeTypeUtils {
    private NodeTypeUtils() {
        // ignore
    }

    public static void createNodeType(Session session, String nodeType) throws RepositoryException {
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        if (!nodeTypeManager.hasNodeType(nodeType)) {
            registerNodeOrMixin(session, nodeType, false);
        }
    }

    public static void createMixin(Session session, String mixinType) throws RepositoryException {
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        if (!nodeTypeManager.hasNodeType(mixinType)) {
            registerNodeOrMixin(session, mixinType, true);
        }
    }

    private static void registerNodeOrMixin(Session session, String nodeType, boolean isMixin) throws RepositoryException {
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        String uri = getOrRegisterNamespace(session, nodeType);
        NameFactory nameFactory = NameFactoryImpl.getInstance();
        Name name = nameFactory.create(uri, nodeType.substring(nodeType.indexOf(":") + 1));
        ValueFactory valueFactory = session.getValueFactory();
        Name[] supertypes;
        if (!isMixin) {
            supertypes = new Name[]{nameFactory.create("http://www.jcp.org/jcr/nt/1.0", "unstructured")};
        } else {
            supertypes = new Name[0];
        }

        QNodeTypeDefinitionImpl ntd = new QNodeTypeDefinitionImpl(name, supertypes, new Name[0], isMixin, false, true, true, name, new QPropertyDefinition[0], new QNodeDefinition[0]);
        NodeTypeDefinition nodeTypeDefinition = new NodeTypeDefinitionImpl(ntd, (SessionImpl) session, valueFactory);
        nodeTypeManager.registerNodeType(nodeTypeDefinition, false);
    }

    private static String getOrRegisterNamespace(Session session, String nodeName) throws RepositoryException {
        String prefix = nodeName.substring(0, nodeName.indexOf(":"));
        NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
        String uri;
        try {
            uri = namespaceRegistry.getURI(prefix);
        } catch (RepositoryException e) {
            uri = "https://www.openweb.nl/" + prefix + "/nt/1.0";
            namespaceRegistry.registerNamespace(prefix, uri);
        }
        return uri;
    }
}
