package eu.fugiczek.wol;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Fugiczek
 */
public class Controller implements Initializable {
    
    private FileChooser fileChooser;
    private MPSender sender;
    
    // new PC tab
    @FXML private TextField tf_mac;
    @FXML private TextField tf_ip;
    @FXML private TextField tf_pc_name;
    @FXML private Label l_log_added;
    @FXML private TableView<PC> tv_pcs;
    @FXML private TableColumn<PC, String> tc_pc_name;
    @FXML private TableColumn<PC, String> tc_mac;
    @FXML private TableColumn<PC, String> tc_ip;
    
    // settings
    @FXML private TextField tf_port;
    
    // main tab
    @FXML private Label l_log;
    
    @FXML private void trySendMP(ActionEvent event) {
        int port;
        if(tf_port.getText() != null) {
            if(!tf_port.getText().isEmpty()) {
                try {
                    port = Integer.parseInt(tf_port.getText());
                } catch(NumberFormatException e) {
                    l_log.setTextFill(Color.RED);
                    l_log.setText("Wrong port number");
                    return;
                }
            } else {
                port = MPSender.DEFAULT_PORT;
            }          
        } else {
            port = MPSender.DEFAULT_PORT;
        }
        
        int success = 0;
        success = tv_pcs.getItems().stream().filter((pc) -> (sender.send(pc.getMac(), 
                pc.getIp().equals("") ? MPSender.DEFAULT_IP : pc.getIp(),
                port))).map((ignor) -> 1).reduce(success, Integer::sum);
        
        l_log.setTextFill(Color.GREEN);
        l_log.setText(success + " success " + (tv_pcs.getItems().size() - success) + " fail");
    }
    
    @FXML private void addNewPC(ActionEvent event) {  
        String mac = tf_mac.getText();
        if(!validateMac(mac)) {
            l_log_added.setTextFill(Color.RED);
            l_log_added.setText("MAC - wrong format");
            return;
        }
        
        String ip = tf_ip.getText();
        if(ip != null) {
            if(!ip.isEmpty()) {
                if(!validateIPv4(ip)) {
                    l_log_added.setTextFill(Color.RED);
                    l_log_added.setText("IP - wrong format");
                    return;
                }
            }       
        } else {
            ip = "";
        }
        
        tv_pcs.getItems().add(new PC(tf_pc_name.getText(), mac, ip));
        l_log_added.setTextFill(Color.GREEN);
        l_log_added.setText("PC added");
    }
    
    public boolean validateMac(String mac) {
        Pattern p = Pattern.compile("^([0-9a-fA-F]{2}[:-]){5}([0-9a-fA-F]{2})$");
        Matcher m = p.matcher(mac);
        return m.find();
    }
    
    public boolean validateIPv4(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            return !ip.endsWith(".");
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    @FXML private void loadPCList(ActionEvent event) {
        Path path = fileChooser.showOpenDialog(null).toPath();
        
        List<String> lines;
        try {
            lines = Files.readAllLines(path, Charset.defaultCharset());
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        StringBuilder source = new StringBuilder();
        lines.forEach((line) -> source.append(line));
        
        JSONObject pc_list = new JSONObject(source.toString());
        JSONArray pcs = pc_list.getJSONArray("pc_list");
        
        List<PC> list = new ArrayList<>();
        for(int i = 0; i < pcs.length(); i++) {
            JSONObject pc = pcs.getJSONObject(i);
            list.add(new PC(pc.getString("pc_name"), pc.getString("mac"), pc.getString("ip")));
        }
        
        tv_pcs.getItems().setAll(list);
    }
    
    @FXML private void savePCList(ActionEvent event) {
        JSONArray pcs = new JSONArray();
        JSONObject pc;
        for(PC p : tv_pcs.getItems()) {
          pc = new JSONObject();
          pc.put("pc_name", p.getPcName());
          pc.put("mac", p.getMac());
          pc.put("ip", p.getIp());
          pcs.put(pc);
        }
        JSONObject pc_list = new JSONObject();
        pc_list.put("pc_list", pcs);

        try {
            Path path = fileChooser.showSaveDialog(null).toPath();
            
            if(path != null) {
                Files.write(path, pc_list.toString().getBytes(Charset.defaultCharset()));
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML private void openTeamViewerHomePage(ActionEvent event) {
        if(Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            
            if(desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(URI.create("http://www.teamviewer.com/"));
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tc_pc_name.setCellValueFactory(new PropertyValueFactory<>("pcName"));
        tc_mac.setCellValueFactory(new PropertyValueFactory<>("mac"));
        tc_ip.setCellValueFactory(new PropertyValueFactory<>("ip"));
        
        fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        
        sender = new MPSender();
    }    
    
}