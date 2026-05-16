package com.example.stegoapp.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ExecutorProvider {
    private static final ExecutorService SINGLE = Executors.newSingleThreadExecutor();
    private ExecutorProvider() {}
    public static ExecutorService get() { return SINGLE; }
}
