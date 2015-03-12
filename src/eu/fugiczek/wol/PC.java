package eu.fugiczek.wol;

/**
 * @author Fugiczek
 */
public class PC {
   private final String pc_name;
   private final String mac;
   private final String ip;
   
   public PC(String pc_name, String mac, String ip) {
       this.pc_name = pc_name;
       this.mac = mac;
       this.ip = ip;
   }
   
   public String getPcName() {
       return pc_name;
   }
   
   public String getMac() {
       return mac;
   }
   
   public String getIp() {
       return ip;
   }
}
