package io.rocktest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.rocktest.modules.RockModule;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
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

            Reflections reflections = new Reflections("io.rocktest.modules");
            Set<Class<? extends RockModule>> classes = reflections.getSubTypesOf(RockModule.class);
            for (Class<? extends RockModule> aClass : classes) {
                System.out.println(aClass.getName());
            }


        if(args.length>=1) {

            File parent =new File(args[0]).getParentFile();
            String dir=".";

            if(parent != null) {
                dir=parent.getName();
            }

            ArrayList<String> stack=new ArrayList<>();
            stack.add(new File(args[0]).getName().replace(".yaml",""));

            // HashMap for each instance of modules
            scenario.setModuleInstances(new HashMap<>());

            Map<String,Object> glob=new HashMap<>();

            String err=scenario.main(args[0],dir,new HashMap<String, Map<String,Object>>(),stack,glob);
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