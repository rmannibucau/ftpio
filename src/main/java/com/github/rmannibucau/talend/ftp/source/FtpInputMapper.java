package com.github.rmannibucau.talend.ftp.source;

import static java.util.Collections.singletonList;

import java.io.Serializable;
import java.util.List;

import com.github.rmannibucau.talend.ftp.service.FtpService;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.input.Assessor;
import org.talend.sdk.component.api.input.Emitter;
import org.talend.sdk.component.api.input.PartitionMapper;
import org.talend.sdk.component.api.input.Split;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Version
@Icon(custom = "ftp")
@PartitionMapper(name = "FtpInput")
@Documentation("FTP(s) file reader.")
public class FtpInputMapper implements Serializable {
    private static final long serialVersionUID = 1L;

    private final FtpInputMapperConfiguration configuration;
    private final FtpService service;
    private final RecordBuilderFactory recordBuilderFactory;

    @Assessor
    public long estimateSize() { // let's not split a FTP for now
        return 1L;
    }

    @Split
    public List<FtpInputMapper> split() {
        return singletonList(this);
    }

    @Emitter
    public FtpInputSource createWorker() {
        return new FtpInputSource(configuration, service, recordBuilderFactory);
    }
}