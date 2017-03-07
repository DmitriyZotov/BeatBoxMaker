package bbclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class logicB {
    private ArrayList<CheckBox> CheckBoxList;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String userName;
    private HashMap<String, boolean[]> otherSeqsMap = new HashMap<String, boolean[]>();
    private ListView incomList;
    private Sequencer sequencer;        
    private Sequence sequence; 
    private Track track;
    private final int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
    private boolean conectStatus = false;//сделать геттер
    
    public boolean getConStatus(){
        return conectStatus;
    }
    public void setConStatus(boolean conStatus){
        conectStatus = conStatus;
    }
    public logicB(ArrayList<CheckBox> cbList, ListView incList){
        this.CheckBoxList = cbList;
        this.incomList = incList;
        setUpMidi();
    }
    public void setUpMidi(){
        try{
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(sequence.PPQ,4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public String getTemp(){
        return Float.toString(sequencer.getTempoFactor());
    }
    public void startBeat(){
        setUpMidi();
        buildTrackAndStart(); 
    }
    public void stopBeat(){
        sequencer.stop();
    }
    public void upTempo(){
        float tempoFactor = sequencer.getTempoFactor();
        sequencer.setTempoFactor((float)(tempoFactor*1.03));
    }
    public void downTempo(){
        float tempoFactor = sequencer.getTempoFactor();
        sequencer.setTempoFactor((float)(tempoFactor*0.97));
    }
    public void openBeat(){
        FileChooser fileCho = new FileChooser();
        fileCho.setTitle("Выберите файл");
        File file = fileCho.showOpenDialog(null);
        boolean [] checkBoxState = null;
        try{
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream is = new ObjectInputStream(fileIn);
            checkBoxState = (boolean[]) is.readObject();
        }
        catch(Exception ex){
            System.out.println(ex);
        }
        for (int i = 0; i < 256; i++) {
            CheckBox chB = (CheckBox) CheckBoxList.get(i);
            if(checkBoxState[i]){
                chB.setSelected(true);
            }
            else{
                chB.setSelected(false);
            }
        }
    }
    public void saveBeat(){
        FileChooser fileCho = new FileChooser();
        fileCho.setTitle("Сохранить файл");
        File file = fileCho.showSaveDialog(null);
        boolean [] checkBoxState = new boolean[256];
        for (int i = 0; i < 256; i++) {
            CheckBox checkB = (CheckBox) CheckBoxList.get(i);
            if(checkB.isSelected()){
                checkBoxState[i] = true;
            }
        }
        try {
            FileOutputStream fileStream = new FileOutputStream(file);
            ObjectOutputStream os = new ObjectOutputStream(fileStream);
            os.writeObject(checkBoxState);
        } 
        catch (IOException ex) {
            Logger.getLogger(logicB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void buildTrackAndStart(){
        ArrayList<Integer> trackList = null;
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        for (int i = 0; i < 10; i++) {
            trackList = new ArrayList<Integer>();
            for (int j = 0; j < 16; j++) {
                CheckBox jc = (CheckBox) CheckBoxList.get(j+(16*i));
                if(jc.isSelected()){
                    int key = instruments[i];
                    trackList.add(new Integer(key));
                }
                else{
                    trackList.add(null);
                }
            }
            makeTracks(trackList);
        }
        track.add(makeEvent(192,9,1,0,15));
        try{
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
        }
        catch(Exception ex){
            System.out.println("Ошибка воспроизведения " + ex);
        }
    }
    public void makeTracks(ArrayList list){
        Iterator it = list.iterator();
        for (int i = 0; i < 16; i++) {
            Integer num = (Integer) it.next();
            if(num != null){
                int numKey = num.intValue();
                track.add(makeEvent(144,9,numKey,100,i));
                track.add(makeEvent(128,9,numKey,100,i+1));
            }
        }
    }
    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick){
        MidiEvent event = null;
        try{
            ShortMessage a = new ShortMessage();
            a.setMessage(comd,chan,one,two);
            event = new MidiEvent(a, tick);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return event;
    }
    public void connectUp(String name){
        userName = name;
        try{
            Socket sock = new Socket("127.0.0.1", 4242);
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            Thread remote = new Thread(new RemoteReader()); //тут вызывается РемотеРиадер
            remote.start();
            setConStatus(true);
        }
        catch(Exception ex){
            System.out.println("Нет подключения " + ex);
        }
        setUpMidi();
    }
    public class RemoteReader implements Runnable {
        boolean[] checkboxState = null;
        String nameToShow = userName;
        Object obj = null;
        @Override
        public void run() {
            try{
                while((obj=in.readObject()) != null){
                    System.out.println("got an obj from server");
                    System.out.println(obj.getClass());
                    String nameToShow = (String) obj;
                    checkboxState = (boolean[]) in.readObject();
                    otherSeqsMap.put(nameToShow, checkboxState);
                    incomList.getItems().add(nameToShow);  
                }
            }
            catch(Exception ex){
                System.out.println("read error " +ex);
            }
        }
    }
    public void sendMessaage(TextField textFieldChat){
        boolean[] checkBoxState = new boolean[256];
        if(getConStatus()==false){
            System.out.println("нет подключения");
        }
        else{
            for (int i = 0; i < 256; i++) {
                CheckBox check = (CheckBox) CheckBoxList.get(i);
                if(check.isSelected()){
                    checkBoxState[i] = true;
                }
            }
            String messageToSend = null;
            try{
                out.writeObject(userName + ": " + textFieldChat.getText());
                out.writeObject(checkBoxState);
            }
            catch(Exception ex){
                System.out.println("Ошибка отправки сообщения на сервер");
            }
            textFieldChat.setText("");
        }
    }
    public void listClicked(){
        incomList.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                String selected = (String) incomList.getSelectionModel().getSelectedItem();
                if(selected != null){
                    boolean[] selectedState = (boolean[]) otherSeqsMap.get(selected);
                    changeSequence(selectedState);
                    sequencer.stop();
                    buildTrackAndStart();
                }
            } 
        });
    }
    public void changeSequence(boolean[] checkBoxState){
        for (int i = 0; i < 256; i++) {
            CheckBox check = (CheckBox) CheckBoxList.get(i);
            if(checkBoxState[i]){
                check.setSelected(true);
            }else{
                check.setSelected(false);
            }
        }
    }
}