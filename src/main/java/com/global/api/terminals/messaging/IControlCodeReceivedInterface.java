package com.global.api.terminals.messaging;

import com.global.api.entities.enums.ControlCodes;

public interface IControlCodeReceivedInterface {
    void codeReceived(ControlCodes code);
}
