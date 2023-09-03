package com.example.cn1_project1;

import java.io.Serializable;

/**
 * Used to send the initial SYN data which includes packet size, window size,
 * Number of packets to the receiver.
 */
public class TransmissionInformation implements Serializable {
	private static final long serialVersionUID = 1L;

	private int flowControl;

	private int windowSize;

	private long packetSize;

	private int packetCount;

	public int getFlowControl() {
		return flowControl;
	}

	public void setFlowControl(int flowControl) {
		this.flowControl = flowControl;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public long getPacketSize() {
		return packetSize;
	}

	public void setPacketSize(long packetSize) {
		this.packetSize = packetSize;
	}

	public int getPacketCount() {
		return packetCount;
	}

	public void setPacketCount(int packetCount) {
		this.packetCount = packetCount;
	}

	@Override
	public String toString() {
		return "TransmissionInformation [Flow control techniques=" + flowControl + ", windowSize=" + windowSize + ", packetSize=" + packetSize
				+ ", packetCount=" + packetCount + "]";
	}

}
