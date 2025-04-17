package com.global.api.terminals.abstractions;

public interface IAidlService {
    void onSendAidlMessage(String data, IAidlCallback aidlCallback);

    void onAidlResponse(String data);
}
