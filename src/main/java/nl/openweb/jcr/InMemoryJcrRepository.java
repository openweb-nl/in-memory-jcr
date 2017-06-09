package nl.openweb.jcr;


import javax.jcr.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

public class InMemoryJcrRepository implements Repository {

    private final RepositoryImpl repository;
    private final File repositoryFolder;

    public InMemoryJcrRepository() throws RepositoryException, URISyntaxException, IOException {
        File configFile = new File(InMemoryJcrRepository.class.getClassLoader().getResource("configuration.xml").toURI());
        this.repositoryFolder = Files.createTempDirectory("repository-").toFile();
        RepositoryConfig config = RepositoryConfig.create(configFile, this.repositoryFolder);
        this.repository = RepositoryImpl.create(config);
    }

    public void shutdown() throws IOException {
        repository.shutdown();
        FileUtils.deleteDirectory(repositoryFolder);
    }

    @Override
    public Session login(Credentials credentials, String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
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
}
