package com.isanechek.imagehandler.utils
//
//import androidx.annotation.RequiresApi
//import com.isanechek.imagehandler.TLS_1_2_ONLY
//import okio.IOException
//import java.net.InetAddress
//import java.net.Socket
//import java.net.UnknownHostException
//import javax.net.ssl.SSLSocket
//import javax.net.ssl.SSLSocketFactory
//
///** A SocketFactory that forces TLS v1.2. */
//@RequiresApi(16)
//class Tls12SocketFactory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {
//
//    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites
//
//    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites
//
//    @Throws(IOException::class)
//    override fun createSocket(socket: Socket, host: String, port: Int, autoClose: Boolean): Socket {
//        return wrap(delegate.createSocket(socket, host, port, autoClose))
//    }
//
//    @Throws(IOException::class, UnknownHostException::class)
//    override fun createSocket(host: String, port: Int): Socket {
//        return wrap(delegate.createSocket(host, port))
//    }
//
//    @Throws(IOException::class, UnknownHostException::class)
//    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
//        return wrap(delegate.createSocket(host, port, localHost, localPort))
//    }
//
//    @Throws(IOException::class)
//    override fun createSocket(host: InetAddress, port: Int): Socket {
//        return wrap(delegate.createSocket(host, port))
//    }
//
//    @Throws(IOException::class)
//    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
//        return wrap(delegate.createSocket(address, port, localAddress, localPort))
//    }
//
//    private fun wrap(socket: Socket): Socket {
//        if (socket is SSLSocket) {
//            socket.enabledProtocols = TLS_1_2_ONLY
//        }
//        return socket
//    }
//}