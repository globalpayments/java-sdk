/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.initiateauthentication;

import com.global.api.entities.enums.AuthenticationSource;
import com.global.api.entities.enums.ChallengeWindowSize;
import com.global.api.entities.enums.ColorDepth;

class EnumHelper {

    ColorDepth getColorDepthByName(String value) {
        for (ColorDepth cd : ColorDepth.values()) {
            if (cd.getValue().equals(value)) {
                return cd;
            }
        }
        throw new RuntimeException("Non valid ColorDepth"); // for this example, just fail fast
    }

    ChallengeWindowSize getChallengeWindowSizeByName(String value) {
        for (ChallengeWindowSize cw : ChallengeWindowSize.values()) {
            if (cw.getValue().equals(value)) {
                return cw;
            }
        }
        throw new RuntimeException("Non valid ChallengeWindowSize"); // for this example, just fail fast
    }

    AuthenticationSource getAuthenticationSourceByName(String value) {
        for (AuthenticationSource as : AuthenticationSource.values()) {
            if (as.getValue().equals(value)) {
                return as;
            }
        }
        throw new RuntimeException("Non valid AuthenticationSource"); // for this example, just fail fast
    }

}