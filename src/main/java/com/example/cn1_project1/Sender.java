package com.example.cn1_project1;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class Sender extends Application {
    public static int TIMER = 3000;

    public static final double acknowledgeLostRate = 0.04;

    public static final double bitErrorRate = 0.09;

    public static double totalTime;

    public static String t = "";

    /**
     * This method starts the graphical menu and run sender class
     * @param stage
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws IOException {
            FXMLLoader fxmlLoader = new FXMLLoader(Sender.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
            stage.setTitle("Hello!");
            stage.setScene(scene);
            stage.show();
    }

    /**
     * This method sends initial parameters to receiver and send data
     */
    public static void in(){
        long startTime = System.nanoTime();
        int numPackets = 20;
        String flowControl = "GBN";
        int windowSize = 0;
        long timeOut = 3000;
        long sizeSegment = 0;
        int portNumber = 50127;
        numPackets = HelloController.numberOfPackets;
        try {
            /**
             * set data from menu
             */
            flowControl = HelloController.type;
            windowSize = HelloController.windowSize;
            timeOut = HelloController.timeout;
            sizeSegment = 2;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurred while setting information");
        }
        System.out.println("Flow Control techniques : " + flowControl + " Number of Seq bits: " +  " Window Size " + windowSize
                + " Timeout: " + timeOut + " Segment Size: " + sizeSegment);
        t += "\nFlow Control techniques : " + flowControl + " Number of Seq bits: " +  " Window Size " + windowSize
                + " Timeout: " + timeOut + " Segment Size: " + sizeSegment;
        TIMER = (int) timeOut;
        // Sending Data Function
        try {
            /**
             * Initially the sender sends the synchronization packet containing initial information like
             * packet size, window size, Number of packets to the receiver.
             */
            sendData(portNumber, numPackets, flowControl,  windowSize, timeOut, sizeSegment);
        } catch (Exception e) {

            e.printStackTrace();
        }
        long stopTime = System.nanoTime();
        totalTime = stopTime - startTime;
        totalTime = (double) totalTime / 1000000000;
    }
    private static void sendData(int portNumber, int numPackets, String flowControl, int windowSize,
                                 long timeOut, long sizeSegment) throws IOException, ClassNotFoundException, InterruptedException {

        ArrayList<Segment> sent = new ArrayList<>();

        // Last Packet sent
        int lastSent = 0;

        // Sequence number of the last Acknowledged packet
        int waitingForAck = 0;

        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int N = alphabet.length();

        DatagramSocket Socket = null;
        if (flowControl.equalsIgnoreCase("gbn")) {

            byte[] incomingData = new byte[1024];
            TransmissionInformation transmissionInformation = new TransmissionInformation();
            transmissionInformation.setFlowControl(0);
            transmissionInformation.setPacketCount(numPackets);
            transmissionInformation.setPacketSize(sizeSegment);
            transmissionInformation.setWindowSize(1);

            Socket = new DatagramSocket();

            InetAddress IPAddress = InetAddress.getByName("localhost");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(transmissionInformation);
            byte[] data1 = outputStream.toByteArray();

            DatagramPacket initialPacket = new DatagramPacket(data1, data1.length, IPAddress, portNumber);
            System.out.println("Sending Initial Data : " + "\n");
            t += "\nSending Initial Data : " + "\n";

            Socket.send(initialPacket);

            DatagramPacket initialAck = new DatagramPacket(incomingData, incomingData.length);
            Socket.receive(initialAck);
            byte[] dataImp = initialAck.getData();
            ByteArrayInputStream inReturn = new ByteArrayInputStream(dataImp);
            ObjectInputStream isReturn = new ObjectInputStream(inReturn);
            TransmissionInformation transmissionInformation2 = (TransmissionInformation) isReturn.readObject();
            if (transmissionInformation2.getFlowControl() == 100) {
                while (true) {
                    while (lastSent - waitingForAck < windowSize && lastSent < numPackets) {
                        if (lastSent == 0 && waitingForAck == 0) {
                            System.out.println("Timer Started for Packet:  " + 0);
                            t += "\nTimer Started for Packet:  " + 0;
                        }
                        Random r = new Random();
                        char ch = alphabet.charAt(r.nextInt(N));
                        int hashCode = ("" + ch).hashCode();
                        Segment segment = new Segment();
                        segment.setData(ch);
                        segment.setNumber(lastSent);
                        segment.setCheckSum(hashCode);
                        if (lastSent == numPackets - 1) {
                            segment.setLast(true);
                        }

                        if (Math.random() <= bitErrorRate) {

                            segment.setData(alphabet.charAt(r.nextInt(N)));

                        }
                        outputStream = new ByteArrayOutputStream();
                        os = new ObjectOutputStream(outputStream);
                        os.writeObject(segment);
                        byte[] data = outputStream.toByteArray();

                        DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, portNumber);
                        System.out.println("Sending Packet : " + segment.getNumber() + "\n");
                        t += "\nSending Packet : "+ segment.getNumber() + "\n";
                        sent.add(segment);
                        Socket.send(sendPacket);
                        lastSent++;
                        Thread.sleep(2500);

                    }
                    DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                    try {
                        Socket.setSoTimeout(TIMER);
                        Socket.receive(incomingPacket);
                        byte[] data = incomingPacket.getData();
                        ByteArrayInputStream in = new ByteArrayInputStream(data);
                        ObjectInputStream is = new ObjectInputStream(in);
                        Acknowledge acknowledge = (Acknowledge) is.readObject();
                        /**
                         * When the sender receives the ACK packets send by the receiver the window is
                         * moved to accommodate the next set of packets.
                         */
                        if (Math.random() > acknowledgeLostRate) {
                            System.out.println("Received ACK for :" + (acknowledge.getNumber() - 1) + "\n");
                            t += "\n" + "Received ACK for :" + (acknowledge.getNumber() - 1) + "\n";
                            waitingForAck = Math.max(waitingForAck, acknowledge.getNumber());
                            if (!(waitingForAck == numPackets)) {
                                System.out.println("Timer Started for Packet:  " + acknowledge.getNumber() );
                                t += "\n" + "Timer Started for Packet:  " + acknowledge.getNumber() ;
                            }
                        } else {
                            System.out.println(" Acknowledgment Lost for :" + (acknowledge.getNumber() - 1) + "\n");
                            t += "\n" +"Acknowledgment Lost for :" + (acknowledge.getNumber() - 1) + "\n";
                        }

                        if (acknowledge.getNumber() == numPackets) {
                            break;
                        }
                    } catch (SocketTimeoutException e) {
                        /**
                         * since the packet is lost in transition, the sender prints the timeout message in
                         * the command line. After that the sender will resend all the remaining packets in window
                         * after the lost Packet .
                         */

                        System.out.println("Timeout Occurred for Packet " + waitingForAck );
                        t += "\n"+"Timeout Occurred for Packet " + waitingForAck ;

                        for (int i = waitingForAck; i < lastSent; i++) {

                            Segment segment = sent.get(i);
                            char ch = segment.getData();
                            int hashCode = ("" + ch).hashCode();
                            segment.setCheckSum(hashCode);

                            if (Math.random() <= bitErrorRate) {
                                Random r = new Random();
                                segment.setData(alphabet.charAt(r.nextInt(N)));
                            }
                            outputStream = new ByteArrayOutputStream();
                            os = new ObjectOutputStream(outputStream);
                            os.writeObject(segment);
                            byte[] data = outputStream.toByteArray();

                            DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, portNumber);
                            System.out.println("Re Sending Packet :" + segment.getNumber() + "\n");
                            t += "\n" + "Re Sending Packet :" + segment.getNumber() + "\n";
                            Socket.send(sendPacket);
                            Thread.sleep(3000);
                        }
                    }
                }
            }
        }
        else if (flowControl.equalsIgnoreCase("sr")) {
            HashSet<Integer> irregular = new HashSet<>();
            byte[] incomingData = new byte[1024];
            TransmissionInformation transmissionInformation = new TransmissionInformation();
            transmissionInformation.setFlowControl(1);
            transmissionInformation.setPacketCount(numPackets);
            transmissionInformation.setPacketSize(sizeSegment);
            transmissionInformation.setWindowSize(windowSize);
            Socket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(transmissionInformation);
            byte[] data1 = outputStream.toByteArray();
            DatagramPacket initialPacket = new DatagramPacket(data1, data1.length, IPAddress, portNumber);
            System.out.println(" \nSending Initial Data : " + "\n");
            t += "\n" + " \nSending Initial Data : " + "\n\n";
            Socket.send(initialPacket);
            DatagramPacket initialAck = new DatagramPacket(incomingData, incomingData.length);
            Socket.receive(initialAck);
            byte[] dataImp = initialAck.getData();
            ByteArrayInputStream inReturn = new ByteArrayInputStream(dataImp);
            ObjectInputStream isReturn = new ObjectInputStream(inReturn);
            TransmissionInformation transmissionInformation2 = (TransmissionInformation) isReturn.readObject();
            if (transmissionInformation2.getFlowControl() == 100) {
                while (true) {
                    while (lastSent - waitingForAck < windowSize && lastSent < numPackets) {
						/*if (lastSent == 0 && waitingForAck == 0) {
							System.out.println("!!!!! Timer Started for Packet: " + 0);
						}*/
                        if(lastSent-waitingForAck==0)
                        {
                            System.out.println("Timer Started for Packet: " + lastSent);
                            t += "\n"+"Timer Started for Packet: " + lastSent;
                        }
                        else
                        {
                            System.out.println("Timer Already Running");
                            t += "\n"+"Timer Already Running";
                        }
                        Random r = new Random();
                        char ch = alphabet.charAt(r.nextInt(N));
                        int hashCode = ("" + ch).hashCode();
                        Segment segment = new Segment();
                        segment.setData(ch);
                        segment.setNumber(lastSent);
                        segment.setCheckSum(hashCode);
                        if (lastSent == numPackets - 1) {
                            segment.setLast(true);
                        }
                        if (Math.random() <= bitErrorRate) {
                            segment.setData(alphabet.charAt(r.nextInt(N)));
                        }
                        outputStream = new ByteArrayOutputStream();
                        os = new ObjectOutputStream(outputStream);
                        os.writeObject(segment);
                        byte[] data = outputStream.toByteArray();

                        DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, portNumber);
                        System.out.println("Sending Packet : " + segment.getNumber() + "\n");
                        t += "\n"+"Sending Packet : " + segment.getNumber() + "\n";
                        sent.add(segment);
                        Socket.send(sendPacket);
                        lastSent++;
                        Thread.sleep(2500);

                    }
                    DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                    try {
                        Socket.setSoTimeout(TIMER);
                        Socket.receive(incomingPacket);
                        byte[] data = incomingPacket.getData();
                        ByteArrayInputStream in = new ByteArrayInputStream(data);
                        ObjectInputStream is = new ObjectInputStream(in);
                        Acknowledge acknowledge = (Acknowledge) is.readObject();
                        if (Math.random() > acknowledgeLostRate) {
                            System.out.println("Received ACK for :" + (acknowledge.getNumber() - 1));
                            t += "\n"+"Received ACK for :" + (acknowledge.getNumber() - 1);
                            if ((acknowledge.getNumber() - waitingForAck) == 1) {
                                waitingForAck = waitingForAck + 1;
                                if (irregular.size() > 0) {
                                    for (int i = waitingForAck; i <= lastSent; i++) {
                                        if (irregular.contains(i)) {
                                            irregular.remove(i);
                                            waitingForAck++;
                                        } else {
                                            break;
                                        }
                                    }
                                }
                                System.out.println("Timer Started for Packet:  " + waitingForAck + " \n");
                                t += "\n"+"Timer Started for Packet:  " + waitingForAck + " \n";
                            } else {
                                System.out.println("Timer already Running for   " + waitingForAck + " \n");
                                t += "\n"+"Timer already Running for   " + waitingForAck + "\n";
                                irregular.add((acknowledge.getNumber() - 1));
                            }
                            /**
                             *  When the packet is lost in transition, so the packets coming after that
                             * is stored in the application buffer, and the timer in the sender for packet starts, since
                             * the ACK is not received within the time period(3000 ms), timeout occurs, so sender is
                             * sending the packet again. Once the receiver receives the packet it sends the ACK and
                             * packet stored in buffer is delivered to the application.
                             */

                        } else {
                            System.out.println("Acknowledgment Lost for :" + (acknowledge.getNumber() - 1) + " \n");
                            t += "\n"+"Acknowledgment Lost for :" + (acknowledge.getNumber() - 1) + "\n";
                        }

                        if (waitingForAck == numPackets && irregular.size() == 0) {
                            break;
                        }
                    } catch (SocketTimeoutException e) {

                        System.out.println("Timeout Occurred");
                        t += "\n"+"Timeout Occurred";

                        for (int i = waitingForAck; i < lastSent; i++) {

                            Segment segment = sent.get(i);
                            if (!(irregular.contains(segment.getNumber()))) {

                                char ch = segment.getData();
                                int hashCode = ("" + ch).hashCode();
                                segment.setCheckSum(hashCode);

                                if (Math.random() <= bitErrorRate) {
                                    Random r = new Random();
                                    segment.setData(alphabet.charAt(r.nextInt(N)));

                                }
                                outputStream = new ByteArrayOutputStream();
                                os = new ObjectOutputStream(outputStream);
                                os.writeObject(segment);
                                byte[] data = outputStream.toByteArray();

                                DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress,
                                        portNumber);
                                System.out.println("Re Sending Packet :" + segment.getNumber() + "\n");
                                t += "\n"+"Re Sending Packet :" + segment.getNumber() + "\n\n";
                                Socket.send(sendPacket);
                                Thread.sleep(2000);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}