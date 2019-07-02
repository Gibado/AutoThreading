package com.gibado.basics;

import com.gibado.basics.workunit.Logger;
import com.gibado.basics.WorkUnit.State;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class FullTest {
    private ProcessPlant processPlant = new ProcessPlant(8);

    @Test
    public void exampleMorningPrepTest() throws Exception {
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
        Sharable<Logger> logShare = new Sharable<>(logger);
        String bodyKey = "body";
        Sharable<StringBuilder> bodyShare = new Sharable<>(new StringBuilder("My body:"));
        String mindKey = "mind";
        Sharable<StringBuilder> mindShare = new Sharable<>(new StringBuilder("My mind:"));
        String locationKey = "location";
        Sharable<StringBuilder> locationShare = new Sharable<>(new StringBuilder("home"));

        WorkUnit goToBathroom = new WorkUnit() {
            @Override
            public void performTask(Map<String, ?> params) {
                StringBuilder body = (StringBuilder) params.get(bodyKey);
                Logger logger = (Logger) params.get(logKey);
                body.append(" (used bathroom)");
                logger.log("Go to the Bathroom");
                logger.log(body.toString());
            }
        };
        goToBathroom.addRequiredParam(bodyKey, bodyShare);
        goToBathroom.addRequiredParam(logKey, logShare);

        WorkUnit eat = new WorkUnit() {
            @Override
            public void performTask(Map<String, ?> params) {
                StringBuilder body = (StringBuilder) params.get(bodyKey);
                Logger logger = (Logger) params.get(logKey);
                body.append(" (ate food)");
                logger.log("Eat");
                logger.log(body.toString());
            }
        };
        eat.addRequiredParam(bodyKey, bodyShare);
        eat.addRequiredParam(logKey, logShare);

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
                logger.log("Get Dressed");
                logger.log(body.toString());
            }
        };
        getDressed.addRequiredParam(bodyKey, bodyShare);
        getDressed.addRequiredParam(locationKey, locationShare);
        getDressed.addRequiredParam(logKey, logShare);

        WorkUnit coffee = new WorkUnit() {
            @Override
            public void performTask(Map<String, ?> params) {
                Logger logger = (Logger) params.get(logKey);
                StringBuilder body = (StringBuilder) params.get(bodyKey);
                body.append(" (caffinated)");
                logger.log("Drink coffee");
                logger.log(body.toString());
            }
        };
        coffee.addRequiredParam(bodyKey, bodyShare);
        coffee.addRequiredParam(logKey, logShare);

        WorkUnit checkCalendar = new WorkUnit() {
            @Override
            public void performTask(Map<String, ?> params) {
                Logger logger = (Logger) params.get(logKey);
                StringBuilder mind = (StringBuilder) params.get(mindKey);
                mind.append(" (calendar checked)");
                logger.log("Check calendar");
                logger.log(mind.toString());
            }
        };
        checkCalendar.addRequiredParam(mindKey, mindShare);
        checkCalendar.addRequiredParam(logKey, logShare);

        WorkUnit news = new WorkUnit() {
            @Override
            public void performTask(Map<String, ?> params) {
                Logger logger = (Logger) params.get(logKey);
                StringBuilder mind = (StringBuilder) params.get(mindKey);
                mind.append(" (latest news)");
                logger.log("Check the news");
                logger.log(mind.toString());
            }
        };
        news.addRequiredParam(mindKey, mindShare);
        news.addRequiredParam(logKey, logShare);

        WorkUnit drive = new WorkUnit() {
            @Override
            public void performTask(Map<String, ?> params) {
                Logger logger = (Logger) params.get(logKey);
                StringBuilder location = (StringBuilder) params.get(locationKey);
                location.delete(0, location.length());
                location.append("work");
                logger.log("Drive to work");
                logger.log(location.toString());
            }
        };
        drive.addRequiredParam(locationKey, locationShare);
        drive.addRequiredParam(logKey, logShare);

        WorkUnit work = new WorkUnit() {
            @Override
            public void performTask(Map<String, ?> params) {
                Logger logger = (Logger) params.get(logKey);
                StringBuilder mind = (StringBuilder) params.get(mindKey);
                mind.append(" ( ready to work)");
                StringBuilder body = (StringBuilder) params.get(bodyKey);
                StringBuilder location = (StringBuilder) params.get(locationKey);
                logger.log("Start work");
                logger.log(body.toString());
                logger.log(mind.toString());
                logger.log(location.toString());
            }
        };
        work.addRequiredParam(mindKey, mindShare);
        work.addRequiredParam(locationKey, locationShare);
        work.addRequiredParam(bodyKey, bodyShare);
        work.addRequiredParam(logKey, logShare);

        // Define work order
        drive.setDependents(Arrays.asList(getDressed, eat));
        work.setDependents(Arrays.asList(drive, goToBathroom, coffee, checkCalendar, news));

        processPlant.queueWorkUnit(work);
        while (!State.ERROR.equals(work.updateState()) && !State.DONE.equals(work.updateState())) {
            Thread.sleep(1000);
        }
        logger.logWorkUnitTree(work);

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
        assertTrue(consoleOutput.contains("DONE in "));
        assertTrue(consoleOutput.contains("-DONE in "));
        assertTrue(consoleOutput.contains("--DONE in "));
        assertTrue(consoleOutput.endsWith(" ms"));
    }
}
