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
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.work.testing.WorkManagerTestInitHelper.initializeTestWorkManager;
import static com.smilla.greentooth.GreenApplication.APP_KEY;
import static com.smilla.greentooth.GreenApplication.DEFAULT_DELAY;
import static com.smilla.greentooth.GreenApplication.DELAY_KEY;
import static com.smilla.greentooth.GreenApplication.ENABLED_KEY;
import static com.smilla.greentooth.GreenApplication.POST_DISABLE_NOTIFICATIONS_KEY;
import static com.smilla.greentooth.GreenApplication.PRE_DISABLE_NOTIFICATIONS_KEY;
import static com.smilla.greentooth.Util.sendNotification;
import static org.hamcrest.Matchers.containsString;
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
    //Resource id of button that expands notifications
    private static final String EXPAND_BUTTON_RES_ID = "android:id/expand_button";
    //Resource id of button to clear all notifications
    private static final String CLEAR_ALL_BUTTON_ID = "com.android.systemui:id/dismiss_text";
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

    public static ViewAction setNumberPickerValue(final int num) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                NumberPicker np = (NumberPicker) view;
                np.setValue(num);

            }

            @Override
            public String getDescription() {
                return "Set the passed number into the NumberPicker";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
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

    @Test
    public void userCanClickPreDisableNotificationSwitch() {
        onView(withId(R.id.preDisableNotificationSwitch)).perform(ViewActions.scrollTo())
                                        .perform(setChecked(true))
                                        .check(matches(isChecked()));
        boolean notifEnabled = sharedPreferences.getBoolean(PRE_DISABLE_NOTIFICATIONS_KEY, false);
        assertTrue(notifEnabled);
        onView(withId(R.id.preDisableNotificationSwitch)).perform(setChecked(false)).check(matches(not(isChecked())));
        notifEnabled = sharedPreferences.getBoolean(PRE_DISABLE_NOTIFICATIONS_KEY, true);
        assertFalse(notifEnabled);
    }

    @Test
    public void preDisableNotificationClickerWorksAsSwitch() {
        onView((withId(R.id.preDisableNotificationSwitch))).perform(ViewActions.scrollTo()).perform(setChecked(false));
        onView((withId(R.id.preDisableNotificationClicker))).perform(ViewActions.scrollTo()).perform(click());
        onView((withId(R.id.preDisableNotificationSwitch))).perform(ViewActions.scrollTo()).check(matches(isChecked()));
        boolean notifEnabled = sharedPreferences.getBoolean(PRE_DISABLE_NOTIFICATIONS_KEY, false);
        assertTrue(notifEnabled);
        onView((withId(R.id.preDisableNotificationClicker))).perform(ViewActions.scrollTo()).perform(click());
        onView((withId(R.id.preDisableNotificationSwitch))).perform(ViewActions.scrollTo()).check(matches(not(isChecked())));
        notifEnabled = sharedPreferences.getBoolean(PRE_DISABLE_NOTIFICATIONS_KEY, true);
        assertFalse(notifEnabled);
    }

    @Test
    public void userCanClickPostDisableNotificationSwitch() {
        onView(withId(R.id.postDisableNotificationSwitch)).perform(ViewActions.scrollTo())
                .perform(setChecked(true))
                .check(matches(isChecked()));
        boolean notifEnabled = sharedPreferences.getBoolean(POST_DISABLE_NOTIFICATIONS_KEY, false);
        assertTrue(notifEnabled);
        onView(withId(R.id.postDisableNotificationSwitch)).perform(setChecked(false)).check(matches(not(isChecked())));
        notifEnabled = sharedPreferences.getBoolean(POST_DISABLE_NOTIFICATIONS_KEY, true);
        assertFalse(notifEnabled);
    }

    @Test
    public void postDisableNotificationClickerWorksAsSwitch() {
        onView((withId(R.id.postDisableNotificationSwitch))).perform(ViewActions.scrollTo()).perform(setChecked(false));
        onView((withId(R.id.postDisableNotificationClicker))).perform(ViewActions.scrollTo()).perform(click());
        onView((withId(R.id.postDisableNotificationSwitch))).perform(ViewActions.scrollTo()).check(matches(isChecked()));
        boolean notifEnabled = sharedPreferences.getBoolean(POST_DISABLE_NOTIFICATIONS_KEY, false);
        assertTrue(notifEnabled);
        onView((withId(R.id.postDisableNotificationClicker))).perform(ViewActions.scrollTo()).perform(click());
        onView((withId(R.id.postDisableNotificationSwitch))).perform(ViewActions.scrollTo()).check(matches(not(isChecked())));
        notifEnabled = sharedPreferences.getBoolean(POST_DISABLE_NOTIFICATIONS_KEY, true);
        assertFalse(notifEnabled);
    }


    @Test
    public void userCanSetDelays() {
        final int targetSeconds = 33;
        final int targetMinutes = 44;
        setAndCheckDelay(0, 0);
        setAndCheckDelay(targetSeconds, 0);
        setAndCheckDelay(0, targetMinutes);
        setAndCheckDelay(targetSeconds, targetMinutes);
    }

    private void setAndCheckDelay(int targetSeconds, int targetMinutes) {
        final int targetDelay = targetMinutes * 60 + targetSeconds;
        String expectedString = Util.getDelayString(targetContext, targetDelay);
        onView((withId(R.id.timeClicker))).perform(ViewActions.scrollTo()).perform(click());
        ViewInteraction secondsInteraction = onView(withId(R.id.secondsPicker));
        ViewInteraction minutesInteraction = onView(withId(R.id.minutesPicker));
        secondsInteraction.perform(setNumberPickerValue(targetSeconds));
        minutesInteraction.perform(setNumberPickerValue(targetMinutes));
        onView(withText(R.string.okay)).perform(click());
        assertEquals(targetDelay, sharedPreferences.getInt(DELAY_KEY, DEFAULT_DELAY));
        onView(withId(R.id.delayValue)).check(matches(withText(expectedString)));
    }

    @Test
    public void userCanClickAbout() {
        onView(withId(R.id.toolbar)).perform(scrollTo());
        openActionBarOverflowOrOptionsMenu(targetContext);
        onView(withText(R.string.about_menu_title)).perform(click());
        onView(withId(android.R.id.message))
                .check(matches(withText(containsString(targetContext.getString(R.string.about_string)))));
        onView(withText(R.string.okay)).perform(click());
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
    public void testPostDisableNotifications() {
        final String testTitle = "Post Disable Title";
        final String testText = "Post Disable Text";
        clearNotifications();
        sendNotification(targetContext, testTitle, testText, GreenApplication.NOTIFICATION_TYPE_POST_DISABLE);
        UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
        mDevice.openNotification();
        mDevice.wait(Until.hasObject(By.text(testTitle)), TIMEOUT);
        UiObject2 notificationTitle = mDevice.findObject(By.text(testTitle));
        assertEquals(notificationTitle.getText(), testTitle);
        UiObject2 notificationText = mDevice.findObject(By.text(testText));
        assertEquals(notificationText.getText(), testText);
        notificationText.click();
    }

    @SdkSuppress(minSdkVersion = 18)
    @Test
    public void testPreDisableNotifications() {
        final String testTitle = "Pre Disable Title";
        final String testText = "Pre Disable Text";
        clearNotifications();
        sendNotification(targetContext, testTitle, testText, GreenApplication.NOTIFICATION_TYPE_PRE_DISABLE);
        UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
        mDevice.openNotification();
        mDevice.wait(Until.hasObject(By.text(testTitle)), TIMEOUT);
        UiObject2 notificationTitle = mDevice.findObject(By.text(testTitle));
        assertEquals(notificationTitle.getText(), testTitle);
        UiObject2 notificationText = mDevice.findObject(By.text(testText));
        assertEquals(notificationText.getText(), testText);
        notificationText.click();
    }

    @SdkSuppress(minSdkVersion = 18)
    @Test
    public void testCanClickNotificationAbortButton() {
        final String testTitle = "Abort button test title";
        final String testText = "Abort button test text";
        clearNotifications();
        sendNotification(targetContext, testTitle, testText, GreenApplication.NOTIFICATION_TYPE_PRE_DISABLE);
        UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
        mDevice.openNotification();
        mDevice.wait(Until.hasObject(By.text(testTitle)), TIMEOUT);
        //Expand notification if it starts collapsed
        UiObject2 abortButton = mDevice.findObject(By.desc(targetContext.getString(R.string.abort_job_button)));
        if (abortButton == null) {
            UiObject2 expandButton = mDevice.findObject(By.res(EXPAND_BUTTON_RES_ID));
            expandButton.click();
            abortButton = mDevice.findObject(By.desc(targetContext.getString(R.string.abort_job_button)));
        }
        abortButton.click();
    }

    @SdkSuppress(minSdkVersion = 18)
    @Test
    public void testCanClickNotificationDisableButton() {
        final String testTitle = "Disable button test title";
        final String testText = "Disable button test text";
        clearNotifications();
        sendNotification(targetContext, testTitle, testText, GreenApplication.NOTIFICATION_TYPE_PRE_DISABLE);
        UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
        mDevice.openNotification();
        mDevice.wait(Until.hasObject(By.text(testTitle)), TIMEOUT);
        //Expand notification if it starts collapsed
        UiObject2 disableButton = mDevice.findObject(By.desc(targetContext.getString(R.string.disable_button)));
        if (disableButton == null) {
            UiObject2 expandButton = mDevice.findObject(By.res(EXPAND_BUTTON_RES_ID));
            expandButton.click();
            disableButton = mDevice.findObject(By.desc(targetContext.getString(R.string.disable_button)));
        }
        disableButton.click();
        boolean isEnabled = sharedPreferences.getBoolean(ENABLED_KEY, false);
        assertFalse(isEnabled);
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
        sharedPreferences.edit().putBoolean(POST_DISABLE_NOTIFICATIONS_KEY, true).apply();
        onView(withId(R.id.onSwitch)).perform(ViewActions.scrollTo()).perform(setChecked(testArg));
        onView(withId(R.id.postDisableNotificationSwitch)).perform(ViewActions.scrollTo()).perform(setChecked(true));
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

    @Test
    public void testGetDelayString() {
        final String none = targetContext.getString(R.string.none);
        final String minute = targetContext.getString(R.string.minute);
        final String minutes = targetContext.getString(R.string.minutes);
        final String second = targetContext.getString(R.string.second);
        final String seconds = targetContext.getString(R.string.seconds);
        final int[] testParams = {0, 1, 2, 60, 61, 122};
        final String[] expectedReturns = {none, "1 " + second, "2 " + seconds, "1 " + minute, "1 " + minute + " 1 " + second,
                                          "2 " + minutes + " 2 " + seconds};
        for (int i = 0; i < testParams.length; i++) {
            assertEquals(expectedReturns[i], Util.getDelayString(targetContext, testParams[i]));
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

    private void clearNotifications() {
        UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
        mDevice.openNotification();
        UiObject2 clearAll = mDevice.findObject(By.res(CLEAR_ALL_BUTTON_ID));
        if (clearAll != null) {
            clearAll.click();
        } else {
            mDevice.pressBack();
        }
    }

}
