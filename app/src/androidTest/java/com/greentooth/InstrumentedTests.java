package com.greentooth;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
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
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTests {

    @Rule
    public ActivityTestRule<MainActivity> activityActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);

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
        Context appContext = getInstrumentation().getTargetContext();

        assertEquals("com.greentooth", appContext.getPackageName());
    }

    @Test
    public void userCanClickOnSwitch() {
        onView(withId(R.id.onSwitch)).perform(setChecked(true)).check(matches(isChecked()));
    }

    @Test
    public void switchCardWorksAsSwitch() {
        onView(withId(R.id.onSwitch)).perform(setChecked(false));
        onView(withId(R.id.switchCard)).perform(click());
        onView(withId(R.id.onSwitch)).check(matches(isChecked()));
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
        for (String selectionText: getInstrumentation().getTargetContext().getResources().
                getStringArray(R.array.wait_entries)) {
            onView(withId(R.id.timeSpinner)).perform(click());
            onData(allOf(is(instanceOf(String.class)), is(selectionText))).perform(click());
            onView(withId(R.id.timeSpinner)).check(matches(withSpinnerText(containsString(selectionText))));
        }
    }

    @Test
    public void timeCardWorksAsSpinner() {
        for (String selectionText: getInstrumentation().getTargetContext().getResources().
                getStringArray(R.array.wait_entries)) {
            onView(withId(R.id.timeCard)).perform(click());
            onData(allOf(is(instanceOf(String.class)), is(selectionText))).perform(click());
            onView(withId(R.id.timeSpinner)).check(matches(withSpinnerText(containsString(selectionText))));
        }
    }

    @Test
    public void userCanClickAbout() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.about_menu_title)).perform(click());
        onView(withId(android.R.id.message)).check(matches(withText(R.string.about_string)));
        onView(withText(R.string.about_button_positive)).perform(click());
    }

}
