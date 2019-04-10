package io.github.chronosx88.dhtBootstrap

import com.sleepycat.bind.EntryBinding
import com.sleepycat.je.DatabaseEntry
import java.io.*

class Serializer<T> : EntryBinding<T> {
    fun serialize(obj: T): ByteArray {
        val byteArray = ByteArrayOutputStream()
        try {
            val objectOutputStream = ObjectOutputStream(byteArray)
            objectOutputStream.writeObject(obj)
            objectOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return byteArray.toByteArray()
    }

    fun deserialize(serializedObject: ByteArray?): T? {
        if (serializedObject == null)
            return null
        val inputStream = ByteArrayInputStream(serializedObject)
        var obj: Any? = null
        try {
            val objectInputStream = ObjectInputStream(inputStream)
            obj = objectInputStream.readObject()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return obj as T?
    }

    override fun entryToObject(databaseEntry: DatabaseEntry): T? {
        return deserialize(databaseEntry.data)
    }

    override fun objectToEntry(obj: T, databaseEntry: DatabaseEntry) {
        databaseEntry.data = serialize(obj)
    }
}
