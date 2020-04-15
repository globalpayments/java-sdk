package com.global.api.tests.terminals.ingenico;

import java.util.Random;

import org.joda.time.DateTime;

import com.global.api.terminals.IRequestIdProvider;

public class RandomIdProvider implements IRequestIdProvider {
	private Random random;
	public RandomIdProvider() {
		random = new Random(DateTime.now().getMillis());
	}
	
	public int getRequestId() {
		return 100000 + random.nextInt(999999);
	}
}
