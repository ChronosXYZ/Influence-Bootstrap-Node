package io.github.chronosx88.dhtBootstrap

import net.tomp2p.connection.DSASignatureFactory
import net.tomp2p.dht.PeerBuilderDHT
import net.tomp2p.dht.PeerDHT
import net.tomp2p.nat.PeerBuilderNAT
import net.tomp2p.p2p.PeerBuilder
import net.tomp2p.peers.Number160
import net.tomp2p.relay.RelayType
import net.tomp2p.relay.tcp.TCPRelayServerConfig
import net.tomp2p.replication.AutoReplication
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.util.*


var peerDHT: PeerDHT? = null
var peerID: Number160? = null
var props: Properties? = null
val DATA_DIR_PATH = System.getProperty("user.home") + "/.local/share/Influence-Bootstrap/"

fun main() {
    props = Properties()
    org.apache.log4j.BasicConfigurator.configure()
    val dataDir = File(DATA_DIR_PATH)
    val config = File(DATA_DIR_PATH + "config.properties")
    try {
        if (!dataDir.exists() && !config.exists()) {
            dataDir.mkdir()
            config.createNewFile()
            props!!.setProperty("isFirstRun", "false")
            props!!.setProperty("peerID", UUID.randomUUID().toString())
            props!!.store(FileWriter(config), "")
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    try {
        props!!.load(FileInputStream(config))
    } catch (e: IOException) {
        e.printStackTrace()
    }

    peerID = Number160.createHash(props!!.getProperty("peerID"))

    try {
        peerDHT = PeerBuilderDHT(PeerBuilder(peerID).ports(7243).start())
            .storage(
                StorageBerkeleyDB(
                    peerID!!,
                    File(DATA_DIR_PATH),
                    DSASignatureFactory()
                )
            ).start()
        PeerBuilderNAT(peerDHT!!.peer())
            .addRelayServerConfiguration(RelayType.OPENTCP, TCPRelayServerConfig())
            .start()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    val replication = AutoReplication(peerDHT!!.peer())
    replication.start()
}
