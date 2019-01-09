package com.nyp.fypj.smartbackpackapp.test.core;

import android.support.test.uiautomator.UiDevice;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class BaseTest {

    @BeforeClass
    public static void setUp() {
        UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());
        try {
            uiDevice.executeShellCommand("settings put secure show_ime_with_hard_keyboard 0");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @AfterClass
    public static void tearDown() {
        UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());
        try {
            uiDevice.executeShellCommand("settings put secure show_ime_with_hard_keyboard 1");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
