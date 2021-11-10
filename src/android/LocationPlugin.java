package com.gperaltap.locationplugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.content.Intent;
import android.content.IntentSender;

import android.widget.Toast;

import android.Manifest;

import android.app.Activity;

/**
 * This class echoes a string called from JavaScript.
 */
public class LocationPlugin extends CordovaPlugin {

    private LocationRequest locationRequest;
    public static final int REQUEST_CHECK_SETTING = 1001; //Request code for GPS Dialog
    private CallbackContext newCallbackContext = null;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        newCallbackContext = callbackContext;
        if (action.equals("enableGPS")) {
            cordova.setActivityResultCallback (this); //neccesary to call onActivityResult
            this.enableGPS();
            return true;
        }
        return false;
    }

    private void enableGPS() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(cordova.getActivity().getApplicationContext()).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete( Task<LocationSettingsResponse> task) {
                try {
                    //When GPS is Active run this code
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(cordova.getActivity(), "GPS in ON", Toast.LENGTH_SHORT).show();
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,"1");
                    newCallbackContext.sendPluginResult(pluginResult);
                }catch (ApiException e){
                    switch (e.getStatusCode()){
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException)e;
                                resolvableApiException.startResolutionForResult(cordova.getActivity(),REQUEST_CHECK_SETTING);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                        break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check Answer Google GPS Dialog
        PluginResult pluginResult;
        if (requestCode == REQUEST_CHECK_SETTING){
            switch (resultCode){

                case Activity.RESULT_OK:
                    Toast.makeText(cordova.getActivity(),"GPS is Turned on", Toast.LENGTH_SHORT).show();
                    pluginResult = new PluginResult(PluginResult.Status.OK,"2");
                    newCallbackContext.sendPluginResult(pluginResult);
                break;

                case Activity.RESULT_CANCELED:
                    Toast.makeText(cordova.getActivity(), "GPS is required to be turned", Toast.LENGTH_SHORT).show();
                    pluginResult = new PluginResult(PluginResult.Status.ERROR,"0");
                    newCallbackContext.sendPluginResult(pluginResult);
                break;
            }
        }
    }

}
