package com.example.cn1_project1;

import java.io.Serializable;

/**
 *  Used to send the data, here we are sending a character as data. We are
 * randomly generating the character data.
 */
public class Segment implements Serializable,Comparable<Segment> {
	private static final long serialVersionUID = 1L;
	private int number;
	private int checkSum;
	private char data;
	boolean last = false;

	@Override
	public String toString() {
		return "Segment [number=" + number + ", checkSum=" + checkSum + ", data=" + data + "]";
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(int checkSum) {
		this.checkSum = checkSum;
	}

	public char getData() {
		return data;
	}

	public void setData(char data) {
		this.data = data;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	@Override
	public int compareTo(Segment o) {
		return this.getNumber()-(o.getNumber());
	}
}
