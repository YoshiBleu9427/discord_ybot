/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.yb.discordybot.gg2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Nicolas
 */
public class LobbyReader {
    
    public static final String LOBBY_UUID = "1ccf16b1-436d-856f-504d-cc1af306aaa7";
    public static final String LOBBY_READ_UUID = "297d0df4-430c-bf61-640a-640897eaef57";
    public static final String LOBBY_SERVER_HOST = "ganggarrison.com";
    public static final int LOBBY_SERVER_PORT = 29944;


    public static byte[] UUIDStringToBytes(String uuid) {
        char currentNibble;
        String posValueString;
        byte[] array = new byte[16];

        uuid = uuid.replaceAll("-", "").toLowerCase();

        posValueString = "0123456789abcdef";
        for(int i=0; i<16; i+=1)
        {
            currentNibble = uuid.charAt(i*2);
            byte numericByte = (byte) ((posValueString.indexOf(currentNibble))*16);

            currentNibble = uuid.charAt(1+i*2);
            numericByte += (byte) (posValueString.indexOf(currentNibble));

            array[i] = numericByte;
        }
        return array;
    }
    
    public static Socket sendLobbyRequest() throws IOException {
        Socket clientSocket = new Socket(LOBBY_SERVER_HOST, LOBBY_SERVER_PORT);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.write(LobbyReader.UUIDStringToBytes(LOBBY_READ_UUID));
        outToServer.write(LobbyReader.UUIDStringToBytes(LOBBY_UUID));
        return clientSocket;
    }
    
    public static LobbyData readResponse(Socket clientSocket) throws IOException {
        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
        LobbyData datagram = new LobbyData();
        
        int nbServers = dis.readInt();
        datagram.setNbServers(nbServers);
        
        for (int i = 0; i < nbServers; i++) {
            LobbyServerData serverDatagram = new LobbyServerData();
            
            int serverInfoLen = dis.readInt();
            
            int protocol = dis.readByte();
            int serverPort = dis.readUnsignedShort();
            
            byte[] serverIPBuf = new byte[4];
            dis.readFully(serverIPBuf);
            String serverIP = String.format("%d.%d.%d.%d",
                    (serverIPBuf[0] & 0xFF),
                    (serverIPBuf[1] & 0xFF),
                    (serverIPBuf[2] & 0xFF),
                    (serverIPBuf[3] & 0xFF)
            );
            
            dis.skipBytes(18);
            
            int slots = dis.readUnsignedShort();
            int players = dis.readUnsignedShort();
            int bots = dis.readUnsignedShort();
            int isPrivateBuf = dis.readUnsignedShort();
            boolean isPrivate = ((isPrivateBuf & 0x1) != 0);
            
            
            serverDatagram.setServerInfoLen(serverInfoLen);
            serverDatagram.setProtocol(protocol);
            serverDatagram.setServerPort(serverPort);
            serverDatagram.setServerIP(serverIP);
            serverDatagram.setSlots(slots);
            serverDatagram.setPlayers(players);
            serverDatagram.setBots(bots);
            serverDatagram.setIsPrivate(isPrivate);
            
            
            int infoLen = dis.readUnsignedShort();
            Map<String, String> infos = new HashMap<>();
            for (int j = 0; j < infoLen; j++) {
                int strLen = dis.readUnsignedByte();
                byte[] keyBuf = new byte[strLen];
                dis.readFully(keyBuf);
                String key = new String(keyBuf, "UTF-8");
                
                strLen = dis.readUnsignedShort();
                byte[] valBuf = new byte[strLen];
                dis.readFully(valBuf);
                String value = new String(valBuf, "UTF-8");
                
                infos.put(key, value);
            }
            
            serverDatagram.setInfos(infos);
            datagram.getServerData().add(serverDatagram);
            
        }
        clientSocket.close();
        return datagram;
    }
}
