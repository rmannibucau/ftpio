package com.github.rmannibucau.talend.ftp.source;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.List;

import com.github.rmannibucau.talend.ftp.dataset.FtpDataset;
import com.github.rmannibucau.talend.ftp.jupiter.FtpFile;
import com.github.rmannibucau.talend.ftp.jupiter.FtpFiles;
import org.junit.jupiter.api.Test;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.junit.ComponentsHandler;
import org.talend.sdk.component.junit5.Injected;
import org.talend.sdk.component.junit5.WithComponents;

@FtpFiles(files = {
    @FtpFile(name = "foo-1.txt", content = "first file\nin the server"),
    @FtpFile(name = "foo-2.txt", content = "second file\nin the server"),
    @FtpFile(name = "bar.txt", content = "last bar"),
    @FtpFile(name = "nested/1.foo", content = "foo1"),
    @FtpFile(name = "nested/2.foo", content = "foo2")
})
@WithComponents("com.github.rmannibucau.talend.ftp")
class FtpInputMapperTest {
    @Injected
    private ComponentsHandler handler;

    @Test
    void simple(final FtpDataset dataset) {
        assertEquals(
            asList(
                "bar.txt/none/none/8/true/last bar",
                "foo-1.txt/none/none/24/true/first file\nin the server",
                "foo-2.txt/none/none/25/true/second file\nin the server"),
            format(collect(createBaseConfiguration(dataset))));
    }

    @Test
    void filtered(final FtpDataset dataset) {
        final FtpInputMapperConfiguration configuration = createBaseConfiguration(dataset);
        configuration.getDataset().setFilePrefix("foo-");
        assertEquals(
            asList(
                "foo-1.txt/none/none/24/true/first file\nin the server",
                "foo-2.txt/none/none/25/true/second file\nin the server"),
            format(collect(configuration)));
    }

    @Test
    void subfolder(final FtpDataset dataset) {
        final FtpInputMapperConfiguration configuration = createBaseConfiguration(dataset);
        configuration.getDataset().setFolder("nested");
        assertEquals(
            asList("1.foo/none/none/4/true/foo1", "2.foo/none/none/4/true/foo2"),
            format(collect(configuration)));
    }

    private Collection<String> format(final List<Record> records) {
        return records.stream()
                .map(record -> record.getString("name") + '/' +
                        record.getString("user") + '/' +
                        record.getString("group") + '/' +
                        record.getLong("size") + '/' +
                        (record.getDateTime("timestamp") != null) + '/' +
                        new String(record.getBytes("content")))
                .collect(toList());
    }

    private List<Record> collect(final FtpInputMapperConfiguration configuration) {
        return handler.collectAsList(Record.class, handler.createMapper(FtpInputMapper.class, configuration));
    }

    private FtpInputMapperConfiguration createBaseConfiguration(final FtpDataset dataset) {
        final FtpInputMapperConfiguration configuration =  new FtpInputMapperConfiguration();
        configuration.setDataset(dataset);
        return configuration;
    }
}