package com.example.cn1_project1;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;

public class MyReceiver {
	public static String text = "";
	public  String temp = text;
	public static final double packetLostRate = 0.1;


	public static void main(String[] args) {
		DatagramSocket datagramSocket = null;
		int portNumber = 50127;
		byte[] incomingData = new byte[1024];
		try {
			clearTheFile();
			datagramSocket = new DatagramSocket(portNumber);
			System.out.println("Receiver Side is Ready to Accept Packets at PortNumber: " + portNumber + "\n");
			text += "Receiver Side is Ready to Accept Packets at PortNumber: " + portNumber + "\n\n";
			savetoFile(text);
			DatagramPacket initialPacket = new DatagramPacket(incomingData, incomingData.length);
			datagramSocket.receive(initialPacket);
			byte[] data1 = initialPacket.getData();
			ByteArrayInputStream inInitial = new ByteArrayInputStream(data1);
			ObjectInputStream isInitial = new ObjectInputStream(inInitial);
			TransmissionInformation transmissionInformation = (TransmissionInformation) isInitial.readObject();
			System.out.println(" \n Initial Data Received = " + transmissionInformation.toString() + "\n");
			text += " \nInitial Data Received = " + transmissionInformation.toString() + "";
			savetoFile(text);
			int flowControl = transmissionInformation.getFlowControl();
			InetAddress IPAddress = initialPacket.getAddress();
			int port = initialPacket.getPort();
			transmissionInformation.setFlowControl(100);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(transmissionInformation);
			byte[] replyByte = outputStream.toByteArray();
			DatagramPacket replyPacket = new DatagramPacket(replyByte, replyByte.length, IPAddress, port);
			datagramSocket.send(replyPacket);
			if (flowControl == 0) {
				transmissionInformation.setFlowControl(0);
				transferGBN(datagramSocket, transmissionInformation);
			} else {
				transmissionInformation.setFlowControl(1);
				transferSR(datagramSocket, transmissionInformation);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void transferSR(DatagramSocket socket, TransmissionInformation transmissionInformation) throws IOException, ClassNotFoundException {
		ArrayList<Segment> receivedSegments = new ArrayList<>();
		boolean isLast = false;
		int lookForward = 0;
		byte[] entrySegments = new byte[1024];
		ArrayList<Segment> segments = new ArrayList<>();
		while (!isLast) {
			DatagramPacket incomingPacket = new DatagramPacket(entrySegments, entrySegments.length);
			socket.receive(incomingPacket);
			InetAddress IPAddress = incomingPacket.getAddress();
			int port = incomingPacket.getPort();
			byte[] data = incomingPacket.getData();
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream is = new ObjectInputStream(in);
			Segment segment = (Segment) is.readObject();
			char ch = segment.getData();
			int hashCode = ("" + ch).hashCode();
			boolean checkSum = (hashCode == segment.getCheckSum());
			if (segment.getNumber() == lookForward && segment.isLast() && checkSum) {
				lookForward++;
				receivedSegments.add(segment);
				int value = sendData(segment, lookForward, socket, IPAddress, port, false);
				if (value < lookForward) {
					lookForward = value;
					int length = receivedSegments.size();
					System.out.println("\nPacket " + (lookForward) + " lost in the Transmission");
					text += "\nPacket " + (lookForward) + " lost in the Transmission\n";
					System.out.println("Packet Lost\n");
					text += "Packet Lost\n\n";
					savetoFile(text);
					receivedSegments.remove(length - 1);
					isLast = false;
				} else {
					System.out.println("Last packet received\n");
					text += "Last packet received\n\n";
					savetoFile(text);
					isLast = true;
				}
			}
			else if (segment.getNumber() == lookForward && checkSum && segments.size() > 0) {
				receivedSegments.add(segment);
				lookForward++;
				int value = sendData(segment, lookForward, socket, IPAddress, port, false);
				if (value < lookForward) {
					lookForward = value;
					int length = receivedSegments.size();
					System.out.println("\nPacket " + (lookForward) + " lost in the Transmission");
					text += "\nPacket " + (lookForward) + " lost in the Transmission\n";
					System.out.println("Packet Lost\n");
					text += "Packet Lost\n\n";
					savetoFile(text);
					receivedSegments.remove(length - 1);
				} else {
					ArrayList<Segment> temp = new ArrayList<>();
					temp.addAll(segments);
					int count = 0;
					for (int i = 0; i < temp.size(); i++) {
						if (!(lookForward == temp.get(i).getNumber())) {
							break;
						} else {
							lookForward++;
							count++;
							System.out.println("Packet " + segments.get(i).getNumber() + " delivered to Application From Buffer");
							text += "Packet " + segments.get(i).getNumber() + " delivered to Application From Buffer\n";
							savetoFile(text);
						}
					}
					segments = new ArrayList<>();
					for (int j = 0; j < temp.size(); j++) {
						if (j < count) {
							continue;
						}
						segments.add(temp.get(j));
					}
					if (lookForward == transmissionInformation.getPacketCount()) {
						isLast = true;
					}
				}
			}

			else if (segment.getNumber() == lookForward && checkSum && segments.size() == 0) {
				receivedSegments.add(segment);
				lookForward++;
				int value = sendData(segment, lookForward, socket, IPAddress, port, false);
				if (value < lookForward) {
					lookForward = value;
					int length = receivedSegments.size();
					System.out.println("\nPacket " + (lookForward) + " lost in the Transmission");
					text += "\nPacket " + (lookForward) + " lost in the Transmission\n";
					System.out.println("Packet Lost\n");
					text += "Packet Lost\n\n";
					savetoFile(text);
					receivedSegments.remove(length - 1);
				}
				else {
				}
			}
			else if (segment.getNumber() > lookForward && checkSum) {
				sendData(segment, lookForward, socket, IPAddress, port, true);
				System.out.println("Packet " + segment.getNumber() + " Stored in Buffer \n");
				text += "Packet " + segment.getNumber() + " Stored in Buffer \n\n";
				segments.add(segment);
				savetoFile(text);
				Collections.sort(segments);
			}
			else if (segment.getNumber() < lookForward && checkSum) {
				sendData(segment, lookForward, socket, IPAddress, port, true);
				System.out.println("Packet Already Delivered  Sending Duplicate Ack ");
				text += "Packet Already Delivered  Sending Duplicate Ack \n";
				savetoFile(text);
			}
			/**
			 * If a Packet  received by the receiver have checksum error so that packet is
			 * discarded and the remaining packets in the window is stored in the application buffer
			 */
			else if (!checkSum) {

				System.out.println("\nPacket " + (segment.getNumber()) + " received");
				text += "\nPacket " + (segment.getNumber()) + " received\n";
				System.out.println("Checksum Error");
				text += "Checksum Error\n";
				System.out.println("Packet " + segment.getNumber() + " Discarded\n");
				text += "Packet " + segment.getNumber() + " Discarded\n\n";
				savetoFile(text);
				segment.setNumber(-1000);
			}
			else {
				System.out.println("Packet " + segment.getNumber() + " Discarded \n");
				text += "Packet " + segment.getNumber() + " Discarded \n\n";
				savetoFile(text);
				segment.setNumber(-1000);
			}
		}
	}

	public static int sendData(Segment segment, int lookForward, DatagramSocket socket, InetAddress IP, int port, boolean flag) throws IOException {
		Acknowledge acknowledge = new Acknowledge();
		acknowledge.setNumber(segment.getNumber() + 1);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(outputStream);
		os.writeObject(acknowledge);
		byte[] replyByte = outputStream.toByteArray();
		DatagramPacket replyPacket = new DatagramPacket(replyByte, replyByte.length, IP, port);
		if ((Math.random() > packetLostRate | flag) && segment.getNumber() != -1000) {
			System.out.println(" \n Packet " + (acknowledge.getNumber()-1) + " received");
			text += " \n Packet " + (acknowledge.getNumber()-1) + " received\n";
			String reply = "Sending Acknowledgment for Packet :" + (acknowledge.getNumber() - 1) + " ";
			text += "Sending Acknowledgment for Packet :" + (acknowledge.getNumber() - 1) + " \n";
			System.out.println(reply);
			savetoFile(text);
			socket.send(replyPacket);
		} else if (segment.getNumber() != -1000 && !flag) {
			lookForward--;
		}
		return lookForward;
	}

	private static void transferGBN(DatagramSocket socket, TransmissionInformation transmissionInformation) throws IOException, ClassNotFoundException {
		ArrayList<Segment> received = new ArrayList<>();
		boolean end = false;
		int lookForward = 0;
		byte[] incomingData = new byte[1024];
		while (!end) {
			DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			socket.receive(incomingPacket);
			byte[] data = incomingPacket.getData();
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream is = new ObjectInputStream(in);
			Segment segment = (Segment) is.readObject();
			System.out.println(" \n Packet Received  = " + segment.getNumber());
			text += " \n Packet Received  = " + segment.getNumber()+"\n";
			savetoFile(text);
			char ch = segment.getData();
			int hashCode = ("" + ch).hashCode();
			boolean checkSum = (hashCode == segment.getCheckSum());
			if (!checkSum) {
				System.out.println("Error Occurred in the Data ");
				text += "Error Occurred in the Data \n";
				savetoFile(text);
			}
			if (segment.getNumber() == lookForward && segment.isLast() && checkSum) {
				lookForward++;
				received.add(segment);
				System.out.println("Last packet received ");
				text += "Last packet received \n";
				savetoFile(text);
				end = true;
			} else if (segment.getNumber() == lookForward && checkSum) {
				lookForward++;
				received.add(segment);
				// System.out.println("Packed stored ");
			}
			/**
			 * Here check sum error is shown. Here for the packet , the
			 * data which the sender sent and the receiver received is different, so the check sum error occurs
			 * and the packets following packet  in the window is discarded.
			 */
			else if (!checkSum) {
				System.out.println("Checksum Error");
				text += "Checksum Error\n";
				savetoFile(text);
				segment.setNumber(-1000);
			}

			else {
				System.out.println("Packet discarded (not in order) ");
				text += "Packet discarded (not in order) \n";
				savetoFile(text);
				segment.setNumber(-1000);
			}

			InetAddress IPAddress = incomingPacket.getAddress();
			int port = incomingPacket.getPort();
			Acknowledge acknowledge = new Acknowledge();
			acknowledge.setNumber(lookForward);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(acknowledge);
			byte[] replyByte = outputStream.toByteArray();
			DatagramPacket replyPacket = new DatagramPacket(replyByte, replyByte.length, IPAddress, port);
			/**
			 * when the receiver correctly receives a particular packet the receiver sends the ACK
			 * for that packet, this is displayed in the sender window as a message “Received ACK”.
			 * Also once the sender receives the ACK packets send by the receiver the window is
			 * moved to accommodate the next set of packets.
			 */
			if (Math.random() > packetLostRate && segment.getNumber() != -1000) {
				String reply = "Sending Acknowledgment Number :" + (acknowledge.getNumber()-1) + "\n";
				text += "Sending Acknowledgment Number :" + (acknowledge.getNumber()-1) + "\n\n";
				System.out.println(reply);
				savetoFile(text);
				socket.send(replyPacket);
				/**
				 * On the receiver side the packet haven’t reached so the
				 * receiver prints the “Packet Lost” message also as per the GBN protocol, and the
				 * following packets are discarded. For this the receiver prints the
				 * message “Packet Discarded” in the command line.
				 */
			} else if (segment.getNumber() != -1000) {
				int length = received.size();
				System.out.println("Packet Lost \n");
				text += "Packet Lost \n\n";
				savetoFile(text);
				received.remove(length - 1);
				lookForward--;
				if (end) {
					end = false;
				}
			}
		}
	}

	/**
	 * This method saves receiver log in info.txt file to be read in the graphical menu
	 * @param text
	 */
	public static void savetoFile(String text){
		try {
			File file = new File("src/main/java/com/example/cn1_project1/info.txt");
			FileWriter fr = new FileWriter(file);
			BufferedWriter br = new BufferedWriter(fr);
			PrintWriter pr = new PrintWriter(br);
			pr.println(text);
			pr.close();
			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * This method clears data of info.txt file at the first of program
	 *
	 */
	public static void clearTheFile() {
		try {
			FileWriter fwOb = new FileWriter("src/main/java/com/example/cn1_project1/info.txt", false);
			PrintWriter pwOb = new PrintWriter(fwOb, false);
			pwOb.flush();
			pwOb.close();
			fwOb.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
