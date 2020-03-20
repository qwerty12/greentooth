package com.greentooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Checkable;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.work.testing.WorkManagerTestInitHelper.initializeTestWorkManager;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTests {

    private SharedPreferences sharedPreferences;
    private Context targetContext;

    @Rule
    public ActivityTestRule<MainActivity> activityActivityTestRule =
            new ActivityTestRule<MainActivity>(MainActivity.class);

    @Before
    public void initPrefs() {
        targetContext = getInstrumentation().getTargetContext();
        String prefName = targetContext.getResources().getString(R.string.preference_name);
        sharedPreferences = targetContext.getSharedPreferences(prefName, 0);
        sharedPreferences.edit().clear().apply();
    }

    public static ViewAction setChecked(final boolean checked) {
        return new ViewAction() {
            @Override
            public BaseMatcher<View> getConstraints() {
                return new BaseMatcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        return isA(Checkable.class).matches(item);
                    }

                    @Override
                    public void describeMismatch(Object item, Description mismatchDescription) {}

                    @Override
                    public void describeTo(Description description) {}
                };
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                Checkable checkableView = (Checkable) view;
                checkableView.setChecked(checked);
            }
        };
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        assertEquals("com.greentooth", targetContext.getPackageName());
    }



    @Test
    public void userCanClickOnSwitch() {
        onView(withId(R.id.onSwitch)).perform(setChecked(true)).check(matches(isChecked()));
        boolean isEnabled = sharedPreferences.getBoolean("isEnabled", false);
        assertTrue(isEnabled);
        onView(withId(R.id.onSwitch)).perform(setChecked(false)).check(matches(not(isChecked())));
        isEnabled = sharedPreferences.getBoolean("isEnabled", true);
        assertFalse(isEnabled);
    }

    @Test
    public void switchCardWorksAsSwitch() {
        onView(withId(R.id.onSwitch)).perform(setChecked(false)).check(matches(not(isChecked())));
        onView(withId(R.id.switchCard)).perform(click());
        onView(withId(R.id.onSwitch)).check(matches(isChecked()));
        boolean isEnabled = sharedPreferences.getBoolean("isEnabled", false);
        assertTrue(isEnabled);
        onView(withId(R.id.switchCard)).perform(click());
        onView(withId(R.id.onSwitch)).check(matches(not(isChecked())));
        isEnabled = sharedPreferences.getBoolean("isEnabled", true);
        assertFalse(isEnabled);
    }

    public void userCanClickNotifSwitch() {
        onView(withId(R.id.notifSwitch)).perform(setChecked(true)).check(matches(isChecked()));
    }

    @Test
    public void notifCardWorksAsSwitch() {
        onView(withId(R.id.notifSwitch)).perform(setChecked(false));
        onView(withId(R.id.notifCard)).perform(click());
        onView(withId(R.id.notifSwitch)).check(matches(isChecked()));
    }

    @Test
    public void userCanSelectTimeSpinnerOptions() {
        for (String selectionText: targetContext.getResources().
                getStringArray(R.array.wait_entries)) {
            onView(withId(R.id.timeSpinner)).perform(click());
            onData(allOf(is(instanceOf(String.class)), is(selectionText))).perform(click());
            onView(withId(R.id.timeSpinner)).check(matches(withSpinnerText(containsString(selectionText))));
        }
    }

    @Test
    public void timeCardWorksAsSpinner() {
        for (String selectionText : targetContext.getResources().
                getStringArray(R.array.wait_entries)) {
            onView(withId(R.id.timeCard)).perform(click());
            onData(allOf(is(instanceOf(String.class)), is(selectionText))).perform(click());
            onView(withId(R.id.timeSpinner)).check(matches(withSpinnerText(containsString(selectionText))));
        }
    }

    @Test
    public void userCanClickAbout() {
        openActionBarOverflowOrOptionsMenu(targetContext);
        onView(withText(R.string.about_menu_title)).perform(click());
        onView(withId(android.R.id.message)).check(matches(withText(R.string.about_string)));
        onView(withText(R.string.about_button_positive)).perform(click());
    }

    public BluetoothAdapter bluetoothTestHelper(Boolean testArg) {
        if (!hasBluetooth()) {
            return null;
        }
        final int TIME_LIMIT = 5000;
        final int WAIT_TIME = 1000;
        BluetoothManager bluetoothManager = (BluetoothManager) targetContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return null;
        }
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            // Activating Bluetooth takes a couple of seconds
            int totalWait = 0;
            while (!bluetoothAdapter.isEnabled() && totalWait < TIME_LIMIT) {
                try {
                    Thread.sleep(WAIT_TIME);
                    totalWait += WAIT_TIME;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //Run without delay for tests
        sharedPreferences.edit().putInt("wait_time", 0).apply();
        //Setting the switch doesn't seem to be enough, maybe due to threading shenanigans so notifications
        //are enabled manually
        sharedPreferences.edit().putBoolean("enableNotifications", true).apply();
        onView(withId(R.id.onSwitch)).perform(setChecked(testArg));
        onView(withId(R.id.notifSwitch)).perform(setChecked(true));
        initializeTestWorkManager(targetContext);
        Intent intent = new Intent(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        BluetoothReceiver receiver = new BluetoothReceiver();
        receiver.onReceive(targetContext, intent);
        return bluetoothAdapter;
    }

    public boolean hasBluetooth() {
        return targetContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
    }


    @Test
    public void testToggledOnAppFunction() {
        BluetoothAdapter bluetoothAdapter = bluetoothTestHelper(true);
        if (bluetoothAdapter != null) {
            assertFalse(bluetoothAdapter.isEnabled());
        }
    }

    @Test
    public void testToggledOffAppFunction() {
        BluetoothAdapter bluetoothAdapter = bluetoothTestHelper(false);
        if (bluetoothAdapter != null) {
            assertTrue(bluetoothAdapter.isEnabled());
        }
    }



}
