package com.github.rmannibucau.talend.ftp.service;

import java.io.IOException;
import java.io.PrintWriter;

import com.github.rmannibucau.talend.ftp.dataset.FtpDataset;
import com.github.rmannibucau.talend.ftp.datastore.FtpDatastore;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.slf4j.LoggerFactory;
import org.talend.sdk.component.api.service.Service;

import lombok.Data;
import lombok.experimental.Delegate;

@Service
public class FtpService {
    @Service
    private Errors errors;

    public Client createClient(final FtpDataset dataset) {
        final FtpDatastore datastore = dataset.getDatastore();
        final FTPClient client;
        if (!datastore.isSecure()) {
            client = new FTPClient();
        } else {
            final FTPSClient ftps = new FTPSClient(datastore.getProtocol(), datastore.isImplicit());
            switch (datastore.getTrustType()) {
                case ALL:
                    ftps.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
                    break;
                case VALID:
                    ftps.setTrustManager(TrustManagerUtils.getValidateServerCertificateTrustManager());
                    break;
                case NONE:
                    ftps.setTrustManager(null);
                    break;
                // todo: add custom configured trust manager
                default:
                    throw new UnsupportedOperationException("Unsupported trust type: " + datastore.getTrustType());
            }
            client = ftps;
        }
        final FTPClientConfig config = new FTPClientConfig();
        if (dataset.getDateFormat() != null) {
            config.setDefaultDateFormatStr(dataset.getDateFormat());
        }
        if (dataset.getRecentDateFormat() != null) {
            config.setDefaultDateFormatStr(dataset.getRecentDateFormat());
        }

        client.configure(config);
        client.setControlKeepAliveTimeout(dataset.getKeepAliveTimeout());
        client.setControlKeepAliveReplyTimeout(dataset.getKeepAliveReplyTimeout());
        client.setControlEncoding(dataset.getEncoding());
        client.setListHiddenFiles(dataset.isListHiddenFiles());
        // todo: copy stream listener to have size stats?
        if (dataset.isDebug()) {
            client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(new LogWriter(
                    LoggerFactory.getLogger(getClass().getName() + '_' + datastore.getTrustType())
            )), true));
        }
        return new Client(client, dataset, errors);
    }

    @Data
    public static class Client implements AutoCloseable {
        @Delegate
        private final FTPClient ftp;
        private final FtpDataset dataset;
        private final Errors errors;

        public void doConnect() {
            final FtpDatastore datastore = dataset.getDatastore();
            try {
                ftp.connect(datastore.getHost(), datastore.getPort());
                final int replyCode = ftp.getReplyCode();
                if (!FTPReply.isPositiveCompletion(replyCode)) {
                    close();
                    throw new IllegalStateException(errors.invalidReplyCodeDuringConnectPhase(replyCode));
                }
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
            if (datastore.isUseCredentials()) {
                login(datastore);
            }
            try {
                initFileType();
            } catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            initMode();
        }

        @Override
        public void close() {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (final IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        private void initMode() {
            if (dataset.isActive()) {
                ftp.enterLocalActiveMode();
            } else {
                ftp.enterLocalPassiveMode();
            }
        }

        private void initFileType() throws IOException {
            if (dataset.isBinary()) {
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
            } else {
                ftp.setFileType(FTP.ASCII_FILE_TYPE);
            }
        }

        private void login(final FtpDatastore datastore) {
            try {
                if (!ftp.login(datastore.getUsername(), datastore.getPassword())) {
                    ftp.logout();
                    close();
                    throw new IllegalStateException(errors.invalidCredentials());
                }
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}