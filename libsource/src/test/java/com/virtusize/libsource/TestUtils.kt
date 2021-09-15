package com.virtusize.libsource

import com.virtusize.libsource.fixtures.TestFixtures
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream

internal object TestUtils {
    fun readFileFromAssets(fileName: String): JSONObject {
        return try {
            val file = File(javaClass.getResource(fileName).path)
            val `is`: InputStream = file.inputStream()
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            JSONObject(String(buffer))
        } catch (e: IOException) {
            TestFixtures.EMPTY_JSON_DATA
            throw RuntimeException(e)
        }
    }
}
