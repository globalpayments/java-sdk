package com.example.restservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class EndpointsControllerTests {

	@Autowired
	private MockMvc mockMvc;
	public static final String ACCESS_TOKEN_ENDPOINT = "/accesstoken";

	@Test
	public void getAccessToken() throws Exception {

		this.mockMvc
				.perform(post(ACCESS_TOKEN_ENDPOINT)
						.contentType(MediaType.APPLICATION_JSON)
						.content(
									"{"																			+
											"\"appId\"       :    \"x0lQh0iLV0fOkmeAyIDyBqrP9U5QaiKc\",\r\n"	+
											"\"appKey\"      :    \"DYcEE2GpSzblo0ib\",\r\n"					+
											"\"permissions\" :    []\r\n"										+
									"}"
								)
						)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());
	}

}