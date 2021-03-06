package com.gibado.basics;

import com.gibado.basics.sharable.Sharable;
import com.gibado.basics.sharable.SharableMap;
import com.gibado.basics.workunit.ITask;
import com.gibado.basics.workunit.ITaskRunner;
import com.gibado.basics.workunit.WorkUnit;
import com.gibado.basics.workunit.example.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertTrue;

public class FullTest {
    private IProcessPlant processPlant;

    @Before
    public void setup() {
        processPlant = new ProcessPlant(8);
    }

    @Test
    public void exampleMorningPrepTest() {
        // Things that need to be done to prepare in the morning
        // Go to the bathroom
        // Eat
        // Get dressed
        // Have coffee
        // Check Calendar
        // Listen to the news
        // drive to work
        // ready for work

        Logger logger = new Logger();
        String logKey = "log";
        Sharable<Logger> logShare = new Sharable<>(null);
        logShare.assignValue(logger);
        String bodyKey = "body";
        Sharable<StringBuilder> bodyShare = new Sharable<>(new StringBuilder("My body:"));
        String mindKey = "mind";
        Sharable<StringBuilder> mindShare = new Sharable<>(new StringBuilder("My mind:"));
        String locationKey = "location";
        Sharable<StringBuilder> locationShare = new Sharable<>(new StringBuilder("home"));

        StringAppendWorkUnit goToBathroom = new StringAppendWorkUnit("Go to the Bathroom", "used bathroom", bodyShare, logShare);
        StringAppendWorkUnit eat = new StringAppendWorkUnit("Eat", "ate food", bodyShare, logShare);

        String getDressedName = "Get Dressed";
        WorkUnit getDressed = new WorkUnit() {
            @Override
            public void performTask(Map<String, ?> params) {
                Logger logger = (Logger) params.get(logKey);
                StringBuilder location = (StringBuilder) params.get(locationKey);
                if (!location.toString().equals("home")) {
                    throw new IllegalStateException("Can't get dressed unless at home");
                }
                StringBuilder body = (StringBuilder) params.get(bodyKey);
                body.append(" (dressed)");
                logger.log(getName());
                logger.log(body.toString());
            }
        };
        getDressed.setName(getDressedName);
        getDressed.addResource(bodyKey, bodyShare);
        getDressed.addResource(locationKey, locationShare);
        getDressed.addResource(logKey, logShare);

        StringAppendWorkUnit coffee = new StringAppendWorkUnit("Drink coffee", "caffinated", bodyShare, logShare);
        StringAppendWorkUnit checkCalendar = new StringAppendWorkUnit("Check calendar", "calendar checked", mindShare, logShare);
        StringAppendWorkUnit news = new StringAppendWorkUnit("Check the news", "latest news", mindShare, logShare);

        String driveName = "Drive to work";
        WorkUnit drive = new WorkUnit() {
            @Override
            public void performTask(Map<String, ?> params) {
                Logger logger = (Logger) params.get(logKey);
                StringBuilder location = (StringBuilder) params.get(locationKey);
                location.delete(0, location.length());
                location.append("work");
                logger.log(getName());
                logger.log(location.toString());
            }
        };
        drive.setName(driveName);
        drive.addResource(locationKey, locationShare);
        drive.addResource(logKey, logShare);

        StringAppendWorkUnit work = new StringAppendWorkUnit("Start work", "ready to work", mindShare, logShare);
        PrintResultWorkUnit results = new PrintResultWorkUnit(logShare, mindShare, locationShare, bodyShare);

        // Define work order
        results.setDependents(work);
        work.setDependents(drive, goToBathroom, coffee, checkCalendar, news);
        drive.setDependents(getDressed, eat);

        processPlant.queueWorkUnit(results);
        logger.logWorkUnitTree(results);

        String consoleOutput = logger.toString().trim();

        assertTrue(consoleOutput.contains("Get Dressed"));
        assertTrue(consoleOutput.contains("(dressed)\n"));
        assertTrue(consoleOutput.contains("Eat"));
        assertTrue(consoleOutput.contains("(ate food)\n"));
        assertTrue(consoleOutput.contains("Go to the Bathroom"));
        assertTrue(consoleOutput.contains("(used bathroom)\n"));
        assertTrue(consoleOutput.contains("Drink coffee"));
        assertTrue(consoleOutput.contains("(caffinated)\n"));
        assertTrue(consoleOutput.contains("Check calendar"));
        assertTrue(consoleOutput.contains("(calendar checked)\n"));
        assertTrue(consoleOutput.contains("Check the news"));
        assertTrue(consoleOutput.contains("(latest news)\n"));
        assertTrue(consoleOutput.contains("Drive to work"));
        assertTrue(consoleOutput.contains("Start work"));
        assertTrue(consoleOutput.contains("(ready to work)\n"));

        assertTrue(consoleOutput.contains("Start work - DONE in "));
        assertTrue(consoleOutput.contains("-Drive to work - DONE in "));
        assertTrue(consoleOutput.contains("--Get Dressed - DONE in "));
        assertTrue(consoleOutput.contains("--Eat - DONE in "));
        assertTrue(consoleOutput.contains("-Go to the Bathroom - DONE in "));
        assertTrue(consoleOutput.contains("-Drink coffee - DONE in "));
        assertTrue(consoleOutput.contains("-Check calendar - DONE in "));
        assertTrue(consoleOutput.contains("-Check the news - DONE in "));
    }

