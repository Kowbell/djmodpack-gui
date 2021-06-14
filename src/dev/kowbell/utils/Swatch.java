package dev.kowbell.utils;

import java.util.HashMap;

public class Swatch {

    private static HashMap<String, Long> activeTimers = new HashMap<>();

    public static void StartTimer(String inTag) {
        activeTimers.put(inTag, System.nanoTime());
    }

    public static void StopTimer(String inTag) {
        StopTimer(inTag, true);
    }

    public static void StopTimer(String inTag, boolean inReportMemory) {
        long stopTime = System.nanoTime();
        long startTime = activeTimers.get(inTag);
        long duration = stopTime - startTime;


        System.out.printf("Stopwatch '%s': %.2f", inTag, (float)duration / 1e6f);

        if (inReportMemory == true) {
            System.out.printf(" (mem usage: %.2f kb)", (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1024.f);
        }

        System.out.println();
    }
}
