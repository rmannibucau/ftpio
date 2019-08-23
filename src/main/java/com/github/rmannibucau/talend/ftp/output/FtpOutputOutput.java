package com.github.rmannibucau.talend.ftp.output;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.PreDestroy;

import com.github.rmannibucau.talend.ftp.dataset.FtpDataset;
import com.github.rmannibucau.talend.ftp.service.Errors;
import com.github.rmannibucau.talend.ftp.service.FtpService;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Processor;
import org.talend.sdk.component.api.record.Record;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Version
@Icon(custom = "ftp")
@Processor(name = "FtpOutput")
@Documentation("FTP output which will upload files in the server using the `name` string from the incoming record " +
        "(will be the file name in the dataset folder) and `content` payload (either of type string or bytes).")
public class FtpOutputOutput implements Serializable {
    private static final long serialVersionUID = 1L;

    private final FtpOutputOutputConfiguration configuration;
    private final FtpService service;
    private final Errors errors;

    private volatile FtpService.Client client;
    private Charset charset;

    @ElementListener
    public void onRecord(final Record record) {
        final FtpService.Client currentClient = this.client;
        if (currentClient == null) {
            charset = Charset.forName(configuration.getDataset().getEncoding());
            this.client = service.createClient(configuration.getDataset());
        }

        final String name = record.getString("name");
        final byte[] content = record.getSchema().getEntries().stream()
                .filter(it -> "content".equals(it.getName()))
                .findFirst()
                .map(entry -> {
                    switch (entry.getType()) {
                        case STRING:
                            return record.getString("content").getBytes(charset);
                        case BYTES:
                            return record.getBytes("content");
                        default:
                            throw new IllegalArgumentException("Unsupported content of type: " + entry.getType());
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException(errors.noContentFound()));

        if (currentClient == null) { // after record was loaded to avoid to connect if it fails cause of an invalid record
            client.doConnect();
        }

        try (final InputStream stream = new ByteArrayInputStream(content)) {
            final FtpDataset dataset = configuration.getDataset();
            if (!this.client.storeFile(String.join(dataset.getFileSystemSeparator(), dataset.getFolder(), name), stream)) {
                throw new IllegalStateException("Can't store: " + name);
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Can't store: " + name, e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.close();
        }
    }
}