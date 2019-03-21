package com.global.api.network.abstractions;

public interface IDataElement<TResult> {
    TResult fromByteArray(byte[] buffer);
    byte[] toByteArray();
}
