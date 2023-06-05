package utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;

public class CustomRMIServerSocketFactory extends RMISocketFactory implements RMIServerSocketFactory {
    private final RMIServerSocketFactory delegate;
    private final int port;

    public CustomRMIServerSocketFactory(RMIServerSocketFactory delegate, int port) {
        this.delegate = delegate;
        this.port = port;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return null;
    }
    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return delegate.createServerSocket(this.port);
    }
}