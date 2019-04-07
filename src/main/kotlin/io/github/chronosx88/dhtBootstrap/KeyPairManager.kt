package io.github.chronosx88.dhtBootstrap

import java.io.IOException
import java.io.FileOutputStream
import java.io.File
import java.security.KeyPair
import java.security.NoSuchAlgorithmException
import java.security.KeyPairGenerator
import java.io.FileInputStream


class KeyPairManager {
    private val keyPairDir: File
    private val serializer: Serializer<KeyPair>

    init {
        this.keyPairDir = File(DATA_DIR_PATH, "keyPairs")
        if (!this.keyPairDir.exists()) {
            this.keyPairDir.mkdir()
        }
        this.serializer = Serializer()
    }

    fun openMainKeyPair(): KeyPair? {
        return getKeyPair("mainKeyPair")
    }

    fun getKeyPair(keyPairName: String): KeyPair? {
        var keyPairName = keyPairName
        keyPairName = "$keyPairName.kp"
        val keyPairFile = File(keyPairDir, keyPairName)
        return if (!keyPairFile.exists()) {
            createKeyPairFile(keyPairFile)
        } else openKeyPairFile(keyPairFile)
    }

    @Synchronized
    private fun openKeyPairFile(keyPairFile: File): KeyPair? {
        var keyPair: KeyPair? = null
        try {
            val inputStream = FileInputStream(keyPairFile)
            val serializedKeyPair = ByteArray(keyPairFile.length().toInt())
            inputStream.read(serializedKeyPair)
            inputStream.close()
            keyPair = serializer.deserialize(serializedKeyPair)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return keyPair
    }

    @Synchronized
    private fun createKeyPairFile(keyPairFile: File): KeyPair? {
        var keyPair: KeyPair? = null
        try {
            keyPairFile.createNewFile()
            keyPair = KeyPairGenerator.getInstance("DSA").generateKeyPair()
            val outputStream = FileOutputStream(keyPairFile)
            outputStream.write(serializer.serialize(keyPair))
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return keyPair
    }

    @Synchronized
    fun saveKeyPair(keyPairID: String, keyPair: KeyPair) {
        val keyPairFile = File(keyPairDir, "$keyPairID.kp")
        if (!keyPairFile.exists()) {
            try {
                val outputStream = FileOutputStream(keyPairFile)
                outputStream.write(serializer.serialize(keyPair))
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}
