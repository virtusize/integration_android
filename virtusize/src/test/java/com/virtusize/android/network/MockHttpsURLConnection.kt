package com.virtusize.android.network

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.security.cert.Certificate
import javax.net.ssl.HttpsURLConnection

class MockHttpsURLConnection constructor(url: URL, private val mockedResponse: MockedResponse) :
    HttpsURLConnection(url) {
        override fun getInputStream(): InputStream? = mockedResponse.response

        override fun getErrorStream(): InputStream? = mockedResponse.response

        override fun getCipherSuite(): String {
            return ""
        }

        override fun getLocalCertificates(): Array<Certificate> {
            return arrayOf()
        }

        override fun getServerCertificates(): Array<Certificate> {
            return arrayOf()
        }

        override fun getHeaderField(name: String?): String? = name?.let { mockedResponse.headers[it] }

        override fun getResponseMessage(): String? {
            return mockedResponse.message
        }

        override fun getOutputStream(): OutputStream {
            return ByteArrayOutputStream()
        }

        override fun getResponseCode(): Int {
            return mockedResponse.code
        }

        override fun connect() {}

        override fun disconnect() {}

        override fun usingProxy(): Boolean {
            return false
        }
    }

class MockedResponse(
    val code: Int,
    val response: InputStream?,
    val message: String? = null,
    val headers: Map<String, String> = emptyMap(),
)
