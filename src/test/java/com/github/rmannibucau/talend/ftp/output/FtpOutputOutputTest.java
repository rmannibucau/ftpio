package com.github.rmannibucau.talend.ftp.output;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.rmannibucau.talend.ftp.dataset.FtpDataset;
import com.github.rmannibucau.talend.ftp.jupiter.FtpFiles;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystemEntry;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.talend.sdk.component.junit.ComponentsHandler;
import org.talend.sdk.component.junit.JoinInputFactory;
import org.talend.sdk.component.junit5.Injected;
import org.talend.sdk.component.junit5.WithComponents;

@FtpFiles
@WithComponents("com.github.rmannibucau.talend.ftp")
class FtpOutputOutputTest {
    @Injected
    private ComponentsHandler handler;

    @Service
    private RecordBuilderFactory records;

    @Test
    void map(final FtpDataset dataset, final FakeFtpServer server) {
        final FtpOutputOutputConfiguration configuration = createBaseConfiguration(dataset);

        final JoinInputFactory inputs = new JoinInputFactory()
                .withInput("__default__", asList(
                        records.newRecordBuilder()
                            .withString("name", "my_content.data")
                            .withString("content", "my content")
                            .build(),
                        records.newRecordBuilder()
                            .withString("name", "my content 2.txt")
                            .withBytes("content", "my content 2".getBytes(StandardCharsets.UTF_8))
                            .build()));

        collect(configuration, inputs);

        final List<FileSystemEntry> files = server.getFileSystem().listFiles("/");
        assertEquals(
            asList("/my_content.data#10#my content", "/my content 2.txt#12#my content 2"),
            files.stream()
                .map(FileEntry.class::cast)
                .map(e -> {
                    try {
                        return e.getPath() + '#' + e.getSize() + '#' +
                                ByteArrayOutputStream.class.cast(e.createOutputStream(true)).toString("UTF-8");
                    } catch (final UnsupportedEncodingException ex) {
                        throw new IllegalStateException(ex);
                    }
                })
                .collect(toList()));
    }

    private List<Record> collect(FtpOutputOutputConfiguration configuration, JoinInputFactory inputs) {
        return handler.collect(handler.createProcessor(FtpOutputOutput.class, configuration), inputs)
                .get(Record.class, "__default__");
    }

    private FtpOutputOutputConfiguration createBaseConfiguration(final FtpDataset dataset) {
        final FtpOutputOutputConfiguration configuration =  new FtpOutputOutputConfiguration();
        configuration.setDataset(dataset);
        return configuration;
    }
}