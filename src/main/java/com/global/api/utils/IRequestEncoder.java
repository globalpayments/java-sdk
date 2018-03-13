package com.global.api.utils;

import com.global.api.entities.exceptions.ApiException;

public interface IRequestEncoder {
    String encode(Object value);
    String decode(Object value);
}
