package com.global.api.logging;

import java.io.IOException;

public interface IRequestLogger {
    void RequestSent(String request) throws IOException;
    void ResponseReceived(String response) throws IOException;
}
