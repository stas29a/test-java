package com.test.App;

import com.test.App.Entities.Timestamp;
import com.test.App.Services.DbStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class AppApplication implements CommandLineRunner
{
	static Logger LOG = LoggerFactory.getLogger(AppApplication.class);

	private DbStorage dbStorage;

	@Autowired
	public AppApplication(DbStorage dbStorage)
	{
		this.dbStorage = dbStorage;
	}

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.info("start app");

		if (args.length == 0)
			savingTimestamps();
		else if (args[0].equals("-p"))
		{
			showTimestamps();
		}
	}

	private void showTimestamps()
	{
		List<Timestamp> list = dbStorage.fetchAll();

		for (Timestamp timestamp : list)
			LOG.info(timestamp.getId() + " " + timestamp.getTimestamp().toString());
	}

	private void savingTimestamps()
	{
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			Timestamp timestamp = new Timestamp();
			timestamp.setTimestamp(new Date());

			LOG.info("saving timestamp " + timestamp.getTimestamp().toString());
			dbStorage.saveAsync(timestamp);

		}, 0, 1, TimeUnit.SECONDS);
	}
}
