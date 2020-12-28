package com.gmail.leonard.spring.backend;

import com.gmail.leonard.spring.backend.util.ExceptionUtil;
import com.gmail.leonard.spring.backend.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class MainScheduler {

    private final static Logger LOGGER = LoggerFactory.getLogger(MainScheduler.class);

    private static final MainScheduler ourInstance = new MainScheduler();
    public static MainScheduler getInstance() { return ourInstance; }
    private MainScheduler() { }

    private final Timer timer = new Timer();
    private final Timer poller = new Timer();
    private final Timer timeOutMonitorer = new Timer();
    private final ConcurrentLinkedQueue<ScheduleSlot> slots = new ConcurrentLinkedQueue<>();

    public void schedule(long millis, String name, Runnable listener) {
        ScheduleSlot scheduleSlot = new ScheduleSlot(name);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    slots.add(scheduleSlot);
                    monitorTimeOuts(scheduleSlot);
                    listener.run();
                } catch (Throwable e) {
                    LOGGER.error("Unchecked exception in schedule timer");
                }
                slots.remove(scheduleSlot);
            }
        }, millis);
    }

    public void schedule(long amount, TemporalUnit unit, String name, Runnable listener) {
        long millis = Duration.of(amount, unit).toMillis();
        schedule(millis, name, listener);
    }

    public void schedule(Instant dueInstant, String name, Runnable listener) {
        long millis = TimeUtil.getMilisBetweenInstants(Instant.now(), dueInstant);
        schedule(millis, name, listener);
    }

    /*
    Keeps polling in the specified time interval as long as the listener returns true
     */
    public void poll(long millis, String name, Supplier<Boolean> listener) {
        ScheduleSlot scheduleSlot = new ScheduleSlot(name);
        poller.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    slots.add(scheduleSlot);
                    monitorTimeOuts(scheduleSlot);
                    if (listener.get()) {
                        poll(millis, name, listener);
                    }
                } catch (Throwable e) {
                    LOGGER.error("Unchecked exception in poll timer");
                }
                slots.remove(scheduleSlot);
            }
        }, millis);
    }

    public void poll(long amount, TemporalUnit unit, String name, Supplier<Boolean> listener) {
        long millis = Duration.of(amount, unit).toMillis();
        poll(millis, name, listener);
    }

    private void monitorTimeOuts(ScheduleSlot slot) {
        Thread runnerThread = Thread.currentThread();
        timeOutMonitorer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (slots.contains(slot)) {
                    Exception e = ExceptionUtil.generateForStack(runnerThread);
                    LOGGER.error("Task \"{}\" stuck in scheduler {}", slot.name, runnerThread.getName(), e);
                }
            }
        }, 500);
    }


    private static class ScheduleSlot {

        private final String name;

        public ScheduleSlot(String name) {
            this.name = name;
        }

    }

}
