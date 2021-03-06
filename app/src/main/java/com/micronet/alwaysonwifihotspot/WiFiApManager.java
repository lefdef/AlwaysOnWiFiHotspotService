package com.micronet.alwaysonwifihotspot;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Scanner;

/**
 * Created by brigham.diaz on 12/12/2016.
 *
 */
public class WiFiApManager {
    private static String TAG = "AOWHS - WifiApManager";

  /*  private static Method getWifiApStateMethod;
    private static Method isWifiApEnabledMethod;

    static {
        for (Method method : WifiManager.class.getDeclaredMethods()) {
            switch (method.getName()) {

                case "getWifiApState":
                    getWifiApStateMethod = method;
                    break;
                case "isWifiApEnabled":
                    isWifiApEnabledMethod = method;
                    break;
            }
        }
    }*/

    /* Wifi AP values and intent string from obc_android SDK */
    public static final int WIFI_AP_STATE_DISABLING = 10; // Wi-Fi AP is currently being disabled. The state will change to WIFI_AP_STATE_DISABLED if it finishes successfully.
    public static final int WIFI_AP_STATE_DISABLED = 11; // Wi-Fi AP is disabled.
    public static final int WIFI_AP_STATE_ENABLING = 12; // Wi-Fi AP is currently being enabled. The state will change to WIFI_AP_STATE_ENABLED if it finishes successfully.
    public static final int WIFI_AP_STATE_ENABLED = 13; // Wi-Fi AP is enabled.
    public static final int WIFI_AP_STATE_FAILED = 14; // Wi-Fi AP is in a failed state. This state will occur when an error occurs during enabling or disabling

    // Broadcast intent action indicating that Wi-Fi AP has been enabled, disabled, enabling, disabling, or failed.
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";


    public static int getWifiApState(Context context, String handlerCount) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            // access isWifiApEnabled method by reflection
            Method isWifiApEnabledMethod = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            isWifiApEnabledMethod.setAccessible(true);
            int isWifiAponvalue= (Integer) isWifiApEnabledMethod.invoke(wifiManager);

            //TODO: Test this code

            /* Verify that the IP got created
             * There was a bug where IP sometimes does not get created even though the state shows hotspot ON
             */
            Process process = Runtime.getRuntime().exec("netstat");
            Scanner scanner = new Scanner(process.getInputStream(), "IBM850");
            scanner.useDelimiter("\\A");
            String netstat_str = scanner.next();
            if (netstat_str.contains("192.168.43.1")) {
                Log.d(TAG, "Found address 192.168.43.1 in netstat");
            }
            else {
                /**
                * If for some reason the IP was not created then set the Wi-Fi AP (Hotspot) UI state to the correct state.
                * Delay necessary? Verify.
                */
                ReadWriteFile.LogToFile(context, handlerCount, "Didn't find 192.168.43.1");
                Log.d(TAG, "Did NOT find 192.168.43.1 in netstat, Considering Wi-Fi AP (Hotspot) is disabled!");
                //Disable Wi-Fi AP
                ReadWriteFile.LogToFile(context, handlerCount, "Disabling Wi-Fi Ap since 192.168.43.1 wasn't found in netstat!!");
                WiFiApManager.setWiFiApState(context, false);
                try {
                    Thread.sleep(5000);
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
                isWifiAponvalue = WIFI_AP_STATE_DISABLED;
            }

            return isWifiAponvalue;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return WIFI_AP_STATE_FAILED;
    }

    /**
     * Start or stop the AccessPoint using the saved configuration.
     * Uses reflection to access System API
     *
     * @param context Activity context
     * @param enabled true to enable and false to disable
     * @return {@code true} if the operation succeeds, {@code false} otherwise
     */
    public static boolean setWiFiApState(Context context, boolean enabled) {

        /**
         * Method overview
         * 1. Get WifiManager
         * 2. Get Wifi AP state
         * 3. Compare enabled argument with AP state.
         *      - return if states match. No need to enable Wifi AP if it's already running, could cause interruption?
         * 4. If enabling AP, then first disable WiFi radio using WifiManager
         * 5. Get the current Wifi AP configuration
         * 6. Get a handle to setWifiApEnabled
         * 6. Pass enabled and Wifi AP configuration as argument values to setWifiApEnabled for WifiManager
         */
        boolean result = false;

        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            // Wifi Enabled doesn't work on normal apps
            //boolean wifienabled= wifiManager.setWifiEnabled(true);
            // Log.d("WifiEnabled","setWiFiState: "+wifienabled);
            // using reflection to get method access for getWifiApConfiguration and setWifiApEnabled
            Method getWifiApMethod = wifiManager.getClass().getDeclaredMethod("getWifiApConfiguration");
            getWifiApMethod.setAccessible(true);
            WifiConfiguration wifiApConfig = (WifiConfiguration) getWifiApMethod.invoke(wifiManager);
            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            // pass in the ObjectReceiver, WifiConfiguration, boolean
            result = (boolean) setWifiApMethod.invoke(wifiManager, wifiApConfig, enabled);
            Log.d(TAG, (enabled ? "Enabling" : "Disabling") + " Wifi AP state, result = " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
