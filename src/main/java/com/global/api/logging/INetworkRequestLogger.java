package com.global.api.logging;

public interface INetworkRequestLogger extends IRequestLogger{
        void logInfo(String message);
        void logMessage(String message, String value);
        <E extends Enum<E>> void logMessage(String fieldName, E value);
}
