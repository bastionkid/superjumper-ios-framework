package com.badlogicgames.superjumper.framework

import com.badlogic.gdx.Net
import com.badlogic.gdx.Net.HttpResponseListener
import com.badlogic.gdx.net.*
import org.robovm.apple.foundation.NSURL
import org.robovm.apple.uikit.UIApplication

class CustomIOSNet(app: CustomIOSApplication, configuration: CustomIOSApplicationConfiguration) :
    Net {
    var netJavaImpl: NetJavaImpl
    val uiApp: UIApplication?
    override fun sendHttpRequest(
        httpRequest: Net.HttpRequest,
        httpResponseListener: HttpResponseListener
    ) {
        netJavaImpl.sendHttpRequest(httpRequest, httpResponseListener)
    }

    override fun cancelHttpRequest(httpRequest: Net.HttpRequest) {
        netJavaImpl.cancelHttpRequest(httpRequest)
    }

    override fun newServerSocket(
        protocol: Net.Protocol,
        hostname: String,
        port: Int,
        hints: ServerSocketHints
    ): ServerSocket {
        return NetJavaServerSocketImpl(protocol, hostname, port, hints)
    }

    override fun newServerSocket(
        protocol: Net.Protocol,
        port: Int,
        hints: ServerSocketHints
    ): ServerSocket {
        return NetJavaServerSocketImpl(protocol, port, hints)
    }

    override fun newClientSocket(
        protocol: Net.Protocol,
        host: String,
        port: Int,
        hints: SocketHints
    ): Socket {
        return NetJavaSocketImpl(protocol, host, port, hints)
    }

    override fun openURI(URI: String): Boolean {
        val url = NSURL(URI)
        if (uiApp!!.canOpenURL(url)) {
            uiApp.openURL(url)
            return true
        }
        return false
    }

    init {
        uiApp = app.uiApp
        netJavaImpl = NetJavaImpl(configuration.maxNetThreads)
    }
}