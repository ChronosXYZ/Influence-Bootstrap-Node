package io.github.chronosx88.dhtBootstrap;

import net.tomp2p.connection.RSASignatureFactory;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.StorageDisk;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Main {
    private static PeerDHT peerDHT;
    private static Number160 peerID;

    public static void main(String[] args) {
        //TODO: Save peerID and route table to config
        peerID = Number160.createHash(UUID.randomUUID().toString());
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
