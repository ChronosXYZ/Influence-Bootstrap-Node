package io.github.chronosx88.dhtBootstrap;

import net.tomp2p.connection.*;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.nat.PeerBuilderNAT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.relay.RelayType;
import net.tomp2p.relay.tcp.TCPRelayServerConfig;
import net.tomp2p.replication.IndirectReplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.UUID;

public class Main {
    private static PeerDHT peerDHT;
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
                    new PeerBuilderDHT(new PeerBuilder(peerID).ports(7243).channelClientConfiguration(createClientChannelConfig()).channelServerConfiguration(createServerChannelConfig()).start())
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ChannelClientConfiguration createClientChannelConfig() {
        ChannelClientConfiguration channelClientConfiguration = new ChannelClientConfiguration();
        channelClientConfiguration.bindings(new Bindings());
        channelClientConfiguration.maxPermitsPermanentTCP(250);
        channelClientConfiguration.maxPermitsTCP(250);
        channelClientConfiguration.maxPermitsUDP(250);
        channelClientConfiguration.pipelineFilter(new PeerBuilder.DefaultPipelineFilter());
        channelClientConfiguration.signatureFactory(new RSASignatureFactory());
        channelClientConfiguration.senderTCP(new InetSocketAddress(0).getAddress());
        channelClientConfiguration.senderUDP(new InetSocketAddress(0).getAddress());
        channelClientConfiguration.byteBufPool(false);
        return channelClientConfiguration;
    }

    private static ChannelServerConfiguration createServerChannelConfig() {
        ChannelServerConfiguration channelServerConfiguration = new ChannelServerConfiguration();
        channelServerConfiguration.bindings(new Bindings());
        //these two values may be overwritten in the peer builder
        channelServerConfiguration.ports(new Ports(Ports.DEFAULT_PORT, Ports.DEFAULT_PORT));
        channelServerConfiguration.portsForwarding(new Ports(Ports.DEFAULT_PORT, Ports.DEFAULT_PORT));
        channelServerConfiguration.behindFirewall(false);
        channelServerConfiguration.pipelineFilter(new PeerBuilder.DefaultPipelineFilter());
        channelServerConfiguration.signatureFactory(new RSASignatureFactory());
        channelServerConfiguration.byteBufPool(false);
        return channelServerConfiguration;
    }
}
