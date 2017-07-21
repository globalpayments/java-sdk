package com.global.api.utils;

import java.io.UnsupportedEncodingException;

public interface IRequestEncoder {
    String encode(Object value);
    String decode(Object value);
}
