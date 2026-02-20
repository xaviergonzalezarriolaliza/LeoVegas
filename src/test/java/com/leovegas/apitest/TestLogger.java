package com.leovegas.apitest;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.Optional;

public class TestLogger implements TestWatcher, BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";

    private String color(String code, String message) {
        return code + message + RESET;
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        System.out.println(color(CYAN, "[RUNNING] " + context.getDisplayName()));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        // no-op
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        System.out.println(color(GREEN, "[TEST] " + context.getDisplayName() + " - PASS"));
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String message = "[TEST] " + context.getDisplayName() + " - FAIL";
        if (cause != null) message += ": " + cause.toString();
        System.out.println(color(RED, message));
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        System.out.println(color(YELLOW, "[TEST] " + context.getDisplayName() + " - DISABLED: " + reason.orElse("no reason")));
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        String message = "[TEST] " + context.getDisplayName() + " - ABORTED";
        if (cause != null) message += ": " + cause.toString();
        System.out.println(color(YELLOW, message));
    }
}
