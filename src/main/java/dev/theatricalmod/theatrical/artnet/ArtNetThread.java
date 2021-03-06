package dev.theatricalmod.theatrical.artnet;

import ch.bildspur.artnet.ArtNetClient;

public class ArtNetThread extends Thread {

    private String ip;
    private ArtNetClient client;
    private boolean running = false;

    public ArtNetThread(String ip, ArtNetClient artNetClient){
        this.ip = ip;
        client = artNetClient;
        setDaemon(true);
    }

    public void stopClient(){
        client.stop();
    }

    @Override
    public void run() {
    client.start(ip);
    }
}
