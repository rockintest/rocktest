package io.rocktest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class RocktestApplication
		implements CommandLineRunner {

	@Autowired
	private Scenario scenario;
	
	@Value("${topic.name}")
	private String topicName;

	@Value("${topic.partitions-num}")
	private Integer partitions;

	@Value("${topic.replication-factor}")
	private short replicationFactor;


	private static Logger LOG = LoggerFactory
			.getLogger(RocktestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RocktestApplication.class, args);
	}

	@Override
	public void run(String... args) throws IOException, InterruptedException {
		if(args.length>=1) {

			File parent =new File(args[0]).getParentFile();
			String dir=".";

			if(parent != null) {
				dir=parent.getName();
			}

			ArrayList<String> stack=new ArrayList<>();
			stack.add(new File(args[0]).getName().replace(".yaml",""));

			Map<String, List<Map<String,Object>>> functions = new HashMap<>();

			String err=scenario.run(args[0],dir,new HashMap<String, Map<String,String>>(),stack,functions);
			if(err!=null) {
				LOG.error("Error : {}",err);
				System.exit(1);
			} else {
				LOG.info("Scenario SUCCESS");
				System.exit(0);
			}
		} else {
			LOG.error("1 argument missing (scenario)");
		}
	}
}