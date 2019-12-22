package com.gmail.leonard.spring;

import com.sun.management.OperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;

public class Console {

    private static Console instance = new Console();
    private double maxMemory = 0;

    private Console() {}

    public static Console getInstance() {
        return instance;
    }

    public void start() {
        Thread t1 = new Thread(this::manageConsole);
        t1.setName("console");
        t1.setPriority(1);
        t1.start();

        Thread t3 = new Thread(this::trackMemory);
        t3.setName("console_memorytracker");
        t3.setPriority(1);
        t3.start();
    }

    private void manageConsole() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                if (br.ready()) {
                    String s = br.readLine();
                    if (s != null) {
                        switch (s) {
                            case "quit":
                                System.exit(0);
                                break;

                            case "stats":
                                System.out.println(getStats());
                                break;

                            case "threads":
                                StringBuilder sb = new StringBuilder();
                                for(Thread t: Thread.getAllStackTraces().keySet())
                                    sb.append(t.getName()).append(", ");

                                String str = sb.toString();
                                str = str.substring(0, str.length() - 2);

                                System.out.println("\n--- THREADS (" + Thread.getAllStackTraces().size() + ") ---");
                                System.out.println(str + "\n");

                                break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void trackMemory() {
        while(true) {
            double memoryTotal = Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0);
            double memoryUsed = memoryTotal - (Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0));
            maxMemory = Math.max(maxMemory, memoryUsed);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String getStats() {
        StringBuilder sb = new StringBuilder("\n--- STATS ---\n");

       //Memory
        double memoryTotal = Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0);
        double memoryUsed = memoryTotal - (Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0));
        sb.append("Memory: ").append(String.format("%1$.2f", memoryUsed) + " / " + String.format("%1$.2f", memoryTotal) + " MB").append("\n");

        //Max Memory
        maxMemory = Math.max(maxMemory, memoryUsed);
        sb.append("Max Memory: ").append(String.format("%1$.2f", maxMemory) + " / " + String.format("%1$.2f", memoryTotal) + " MB").append("\n");

        //Threads
        sb.append("Threads: ").append(Thread.getAllStackTraces().keySet().size()).append("\n");

        //CPU Usage
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double cpuJvm = osBean.getProcessCpuLoad();
        double cpuTotal = osBean.getSystemCpuLoad();

        sb.append("CPU JVM: ").append(Math.floor(cpuJvm * 1000) / 10 + "%").append("\n");
        sb.append("CPU Total: ").append(Math.floor(cpuTotal * 1000) / 10 + "%").append("\n");

        return sb.toString();
    }

}