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
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.hippoecm.repository.impl.SessionDecorator;

public class NodeTypeDefUtils {
    private NodeTypeDefUtils() {
        // ignore
    }

    public static void createNodeType(Session session, String nodeType) throws RepositoryException {
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        if (!nodeTypeManager.hasNodeType(nodeType)) {
            registerNodeOrMixin(session, nodeType, "nt:unstructured", false);
        }
    }

    public static void createNodeType(Session session, String nodeType, String superType) throws RepositoryException {
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        createNodeType(session, superType);
        if (!nodeTypeManager.hasNodeType(nodeType)) {
            registerNodeOrMixin(session, nodeType, superType, false);
        }
    }

    public static void createMixin(Session session, String mixinType) throws RepositoryException {
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        if (!nodeTypeManager.hasNodeType(mixinType)) {
            registerNodeOrMixin(session, mixinType, "nt:unstructured", true);
        }
    }

    public static void createMixin(Session session, String mixinType, String superType) throws RepositoryException {
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        createMixin(session, superType);
        if (!nodeTypeManager.hasNodeType(mixinType)) {
            registerNodeOrMixin(session, mixinType, superType, true);
        }
    }

    public static String getOrRegisterNamespace(Session session, String name) throws RepositoryException {
        String uri = null;
        if (name.indexOf(':') > -1) {
            String prefix = name.substring(0, name.indexOf(':'));
            NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
            try {
                uri = namespaceRegistry.getURI(prefix);
            } catch (RepositoryException e) {
                uri = "https://www.openweb.nl/" + prefix + "/nt/1.0";
                namespaceRegistry.registerNamespace(prefix, uri);
            }
        }
        return uri;
    }


    private static void registerNodeOrMixin(Session session, String nodeType, String superType, boolean isMixin) throws RepositoryException {
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        String uri = getOrRegisterNamespace(session, nodeType);
        NameFactory nameFactory = NameFactoryImpl.getInstance();
        Name name = nameFactory.create(uri, getLocalName(nodeType));
        ValueFactory valueFactory = session.getValueFactory();
        Name[] supertypes;
        String namespace = getOrRegisterNamespace(session, superType);
        supertypes = new Name[]{nameFactory.create(namespace, getLocalName(superType))};
        QNodeTypeDefinitionImpl ntd = new QNodeTypeDefinitionImpl(name, supertypes, new Name[0], isMixin, false, true, true, null, new QPropertyDefinition[0], new QNodeDefinition[0]);
        NamePathResolver resolver = getNamePathResolver(session);
        NodeTypeDefinition nodeTypeDefinition = new NodeTypeDefinitionImpl(ntd, resolver, valueFactory);
        nodeTypeManager.registerNodeType(nodeTypeDefinition, false);
    }

    private static NamePathResolver getNamePathResolver(Session session) {
        NamePathResolver resolver = null;
        Session unwrapped = session;
        if (session instanceof SessionDecorator) {
            unwrapped = SessionDecorator.unwrap(session);
        }
        if (unwrapped instanceof NamePathResolver) {
            resolver = (NamePathResolver) unwrapped;
        }
        return resolver;
    }

    private static String getLocalName(String nodeType) {
        return nodeType.substring(nodeType.indexOf(':') + 1);
    }


}
