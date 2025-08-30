package com.smartbus360.app.data.network

import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class TLSSocketFactory : SSLSocketFactory() {
    private val internalSSLSocketFactory: SSLSocketFactory

    init {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, null, null)
        internalSSLSocketFactory = sslContext.socketFactory
    }

    override fun getDefaultCipherSuites(): Array<String> =
        internalSSLSocketFactory.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> =
        internalSSLSocketFactory.supportedCipherSuites

    override fun createSocket(
        s: Socket,
        host: String,
        port: Int,
        autoClose: Boolean
    ): Socket = patchSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose))

    override fun createSocket(
        host: String,
        port: Int
    ): Socket = patchSocket(internalSSLSocketFactory.createSocket(host, port))

    override fun createSocket(
        host: String,
        port: Int,
        localHost: InetAddress?,
        localPort: Int
    ): Socket = patchSocket(
        internalSSLSocketFactory.createSocket(host, port, localHost, localPort)
    )

    override fun createSocket(
        host: InetAddress?,
        port: Int
    ): Socket = patchSocket(internalSSLSocketFactory.createSocket(host, port))

    override fun createSocket(
        address: InetAddress?,
        port: Int,
        localAddress: InetAddress?,
        localPort: Int
    ): Socket = patchSocket(
        internalSSLSocketFactory.createSocket(address, port, localAddress, localPort)
    )

    private fun patchSocket(s: Socket): Socket {
        if (s is SSLSocket) {
            s.enabledProtocols = arrayOf("TLSv1.1", "TLSv1.2")
        }
        return s
    }
}