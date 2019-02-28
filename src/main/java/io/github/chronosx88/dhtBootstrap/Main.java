package io.github.chronosx88.dhtBootstrap;

import io.github.chronosx88.dhtBootstrap.kademlia.JKademliaNode;
import io.github.chronosx88.dhtBootstrap.kademlia.node.KademliaId;

import java.io.IOException;

public class Main {
    private static JKademliaNode node;
    public static void main(String[] args) {
        try {
            node = new JKademliaNode("Main Bootstrap Node", new KademliaId("D65D56E189E513A6AB8E38370E6B33386EB639D6"), 7243);
            System.out.println(node.getNode().getNodeId().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
