package com.example.cn1_project1;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * This is controller class for javafx menu
 */
public class HelloController {
    public static String type;
    public static int windowSize;
    public static Long timeout;

    public static int numberOfPackets;


    @FXML
    private TextArea sender;

    @FXML
    private TextArea rec;

    @FXML
    private TextField ws;

    @FXML
    private TextField to;

    @FXML
    private TextField np;
    @FXML
    private TextArea time;
    /*
    onMouseClick event to connect sender & receiver
     */
    @FXML
    protected void onHelloButtonClick() throws InterruptedException {
        windowSize = Integer.parseInt(ws.getText());
        timeout = Long.parseLong(to.getText());
        numberOfPackets = Integer.parseInt(np.getText());

        Thread thread = new Thread("New Thread") {
            public void run(){
                Sender.in();
            }
        };
        thread.start();
    }
    /*
    onMouseEnter event to show sender log
     */
    @FXML
    protected void enter() {
        sender.setText(Sender.t);
    }
    /*
    onMouseEnter event to show receiver log
     */
    @FXML
    protected void en() {
        String content = "";
        rec.setText("");
        try {
            File myObj = new File("src/main/java/com/example/cn1_project1/info.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                content += "\n"+data;
            }
            rec.setText(content);
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    @FXML
    protected void gbn() {
        type = "GBN";
    }
    @FXML
    protected void sr() {
        type = "sr";
    }
    @FXML
    protected void tc() {
        time.setText(Sender.totalTime+"");
    }

    @FXML
    protected void ck() {
//        System.out.println("///////////////////////////////////////////////Rec should be "+MyReceiver.text);
//        rec.setText(Sender.ny);

    }
}