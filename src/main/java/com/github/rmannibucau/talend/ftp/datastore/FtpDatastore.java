package com.github.rmannibucau.talend.ftp.datastore;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout.Row;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@DataStore("FtpConnection")
@GridLayout({
    @Row({ "host", "port", "implicit" }),
    @Row({ "useCredentials", "username", "password" }),
    @Row({ "secure", "trustType", "protocol" })
})
@Documentation("FTP connection.")
public class FtpDatastore implements Serializable {
    private static final long serialVersionUID = 1L;

    @Option
    @Required
    @Documentation("FTP host.")
    private String host;

    @Option
    @Documentation("FTP port.")
    private int port = 21;

    @Option
    @Documentation("Does FTP requires credentials.")
    private boolean useCredentials;

    @Option
    @ActiveIf(target = "useCredentials", value = "true")
    @Documentation("FTP username.")
    private String username;

    @Option
    @ActiveIf(target = "useCredentials", value = "true")
    @Documentation("FTP password.")
    private String password;

    @Option
    @Documentation("Should the connection use FTPS.")
    private boolean secure;

    @Option
    @Documentation("How to trust server certificates.")
    @ActiveIf(target = "secure", value = "true")
    private TrustType trustType = TrustType.VALID;

    @Option
    @Documentation("Is the connection implicit.")
    private boolean implicit;

    @Option
    @Documentation("FTPS protocol.")
    @ActiveIf(target = "secure", value = "true")
    private String protocol = "TLS";

    public enum TrustType {
        ALL, VALID, NONE
    }
}