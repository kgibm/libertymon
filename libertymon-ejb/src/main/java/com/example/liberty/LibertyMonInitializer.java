package com.example.liberty;

import javax.ejb.Schedule;
import javax.ejb.Singleton;

@Singleton
public class LibertyMonInitializer {
	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	public void run() {
		System.out.println("Called");
	}
}
