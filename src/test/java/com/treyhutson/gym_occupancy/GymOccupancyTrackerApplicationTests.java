package com.treyhutson.gym_occupancy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class GymOccupancyTrackerApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void exposesHealthProbesWithoutDetails() throws Exception {
		mockMvc.perform(get("/actuator/health/liveness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist());

		mockMvc.perform(get("/actuator/health/readiness"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.status").value("DOWN"))
				.andExpect(jsonPath("$.components").doesNotExist());
	}

	@Test
	void doesNotExposeSensitiveActuatorEndpoints() throws Exception {
		mockMvc.perform(get("/actuator/env"))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/actuator/configprops"))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/actuator/heapdump"))
				.andExpect(status().isNotFound());
	}

}
