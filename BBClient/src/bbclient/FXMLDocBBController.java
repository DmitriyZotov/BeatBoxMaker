package bbclient;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;


public class FXMLDocBBController implements Initializable {
    private ArrayList<CheckBox> checkBoxList = new ArrayList<CheckBox>();
    private logicB b;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String userName;
    @FXML
    private Button startBtn, stopBtn,clearBtn,tempUpBtn,tempDownBtn,
            saveBtn, openBtn, sendBtn;
    @FXML
    private ListView listView;
    @FXML
    private TextField textFieldChat, userNameTextField, tempTextField;
    @FXML
    private Pane cbPane;
    @FXML
    private Label errorLbl;
    @FXML
    private void startClicked(ActionEvent event) {
        tempTextField.setText(b.getTemp());
        b.startBeat();
    }
    @FXML
    private void stopClicked(ActionEvent event) {
        b.stopBeat();
    }
    @FXML
    private void clearClicked(ActionEvent event) {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                CheckBox jc = (CheckBox) checkBoxList.get(j+(16*i));
                if(jc.isSelected()){
                    jc.setSelected(false);
                }
            }
        }
    }
    @FXML
    private void tempUpClicked(ActionEvent event) {
        b.upTempo();
        tempTextField.setText(b.getTemp());
    }
    @FXML
    private void tempDownClicked(ActionEvent event) {
        b.downTempo();
        tempTextField.setText(b.getTemp());
    }
    @FXML
    private void saveClicked(ActionEvent event) {
        b.saveBeat();
    }
    @FXML
    private void openClicked(ActionEvent event) {
        b.openBeat();
    }
    @FXML
    private void sendClicked(ActionEvent event) {
        userName = (String) userNameTextField.getText();
        if(b.getConStatus()==false){
            errorLbl.setText(" Press connect button");
        }
        else if(userName.equals("")){
            errorLbl.setText(" Enter username");
        }
        else{
            b.sendMessaage(textFieldChat);
        }
    }
    @FXML
    private void connectClicked(ActionEvent event) {
        userName = (String) userNameTextField.getText();
        if(userName.equals("")){
            errorLbl.setText(" Enter username");
        }
        else{
            b.connectUp(userName);
            if(b.getConStatus()==false){
                errorLbl.setText(" Connection error");
            }
            errorLbl.setText("Connect is ok");
        }
    }
    @FXML
    private void listClicked(MouseEvent arg0){
        b.listClicked();
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        GridPane gridP = new GridPane();
        gridP.setHgap(4);
        gridP.setVgap(21);
        int i,j;
        for (i = 0, j = 0; i < 16 && j < 16; i++) {
            CheckBox c = new CheckBox();
            c.setSelected(false);
            checkBoxList.add(c);
            gridP.add(c, i, j, 1, 1);
            if(i == 15){
                i = -1;
                j++;
            }
        }
        cbPane.getChildren().add(gridP);
        b = new logicB(checkBoxList, listView); 
    }    
}
