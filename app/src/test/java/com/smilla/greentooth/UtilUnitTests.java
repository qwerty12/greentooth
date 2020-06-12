package com.smilla.greentooth;

import android.content.SharedPreferences;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.smilla.greentooth.GreenApplication.DEFAULT_DELAY;
import static com.smilla.greentooth.GreenApplication.DELAY_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtilUnitTests {

    @Mock
    SharedPreferences mockPreferences;

    @Test
    public void testGetSaneDelay() {
        final int[] testParameters = {50, -1, 3600};
        final int[] expectedReturns = {50, 0, 3599};
        for (int i = 0; i < testParameters.length; i++) {
            when(mockPreferences.getInt(DELAY_KEY, DEFAULT_DELAY)).thenReturn(testParameters[i]);
            assertEquals(expectedReturns[i], Util.getSaneDelay(mockPreferences));
        }
    }
}