package edu.hust;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import edu.hust.utils.GeneralValue;

@EnableScheduling
@SpringBootApplication
public class HUSTServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HUSTServerApplication.class, args);
	}

	@Scheduled(cron = "0 1 5 * * ?")
	public void scheduleFixedRateTask() {
	    System.out.println(
	    		GeneralValue.isCheckStudentRollcallToday);
	}
}
