package com.smilla.greentooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static android.bluetooth.BluetoothProfile.GATT;
import static android.bluetooth.BluetoothProfile.HEADSET;
import static android.bluetooth.BluetoothProfile.HEALTH;
import static android.bluetooth.BluetoothProfile.HEARING_AID;
import static android.bluetooth.BluetoothProfile.HID_DEVICE;
import static android.bluetooth.BluetoothProfile.SAP;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;
import static com.smilla.greentooth.Util.isBluetoothConnected;
import static com.smilla.greentooth.Util.isBluetoothEnabled;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BluetoothUtilUnitTests {

    @Mock
    BluetoothAdapter bluetoothAdapter;

    @Test
    public void testIsBluetoothEnabled() {
        int[] onStates = {BluetoothAdapter.STATE_ON, BluetoothAdapter.STATE_TURNING_ON};
        int[] offStates = {BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF};
        for (int state : onStates) {
            when(bluetoothAdapter.getState()).thenReturn(state);
            assertTrue(isBluetoothEnabled(bluetoothAdapter));
        }
        for (int state : offStates) {
            when(bluetoothAdapter.getState()).thenReturn(state);
            assertFalse(isBluetoothEnabled(bluetoothAdapter));
        }
    }

    @Test
    public void testIsBluetoothConnectedAPI16() {
        setAPI(16);
        setMockConnectionState(HEADSET, STATE_CONNECTED);
        assertTrue(isConnected());
        setMockConnectionState(HEADSET, STATE_DISCONNECTED);
        //GATT profile should not be checked at this API level
        setMockConnectionState(GATT, STATE_CONNECTED);
        assertFalse(isConnected());
    }

    @Test
    public void testIsBluetoothConnectedAPI18() {
        setAPI(18);
        setMockConnectionState(GATT, STATE_CONNECTED);
        assertTrue(isConnected());
        setMockConnectionState(GATT, STATE_DISCONNECTED);
        //SAP profile should not be checked at this API level
        setMockConnectionState(SAP, STATE_CONNECTED);
        assertFalse(isConnected());
    }

    @Test
    public void testIsBluetoothConnectedAPI23() {
        setAPI(23);
        setMockConnectionState(SAP, STATE_CONNECTED);
        assertTrue(isBluetoothConnected(bluetoothAdapter));
        setMockConnectionState(SAP, STATE_DISCONNECTED);
        //HID Device should not be checked at this API level
        setMockConnectionState(HID_DEVICE, STATE_CONNECTED);
        assertFalse(isConnected());
    }

    @Test
    public void testIsBluetoothConnectedAPI28() {
        setAPI(28);
        setMockConnectionState(HID_DEVICE, STATE_CONNECTED);
        assertTrue(isConnected());
        setMockConnectionState(HID_DEVICE, STATE_DISCONNECTED);
        //Hearing aid should not be checked at this API level
        setMockConnectionState(HEARING_AID, STATE_CONNECTED);
        assertFalse(isConnected());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testIsBluetoothConnectedAPI29() {
        setAPI(29);
        setMockConnectionState(HEARING_AID, STATE_CONNECTED);
        assertTrue(isConnected());
        setMockConnectionState(HEARING_AID, STATE_DISCONNECTED);
        //Health profile is deprecated at this API level
        setMockConnectionState(HEALTH, STATE_CONNECTED);
        assertFalse(isConnected());
    }

    @Test
    public void testSetBluetoothProfiles() {
        int[] testArray = {1, 2, 3};
        Util.setBluetoothProfiles(testArray);
        assertEquals(testArray, Util.getBluetoothProfiles());
        setAPI(16);
        assertNotEquals(testArray, Util.getBluetoothProfiles());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetBluetoothProfiles() {
        setAPI(0);
        int[] testArray = Util.getBluetoothProfiles();
        assertThat(testArray, equalTo(new int[]{BluetoothProfile.HEADSET, BluetoothProfile.A2DP,
                BluetoothProfile.HEALTH}));
        setAPI(18);
        assertNotEquals(testArray, Util.getBluetoothProfiles());
    }

    @After
    public void teardown() {
        setAPI(0);
    }

    private void setMockConnectionState(int profile, int state) {
        when(bluetoothAdapter.getProfileConnectionState(profile)).thenReturn(state);
    }

    private boolean isConnected() {
        return Util.isBluetoothConnected(bluetoothAdapter);
    }

    private void setAPI(int level) {
        Util.setBluetoothProfiles(level);
    }
}