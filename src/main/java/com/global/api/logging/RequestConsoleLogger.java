package com.global.api.logging;

public class RequestConsoleLogger extends PrettyLogger {

    @Override
    public void RequestSent(String request) {
        logInConsole(requestFormat(request));
    }

    @Override
    public void ResponseReceived(String response) {
        logInConsole(responseFormat(response));
    }

    public String requestFormat(String request) {
        return AppendText("%s\nTimestamp:      %s\n%s", new String[]{initialLine, getTimestamp(), request});
    }

    public String responseFormat(String response) {
        return AppendText("%s\nTimestamp:     %s\n%s\n%s\n", new String[]{middleLine, getTimestamp(), response, endLine});
    }

    private void logInConsole(String text) {
        System.out.println(super.AppendText("%s", new String[]{text}));
    }

}