package com.global.api.gateways;

import com.global.api.builders.Secure3dBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.exceptions.ApiException;

public interface ISecure3dProvider {
    Secure3dVersion getVersion();

    Transaction processSecure3d(Secure3dBuilder builder) throws ApiException;
}
