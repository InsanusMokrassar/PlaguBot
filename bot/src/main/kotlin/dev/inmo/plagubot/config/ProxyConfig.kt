package dev.inmo.plagubot.config

import io.ktor.client.*
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import kotlin.io.encoding.Base64

@Serializable
data class ProxyConfig(
    val host: String,
    val port: Int,
    val username: String? = null,
    val password: String? = null
) {
    @Transient
    private val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(host, port))
    fun createDefaultClient() = HttpClient(OkHttp) {
        engine {
            config {
                proxy(this@ProxyConfig.proxy)
                if (username != null && password != null) {
                    val passwordAuthentication = PasswordAuthentication(
                        username,
                        password.toCharArray()
                    )
                    Authenticator.setDefault(object : Authenticator() {
                        override fun getPasswordAuthentication(): PasswordAuthentication? {
                            return if (requestingHost.lowercase() == host.lowercase()) {
                                passwordAuthentication
                            } else {
                                null
                            }
                        }
                    })
                }
            }
        }
    }
}
