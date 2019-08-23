package com.github.rmannibucau.talend.ftp.source;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;

import com.github.rmannibucau.talend.ftp.service.FtpService;
import org.apache.commons.net.ftp.FTPFile;
import org.talend.sdk.component.api.input.Producer;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Documentation("Implementation of a FTP server actual reader.")
public class FtpInputSource implements Serializable {
    private static final long serialVersionUID = 1L;

    private final FtpInputMapperConfiguration configuration;
    private final FtpService service;
    private final RecordBuilderFactory builderFactory;

    private volatile FtpService.Client client;
    private volatile Iterator<Record> records;

    @Producer
    public Record next() {
        if (records == null) {
            client = service.createClient(configuration.getDataset());
            client.doConnect();
            final String folder = configuration.getDataset().getFolder();
            final String prefix = ofNullable(configuration.getDataset().getFilePrefix()).orElse("");
            try {
                records = Stream.of(client.listFiles(folder, file -> file.getName().startsWith(prefix) && !file.isDirectory()))
                        .sorted(comparing(FTPFile::getName))
                        .map(this::mapToRecord)
                        .iterator();
            } catch (final IOException e) {
                destroy();
                throw new IllegalStateException(e);
            }
        }
        return records.hasNext() ? records.next() : null;
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.close();
        }
    }

    private Record mapToRecord(final FTPFile ftpFile) {
        try {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            client.retrieveFile(
                    String.join(configuration.getDataset().getFileSystemSeparator(), configuration.getDataset().getFolder(), ftpFile.getName()),
                    buffer);
            return builderFactory.newRecordBuilder()
                    .withString("name", ftpFile.getName())
                    .withString("user", ftpFile.getUser())
                    .withString("group", ftpFile.getGroup())
                    .withLong("size", ftpFile.getSize())
                    .withDateTime("timestamp", ZonedDateTime.ofInstant(ftpFile.getTimestamp().toInstant(),
                            ftpFile.getTimestamp().getTimeZone() == null ? ZoneId.of("UTC") : ftpFile.getTimestamp().getTimeZone().toZoneId()))
                    .withBytes("content", buffer.toByteArray())
                    .build();
        } catch (final IOException e) {
            destroy();
            throw new IllegalStateException(e);
        }
    }
}