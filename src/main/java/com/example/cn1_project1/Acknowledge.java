package com.example.cn1_project1;

import java.io.Serializable;

/**
 *  Its object is used to send the details about the acknowledgement.
 */
public class Acknowledge implements Serializable {
	private static final long serialVersionUID = 1L;
	int number;
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return "Acknowledge{" +
				"number=" + number +
				'}';
	}
}
