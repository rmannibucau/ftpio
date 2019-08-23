package com.github.rmannibucau.talend.ftp.source;

import java.io.Serializable;

import com.github.rmannibucau.talend.ftp.dataset.FtpDataset;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@GridLayout({
    @GridLayout.Row({ "dataset" })
})
@Documentation("Configuration of the input component.")
public class FtpInputMapperConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    @Option
    @Documentation("Dataset to use to connect to the server and read data.")
    private FtpDataset dataset;
}