package com.global.api.utils;

public interface IRequestEncoder {
    String encode(Object value);
    String decode(Object value);
}
