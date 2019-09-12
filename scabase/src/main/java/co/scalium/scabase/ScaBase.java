package co.scalium.scabase;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.eukaprotech.networking.AsyncConnection;
import com.eukaprotech.networking.AsyncConnectionHandler;
import com.eukaprotech.networking.Parameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ScaBase {
    private AsyncConnection asyncConnection;
    private Context appContext;
    private final String anaEndPoint = "https://api.scabase.info/";
    private FilePG filePG;
    private boolean isInstall = false;

    private ScaBase(Context context) {
        this.asyncConnection = new AsyncConnection();
        this.appContext = context;
        this.filePG = new FilePG("Scabase",context);

    }
    public static ScaBase init(Context context){
        return new ScaBase(context);
    }

    public void setEvent(String eventName) {
        if (eventName.toLowerCase().equals("install") || eventName.toLowerCase().equals("appopen")) {
            Log.i("ScaBase", "SDK Will Automatically record install and App open Events!");
            return;
        }

        if(eventName.equals("APP_OPENER_FROM_LIB")){
            if(!filePG.fileGetBoolean("InstallEventHandler")){
                eventName = "Install";
                isInstall = true;
            }else{
                eventName = "AppOpen";
            }
        }
        String deviceMan = Build.MANUFACTURER;
        String deviceModel = Build.MODEL;

        String versionName = "ERR";
        int versionCode = 0;

        PackageInfo pInfo = null;
        try {
            pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        versionName = pInfo.versionName;
        versionCode = pInfo.versionCode;

        String androidVersion = Build.VERSION.RELEASE;
        String packageName = appContext.getPackageName();
        ApplicationInfo app = null;
        try {
            app = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Bundle bundle = app.metaData;

        String operator = getCarrier();
        if(operator.contains("irancell") || operator.contains("mtn")){
            operator = "Irancell";
        } else if(operator.contains("mci") || operator.contains("tci")){
            operator = "HamrahAval";
        } else if(operator.contains("rightel")){
            operator = "Rightel";
        }

        @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(appContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        int applicationId = 0;
        applicationId = bundle.getInt("co.scalium.scabase.APP_ID");
        if(applicationId != 0){

            Parameters data = new Parameters();
            data.put("APP_ID",applicationId);
            data.put("VERSION_NAME",versionName);
            data.put("VERSION_CODE",versionCode);
            data.put("DEVICE_MANUFACTURER",deviceMan);
            data.put("DEVICE_MODEL",deviceModel);
            data.put("OPERATOR_NAME",operator);
            data.put("DEVICE_UNIQUE_ID",deviceId);
            data.put("EVENT_NAME",eventName);
            data.put("ANDROID_VERSION",androidVersion);
            data.put("PACKAGE_NAME",packageName);

            asyncConnection.post(anaEndPoint, data, new AsyncConnectionHandler() {
                @Override
                public void onStart() {
                }

                @Override
                public void onSucceed(int responseCode, HashMap<String, String> headers, byte[] response) {

                    try {
                        JSONObject contents = new JSONObject(ParseResponse(response));
                        boolean result = contents.getBoolean("result");

                        if(result){
                            if(isInstall){
                                filePG.fileSet("InstallEventHandler",true);
                                isInstall = false;
                            }
                            Log.i("ScaBase","Event Set Successfully!");
                        }else{
                            Log.e("ScaBase","ERROR : "+contents.getString("error"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFail(int responseCode, HashMap<String, String> headers, byte[] response, Exception error) {
                    Log.e("ScaBase","Connection Error - Can`t Set Event");
                }

                @Override
                public void onComplete() {

                }

            });


        }else{
            Log.e("ScaBase","APP_ID not set! Set it from AndroidManifest file - Can`t Set Event");
        }
    }


    private String getCarrier(){
        TelephonyManager manager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();
        return carrierName.toLowerCase();
    }

    private String ParseResponse(byte[] b)
    {
        StringBuilder resp = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            resp.append((char) b[i]);
        }

        return resp.toString();
    }

}
