package io.github.chronosx88.dhtBootstrap;

import net.tomp2p.connection.ChannelClientConfiguration;
import net.tomp2p.connection.ChannelServerConfiguration;
import net.tomp2p.connection.RSASignatureFactory;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.nat.PeerBuilderNAT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.relay.RelayType;
import net.tomp2p.relay.tcp.TCPRelayServerConfig;
import net.tomp2p.replication.IndirectReplication;
import rice.environment.Environment;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.socket.internet.InternetPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.UUID;

public class Main {
    private static PeerDHT peerDHT;
    private static PastryNode pastryNode;
    private static Number160 peerID;
    private static Properties props;
    private static final String DATA_DIR_PATH = System.getProperty("user.home") + "/.local/share/Influence-Bootstrap/";

    public static void main(String[] args) {
        props = new Properties();
        org.apache.log4j.BasicConfigurator.configure();
        File dataDir = new File(DATA_DIR_PATH);
        File config = new File(DATA_DIR_PATH + "config.properties");
        try {
            if(!dataDir.exists() && !config.exists()) {
                dataDir.mkdirs();
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
                    new PeerBuilderDHT(new PeerBuilder(peerID)
                            .ports(7243)
                            .channelClientConfiguration(createClientChannelConfig())
                            .channelServerConfiguration(createServerChannelConfig())
                            .start())
                    .storage(
                            new StorageMapDB(
                                    peerID,
                                    new File(DATA_DIR_PATH),
                                    new RSASignatureFactory()
                            )
                    ).start();

            new PeerBuilderNAT(peerDHT.peer())
                    .addRelayServerConfiguration(RelayType.OPENTCP, new TCPRelayServerConfig())
                    .start();
            new IndirectReplication(peerDHT).start();
            createNewPastryBootstrapNode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ChannelClientConfiguration createClientChannelConfig() {
        ChannelClientConfiguration channelClientConfiguration = PeerBuilder.createDefaultChannelClientConfiguration();
        channelClientConfiguration.signatureFactory(new RSASignatureFactory());
        return channelClientConfiguration;
    }

    private static ChannelServerConfiguration createServerChannelConfig() {
        ChannelServerConfiguration channelServerConfiguration = PeerBuilder.createDefaultChannelServerConfiguration();
        channelServerConfiguration.signatureFactory(new RSASignatureFactory());
        return channelServerConfiguration;
    }

    private static void createNewPastryBootstrapNode() {
        Environment env = new Environment();
        env.getParameters().setString("probe_for_external_address","true");
        env.getParameters().setString("nat_search_policy","never");
        // Generate the NodeIds Randomly
        NodeIdFactory nidFactory = new RandomNodeIdFactory(env);

        // construct the PastryNodeFactory, this is how we use rice.pastry.socket
        PastryNodeFactory factory = null;
        try {
            factory = new InternetPastryNodeFactory(nidFactory, 7244, env);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // construct a node, but this does not cause it to boot
        PastryNode node = null;
        try {
            node = factory.newNode();
            pastryNode = node;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // in later tutorials, we will register applications before calling boot
        node.boot(new InetSocketAddress(7244));

        // the node may require sending several messages to fully boot into the ring
        synchronized(node) {
            while(!node.isReady() && !node.joinFailed()) {
                // delay so we don't busy-wait
                try {
                    node.wait(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // abort if can't join
                if (node.joinFailed()) {
                    System.out.println("[Pastry] Could not join the FreePastry ring.  Reason:"+node.joinFailedReason());
                }
            }
        }

        System.out.println("[Pastry] Finished creating new bootstrap node "+node);
    }
}
