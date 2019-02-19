package com.global.api.tests.terminals.hpa;

import java.util.Random;
import org.joda.time.DateTime;
import com.global.api.terminals.IRequestIdProvider;

public class RequestIdProvider implements IRequestIdProvider {
    private Random random;

    public RequestIdProvider() {
        random = new Random(DateTime.now().getMillisOfSecond());
    }

    public int getRequestId() {
        return 100000 + random.nextInt(999999);
    }
}
