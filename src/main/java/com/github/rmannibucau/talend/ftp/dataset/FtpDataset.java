package com.github.rmannibucau.talend.ftp.dataset;

import java.io.Serializable;

import com.github.rmannibucau.talend.ftp.datastore.FtpDatastore;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout.Row;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@DataSet("FtpDataset")
@GridLayout({
    @Row("datastore"),
    @Row({ "keepAliveTimeout", "keepAliveReplyTimeout" }),
    @Row("encoding"),
    @Row("listHiddenFiles"),
    @Row({ "dateFormat", "recentDateFormat" }),
    @Row({ "binary", "active" }),
    @Row({ "folder", "filePrefix" })
})
@GridLayout(names = GridLayout.FormType.ADVANCED, value = {
    @Row("debug"),
    @Row("fileSystemSeparator")
})
@Documentation("Dataset representing a list of file in a FTP server.")
public class FtpDataset implements Serializable {
    private static final long serialVersionUID = 1L;

    @Option
    @Documentation("FTP datastore.")
    private FtpDatastore datastore;

    @Option
    @Documentation("How long to wait before sending another control keep-alive message.")
    private int keepAliveTimeout;

    @Option
    @Documentation("How long to wait (ms) for keepalive message replies before continuing.")
    private int keepAliveReplyTimeout = 1000;

    @Option
    @Documentation("Control encoding.")
    private String encoding = "ISO-8859-1";

    @Option
    @Documentation("Should hidden files be listed.")
    private boolean listHiddenFiles = false;

    @Option
    @Documentation("Date format.")
    private String dateFormat;

    @Option
    @Documentation("Recent date format.")
    private String recentDateFormat;

    @Option
    @Documentation("Activate binary mode, if false ascii mode is used.")
    private boolean binary;

    @Option
    @Documentation("Activate active mode, if false passive mode is used.")
    private boolean active;

    @Option
    @Documentation("Folder to work in.")
    private String folder = "";

    @Option
    @Documentation("File name prefix.")
    private String filePrefix;

    @Option
    @Documentation("Activate debug mode (for testing or investigation purposes only).")
    private boolean debug;

    @Option
    @Documentation("The file system separator.")
    private String fileSystemSeparator = "/";
}
