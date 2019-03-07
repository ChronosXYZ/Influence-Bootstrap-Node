package io.github.chronosx88.dhtBootstrap;

import net.tomp2p.connection.RSASignatureFactory;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.StorageDisk;

import java.io.*;
import java.util.Properties;
import java.util.UUID;

public class Main {
    private static PeerDHT peerDHT;
    private static Number160 peerID;
    private static Properties props;
    private static final String DATA_DIR_PATH = System.getProperty("user.home") + "/.local/share/Influence-Bootstrap/";

    public static void main(String[] args) {
        props = new Properties();
        File dataDir = new File(DATA_DIR_PATH);
        File config = new File(DATA_DIR_PATH + "config.properties");
        try {
            if(!dataDir.exists() && !config.exists()) {
                dataDir.mkdir();
                config.createNewFile();
                props.setProperty("isFirstRun", "false");
                props.setProperty("peerID", UUID.randomUUID().toString());
                props.store(new FileWriter(config), "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            props.load(new FileInputStream(config));
        } catch (IOException e) {
            e.printStackTrace();
        }

        peerID = Number160.createHash(props.getProperty("peerID"));

        try {
            peerDHT =
                    new PeerBuilderDHT(new PeerBuilder(peerID).ports(7243).start())
                    .storage(
                            new StorageDisk(
                                    peerID,
                                    new File(System.getProperty("user.home") + "/.local/share"),
                                    new RSASignatureFactory()
                            )
                    ).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
