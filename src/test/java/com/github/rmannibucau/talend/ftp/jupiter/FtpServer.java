/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rmannibucau.talend.ftp.jupiter;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.rmannibucau.talend.ftp.dataset.FtpDataset;
import com.github.rmannibucau.talend.ftp.datastore.FtpDatastore;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.AnnotationUtils;
import org.mockftpserver.core.util.IoUtil;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

public class FtpServer implements BeforeAllCallback, AfterAllCallback, ParameterResolver {
    private Optional<FakeFtpServer> server;

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        server = AnnotationUtils.findAnnotation(context.getRequiredTestClass(), FtpFiles.class)
                .map(files -> {
                    final UnixFakeFileSystem fileSystem = new UnixFakeFileSystem();
                    fileSystem.setCreateParentDirectoriesAutomatically(true);
                    final FtpFile[] ftpFiles = files.files();
                    Stream.of(ftpFiles).forEach(file -> {
                        final String content = file.content();
                        if (content.isEmpty()) {
                            fileSystem.add(new DirectoryEntry('/' + file.name()));
                        } else {
                            fileSystem.add(new FileEntry('/' + file.name(), content(content)));
                        }
                    });
                    if (ftpFiles.length == 0) {
                        fileSystem.add(new DirectoryEntry("/"));
                    }

                    final FakeFtpServer ftp = new FakeFtpServer();
                    ftp.addUserAccount(new UserAccount(files.user(), files.password(), files.root()));
                    ftp.setFileSystem(fileSystem);
                    ftp.setServerControlPort(files.port());
                    ftp.start();
                    return ftp;
                });
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        server.ifPresent(FakeFtpServer::stop);
        server = Optional.empty();
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
        final Class<?> type = parameterContext.getParameter().getType();
        return type == FakeFtpServer.class || type == FtpDataset.class;
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
        return server
                .map(s -> {
                    if (parameterContext.getParameter().getType() == FtpDataset.class) {
                        final FtpDatastore datastore = new FtpDatastore();
                        datastore.setHost("localhost");
                        datastore.setPort(s.getServerControlPort());
                        datastore.setUseCredentials(true);
                        datastore.setUsername("test");
                        datastore.setPassword("testpwd");

                        final FtpDataset dataset = new FtpDataset();
                        dataset.setDatastore(datastore);

                        return dataset;
                    }
                    return s;
                })
                .orElseThrow(() -> new ParameterResolutionException("No server set up in this context"));
    }

    private static String content(final String content) {
        if (content.startsWith("classpath:")) {
            try {
                return new String(IoUtil.readBytes(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(content.substring("classpath:".length()))));
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return content;
    }
}
