package com.treyhutson.gym_occupancy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GymOccupancyTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GymOccupancyTrackerApplication.class, args);
	}

}
