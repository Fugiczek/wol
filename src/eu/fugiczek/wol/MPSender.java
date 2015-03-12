package eu.fugiczek.wol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Fugiczek
 */
public class MPSender {
    
    public static final int DEFAULT_PORT = 9;
    public static final String DEFAULT_IP = "255.255.255.255";
    
    public boolean send(String mac) {
        return send(mac, DEFAULT_IP, DEFAULT_PORT);
    }
    
    public boolean send(String mac, String ip) {
        return send(mac, ip, DEFAULT_PORT);
    }
    
    // new String(new char[16]).replace("\0", mac) 
    // po uprave kodu nepouzitelny, ale libi se mi to
    public boolean send(String mac, String ip, int port) {
        try(DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            
            byte[] macBytes = getMacBytes(mac);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff; // synchronizacni stream
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }
            
            socket.send(new DatagramPacket(
                    bytes, 
                    bytes.length, 
                    InetAddress.getByName(ip), 
                    port));
            
        } catch (Exception ex) {
            Logger.getLogger(MPSender.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }
    
    private byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        
        return bytes;
    }
    
}
