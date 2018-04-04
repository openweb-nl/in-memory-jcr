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


import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.hippoecm.repository.impl.DecoratorFactoryImpl;
import org.hippoecm.repository.jackrabbit.RepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class InMemoryJcrRepository implements Repository, AutoCloseable {

    private final RepositoryImpl originalRepository;
    private final Repository repository;
    private final File repositoryFolder;

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryJcrRepository.class);

    public InMemoryJcrRepository() throws RepositoryException, IOException {
        InputStream configFile = InMemoryJcrRepository.class.getClassLoader().getResourceAsStream("configuration.xml");
        this.repositoryFolder = Files.createTempDirectory("repository-").toFile();
        RepositoryConfig config = RepositoryConfig.create(configFile, this.repositoryFolder.getAbsolutePath());
        this.originalRepository = new HippoRepository(config);
        this.repository = new DecoratorFactoryImpl().getRepositoryDecorator(originalRepository);
    }

    public void shutdown() throws IOException {
        originalRepository.shutdown();
        FileUtils.deleteDirectory(repositoryFolder);
    }

    @Override
    public Session login(Credentials credentials, String workspaceName) throws RepositoryException {
        return repository.login(credentials, workspaceName);
    }

    @Override
    public String getDescriptor(String key) {
        return repository.getDescriptor(key);
    }

    @Override
    public String[] getDescriptorKeys() {
        return repository.getDescriptorKeys();
    }

    @Override
    public Value getDescriptorValue(String key) {
        return repository.getDescriptorValue(key);
    }

    @Override
    public Value[] getDescriptorValues(String key) {
        return repository.getDescriptorValues(key);
    }

    @Override
    public boolean isStandardDescriptor(String key) {
        return repository.isStandardDescriptor(key);
    }

    @Override
    public boolean isSingleValueDescriptor(String key) {
        return repository.isSingleValueDescriptor(key);
    }

    @Override
    public Session login() throws RepositoryException {
        return repository.login();
    }

    @Override
    public Session login(Credentials credentials) throws RepositoryException {
        return repository.login(credentials);
    }

    @Override
    public Session login(String workspace) throws RepositoryException {
        return repository.login(workspace);
    }

    @Override
    public void close() {
        try {
            shutdown();
        } catch (IOException e) {
            LOG.warn("Failed to removed temporary repository folder.", e);
        }
    }
}
