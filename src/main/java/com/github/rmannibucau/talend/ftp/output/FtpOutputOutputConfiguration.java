package com.github.rmannibucau.talend.ftp.output;

import java.io.Serializable;

import com.github.rmannibucau.talend.ftp.dataset.FtpDataset;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

@GridLayout({
    @GridLayout.Row("dataset")
})
@Documentation("TODO fill the documentation for this configuration")
public class FtpOutputOutputConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private FtpDataset dataset;

    public FtpDataset getDataset() {
        return dataset;
    }

    public FtpOutputOutputConfiguration setDataset(final FtpDataset dataset) {
        this.dataset = dataset;
        return this;
    }
}