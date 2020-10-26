package com.global.api.terminals.messaging;

import com.global.api.terminals.ingenico.pat.PATRequest;

public interface IPayAtTableRequestInterface {
	void onPayAtTableRequest(PATRequest payAtTableRequest);
}