    @Test
    public void timeOutTest() {
        // Attempt to hit the timeout exception
        Logger logger = new Logger();
        Sharable<Logger> logShare = new Sharable<>(logger);
        Sharable<StringBuilder> bodyShare = new Sharable<>(new StringBuilder("My body:"));
        List<ITaskRunner> dependents = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            StringAppendWorkUnit goToBathroom = new StringAppendWorkUnit("Go to the Bathroom", "used bathroom", bodyShare, logShare);
            goToBathroom.setTimeout(0);
            dependents.add(goToBathroom);
        }

        PrintResultWorkUnit results = new PrintResultWorkUnit(logShare, bodyShare);
        results.setDependents(dependents);

        processPlant.queueWorkUnit(results);
        logger.logWorkUnitTree(results);

        String consoleOutput = logger.toString().trim();
        assertTrue(consoleOutput.contains("Print results - ERROR"));
        assertTrue(consoleOutput.contains("-Go to the Bathroom - ERROR in "));
    }

    @Test
    public void exampleMorningPrepShellTest() {
        // Refers to the exampleMorningPrepTest
        // Things that need to be done to prepare in the morning
        // Go to the bathroom
        // Eat
        // Get dressed
        // Have coffee
        // Check Calendar
        // Listen to the news
        // drive to work
        // ready for work

        SharableMap sharableMap = new SharableMap();
        String logKey = "log";
        Logger logger = new Logger();
        Sharable<Logger> logShare = new Sharable<>(logger);
        sharableMap.addResource(logKey, logShare);
        String bodyKey = "body";
        sharableMap.addNewResource(bodyKey, new StringBuilder("My body:"));
        String mindKey = "mind";
        sharableMap.addNewResource(mindKey, new StringBuilder("My mind:"));
        String locationKey = "location";
        sharableMap.addNewResource(locationKey, new StringBuilder("home"));
        String resourceMapKey = "resourceMap";
        sharableMap.addNewResource(resourceMapKey, sharableMap.getResourceMap());
        processPlant.addSharableMap(sharableMap);


        ITask goToBathroom = new StringAppendTask("Go to the Bathroom", "used bathroom", bodyKey);
        ITask eat = new StringAppendTask("Eat", "ate food", bodyKey);

        ITask getDressed = new ITask() {
            @Override
            public Collection<ITask> getDependents() {
                return Collections.EMPTY_LIST;
            }

            @Override
            public String getName() {
                return "Get Dressed";
            }

            @Override
            public void performTask(Map<String, ?> params) {
                Logger logger = (Logger) params.get(logKey);
                StringBuilder location = (StringBuilder) params.get(locationKey);
                if (!location.toString().equals("home")) {
                    throw new IllegalStateException("Can't get dressed unless at home");
                }
                StringBuilder body = (StringBuilder) params.get(bodyKey);
                body.append(" (dressed)");
                logger.log(getName());
                logger.log(body.toString());
            }

            @Override
            public void exceptionHandling(Exception exception, Map<String, ?> params) { }

            @Override
            public Collection<String> getResourceKeys() {
                return Arrays.asList(logKey, locationKey, bodyKey);
            }
        };

        ITask coffee = new StringAppendTask("Drink coffee", "caffinated", bodyKey);
        ITask checkCalendar = new StringAppendTask("Check calendar", "calendar checked", mindKey);
        ITask news = new StringAppendTask("Check the news", "latest news", mindKey);

        ITask drive = new ITask() {
            @Override
            public Collection<ITask> getDependents() {
                return Arrays.asList(getDressed, eat);
            }

            @Override
            public String getName() {
                return "Drive to work";
            }

            @Override
            public void performTask(Map<String, ?> params) {
                Logger logger = (Logger) params.get(logKey);
                StringBuilder location = (StringBuilder) params.get(locationKey);
                location.delete(0, location.length());
                location.append("work");
                logger.log(getName());
                logger.log(location.toString());
            }

            @Override
            public void exceptionHandling(Exception exception, Map<String, ?> params) { }

            @Override
            public Collection<String> getResourceKeys() {
                return Arrays.asList(locationKey, logKey);
            }
        };

        ITask work = new StringAppendTask("Start work", "ready to work", mindKey) {
            @Override
            public Collection<ITask> getDependents() {
                return Arrays.asList(drive, goToBathroom, coffee, checkCalendar, news);
            }
        };
        ITask results = new PrintResultTask() {
            @Override
            public Collection<ITask> getDependents() {
                return Arrays.asList(work);
            }
        };

        ITaskRunner resultRunner = processPlant.queueTask(results);
        logger.logWorkUnitTree(resultRunner);

        String consoleOutput = logger.toString().trim();

        assertTrue(consoleOutput.contains("Get Dressed"));
        assertTrue(consoleOutput.contains("(dressed)\n"));
        assertTrue(consoleOutput.contains("Eat"));
        assertTrue(consoleOutput.contains("(ate food)\n"));
        assertTrue(consoleOutput.contains("Go to the Bathroom"));
        assertTrue(consoleOutput.contains("(used bathroom)\n"));
        assertTrue(consoleOutput.contains("Drink coffee"));
        assertTrue(consoleOutput.contains("(caffinated)\n"));
        assertTrue(consoleOutput.contains("Check calendar"));
        assertTrue(consoleOutput.contains("(calendar checked)\n"));
        assertTrue(consoleOutput.contains("Check the news"));
        assertTrue(consoleOutput.contains("(latest news)\n"));
        assertTrue(consoleOutput.contains("Drive to work"));
        assertTrue(consoleOutput.contains("Start work"));
        assertTrue(consoleOutput.contains("(ready to work)\n"));

        assertTrue(consoleOutput.contains("-Start work - DONE in "));
        assertTrue(consoleOutput.contains("--Drive to work - DONE in "));
        assertTrue(consoleOutput.contains("---Get Dressed - DONE in "));
        assertTrue(consoleOutput.contains("---Eat - DONE in "));
        assertTrue(consoleOutput.contains("--Go to the Bathroom - DONE in "));
        assertTrue(consoleOutput.contains("--Drink coffee - DONE in "));
        assertTrue(consoleOutput.contains("--Check calendar - DONE in "));
        assertTrue(consoleOutput.contains("--Check the news - DONE in "));
    }
}
