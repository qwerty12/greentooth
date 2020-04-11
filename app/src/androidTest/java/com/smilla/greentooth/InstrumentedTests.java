package com.smilla.greentooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.work.testing.WorkManagerTestInitHelper.initializeTestWorkManager;
import static com.smilla.greentooth.GreenApplication.APP_KEY;
import static com.smilla.greentooth.GreenApplication.DELAY_KEY;
import static com.smilla.greentooth.GreenApplication.ENABLED_KEY;
import static com.smilla.greentooth.GreenApplication.NOTIFICATIONS_KEY;
import static com.smilla.greentooth.Util.sendNotification;
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

    private static final long TIMEOUT = 15;
    private SharedPreferences sharedPreferences;
    private Context targetContext;

    @Rule
    public ActivityTestRule<MainActivity> activityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void initPrefs() {
        targetContext = getInstrumentation().getTargetContext();
        sharedPreferences = targetContext.getSharedPreferences(APP_KEY, 0);
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
        assertEquals("com.smilla.greentooth", targetContext.getPackageName());
    }

    @Test
    public void userCanClickOnSwitch() {
        onView(withId(R.id.onSwitch)).perform(ViewActions.scrollTo())
                                     .perform(setChecked(true))
                                     .check(matches(isChecked()));
        boolean isEnabled = sharedPreferences.getBoolean(ENABLED_KEY, false);
        assertTrue(isEnabled);
        onView(withId(R.id.onSwitch)).perform(setChecked(false)).check(matches(not(isChecked())));
        isEnabled = sharedPreferences.getBoolean(ENABLED_KEY, true);
        assertFalse(isEnabled);
    }

    @Test
    public void switchCardWorksAsSwitch() {
        onView(withId(R.id.onSwitch)).perform(ViewActions.scrollTo()).perform(setChecked(false));
        onView(withId(R.id.switchCard)).perform(ViewActions.scrollTo()).perform(click());
        onView(withId(R.id.onSwitch)).perform(ViewActions.scrollTo()).check(matches(isChecked()));
        boolean isEnabled = sharedPreferences.getBoolean(ENABLED_KEY, false);
        assertTrue(isEnabled);
        onView(withId(R.id.switchCard)).perform(ViewActions.scrollTo()).perform(click());
        onView(withId(R.id.onSwitch)).perform(ViewActions.scrollTo()).check(matches(not(isChecked())));
        isEnabled = sharedPreferences.getBoolean(ENABLED_KEY, true);
        assertFalse(isEnabled);
    }

    public void userCanClickNotifSwitch() {
        onView(withId(R.id.notificationsSwitch)).perform(ViewActions.scrollTo())
                                        .perform(setChecked(true))
                                        .check(matches(isChecked()));
        boolean notifEnabled = sharedPreferences.getBoolean(NOTIFICATIONS_KEY, false);
        assertTrue(notifEnabled);
        onView(withId(R.id.notificationsSwitch)).perform(setChecked(false)).check(matches(not(isChecked())));
        notifEnabled = sharedPreferences.getBoolean(NOTIFICATIONS_KEY, true);
        assertFalse(notifEnabled);
    }

    @Test
    public void notifClickerWorksAsSwitch() {
        onView((withId(R.id.notificationsSwitch))).perform(ViewActions.scrollTo()).perform(setChecked(false));
        onView((withId(R.id.notifClicker))).perform(ViewActions.scrollTo()).perform(click());
        onView((withId(R.id.notificationsSwitch))).perform(ViewActions.scrollTo()).check(matches(isChecked()));
        boolean notifEnabled = sharedPreferences.getBoolean(NOTIFICATIONS_KEY, false);
        assertTrue(notifEnabled);
        onView((withId(R.id.notifClicker))).perform(ViewActions.scrollTo()).perform(click());
        onView((withId(R.id.notificationsSwitch))).perform(ViewActions.scrollTo()).check(matches(not(isChecked())));
        notifEnabled = sharedPreferences.getBoolean(NOTIFICATIONS_KEY, true);
        assertFalse(notifEnabled);
    }

    @Test
    public void userCanSelectTimeSpinnerOptions() {
        onView(withId(R.id.timeSpinner)).perform(ViewActions.scrollTo());
        for (String selectionText: targetContext.getResources().
                getStringArray(R.array.wait_entries)) {
            onView(withId(R.id.timeSpinner)).perform(click());
            onData(allOf(is(instanceOf(String.class)), is(selectionText))).perform(click());
            onView(withId(R.id.timeSpinner)).check(matches(withSpinnerText(containsString(selectionText))));
        }
    }

    @Test
    public void settingsClickerWorksAsSpinner() {
        onView(withId(R.id.timeSpinner)).perform(ViewActions.scrollTo());
        for (String selectionText : targetContext.getResources().
                getStringArray(R.array.wait_entries)) {
            onView((withId(R.id.timeClicker))).perform(ViewActions.scrollTo()).perform(click());
            onData(allOf(is(instanceOf(String.class)), is(selectionText))).perform(click());
            onView((withId(R.id.timeSpinner))).perform(ViewActions.scrollTo())
                                              .check(matches(withSpinnerText(containsString(selectionText))));
        }
    }

    @Test
    public void userCanClickAbout() {
        onView(withId(R.id.toolbar)).perform(scrollTo());
        openActionBarOverflowOrOptionsMenu(targetContext);
        onView(withText(R.string.about_menu_title)).perform(click());
        onView(withId(android.R.id.message))
                .check(matches(withText(containsString(targetContext.getString(R.string.about_string)))));
        onView(withText(R.string.about_button_positive)).perform(click());
    }

    public void openThemesMenu() {
        onView(withId(R.id.toolbar)).perform(scrollTo());
        openActionBarOverflowOrOptionsMenu(targetContext);
        onView(withText(R.string.themes_menu)).perform(click());
    }

    @Test
    public void userCanSetTheme() {
        openThemesMenu();
        onView(withText(R.string.light_theme)).perform(click());
        int mode = AppCompatDelegate.getDefaultNightMode();
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, mode);
        openThemesMenu();
        onView(withText(R.string.dark_theme)).perform(click());
        mode = AppCompatDelegate.getDefaultNightMode();
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, mode);
        openThemesMenu();
        onView(withText(R.string.default_theme)).perform(click());
        mode = AppCompatDelegate.getDefaultNightMode();
        if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.P)  || (Build.VERSION.SDK_INT == Build.VERSION_CODES.P
                && Build.MANUFACTURER.equalsIgnoreCase("samsung"))) {
            assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, mode);
        } else {
            assertEquals(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, mode);
        }
    }

    @SdkSuppress(minSdkVersion = 18)
    @Test
    public void testNotifications() {
        final String testTitle = "My Title";
        final String testText = "My Text";
        sendNotification(targetContext, testTitle, testText);
        UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
        mDevice.openNotification();
        mDevice.wait(Until.hasObject(By.text(testTitle)), TIMEOUT);
        UiObject2 notificationTitle = mDevice.findObject(By.text(testTitle));
        assertEquals(notificationTitle.getText(), testTitle);
        UiObject2 notificationText = mDevice.findObject(By.text(testText));
        assertEquals(notificationText.getText(), testText);
        notificationText.click();
    }

    public BluetoothAdapter bluetoothTestHelper(Boolean testArg) {
        if (!hasBluetooth() || isEmulator()) {
            return null;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        enableBluetooth(bluetoothAdapter);
        //Run without delay for tests
        sharedPreferences.edit().putInt(DELAY_KEY, 0).apply();
        //Setting the switch doesn't seem to be enough, maybe due to threading shenanigans so notifications
        //are enabled manually
        sharedPreferences.edit().putBoolean(NOTIFICATIONS_KEY, true).apply();
        onView(withId(R.id.onSwitch)).perform(ViewActions.scrollTo()).perform(setChecked(testArg));
        onView(withId(R.id.notificationsSwitch)).perform(ViewActions.scrollTo()).perform(setChecked(true));
        initializeTestWorkManager(targetContext);
        Intent intent = new Intent(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        BluetoothReceiver receiver = new BluetoothReceiver();
        receiver.onReceive(targetContext, intent);
        return bluetoothAdapter;
    }

    public boolean hasBluetooth() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    @SdkSuppress(minSdkVersion = 18)
    @Test
    public void testToggledOnAppFunction() {
        BluetoothAdapter bluetoothAdapter = bluetoothTestHelper(true);
        if (bluetoothAdapter != null) {
            final String testTitle = targetContext.getString(R.string.notification_title);
            final String testText = targetContext.getString(R.string.notification_body);
            assertFalse(bluetoothAdapter.isEnabled());
            UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
            mDevice.openNotification();
            mDevice.wait(Until.hasObject(By.textStartsWith(testTitle)), TIMEOUT);
            UiObject2 notificationTitle = mDevice.findObject(By.text(testTitle));
            assertEquals(notificationTitle.getText(), testTitle);
            UiObject2 notificationText = mDevice.findObject(By.text(testText));
            assertEquals(notificationText.getText(), testText);
            notificationText.click();
        }
    }

    @Test
    public void testToggledOffAppFunction() {
        BluetoothAdapter bluetoothAdapter = bluetoothTestHelper(false);
        if (bluetoothAdapter != null) {
            assertTrue(bluetoothAdapter.isEnabled());
        }
    }

    public boolean enableBluetooth(final BluetoothAdapter bluetoothAdapter) {
        boolean enabled;
        if (bluetoothAdapter.isEnabled()) {
            enabled = true;
        } else {
            final int WAIT_TIME = 10;
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                        if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0) == BluetoothAdapter.STATE_ON) {
                            countDownLatch.countDown();
                        }
                    }
                }
            };
            targetContext.registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            enabled = bluetoothAdapter.enable();
            try {
                countDownLatch.await(WAIT_TIME, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                enabled = false;
            }
            targetContext.unregisterReceiver(broadcastReceiver);
            if (countDownLatch.getCount() != 0L) {
                Log.d("enableBluetooth", "Activation broadcast not received in time.");
            }
        }
        return enabled;
    }

    private boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }



}
