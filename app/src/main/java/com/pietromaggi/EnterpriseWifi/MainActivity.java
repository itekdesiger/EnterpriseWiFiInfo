package com.pietromaggi.EnterpriseWifi;

import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class MainActivity extends Activity {
    private static final String INT_IDENTITY = "identity";
    final String INT_ENTERPRISEFIELD_NAME = "android.net.wifi.WifiConfiguration$EnterpriseField";

    private EditText mSSIDNameEditText;
    private TextView mEAPIdentity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSSIDNameEditText = (EditText)findViewById(R.id.SSID_name);
        mEAPIdentity = (TextView)findViewById(R.id.EAP_Identity);

        Button mReadEAPIdentityButton = (Button)findViewById(R.id.get_eap_identity_button);
        mReadEAPIdentityButton.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View v) {
                // Get EAP Identity for current SSID
                String SSIDName = mSSIDNameEditText.getText().toString();

                if (SSIDName == null || SSIDName.isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.SetSSIDName, Toast.LENGTH_SHORT).show();
                    return;
                }
                SSIDName = "\"" + SSIDName + "\"";

                String EAPIdentity = get_EAP_identity(SSIDName);

                if (EAPIdentity == null || EAPIdentity.isEmpty()) {
                    EAPIdentity = getString(R.string.NoEAPIdentity);
                    Toast.makeText(MainActivity.this, R.string.NoEAPIdentity, Toast.LENGTH_SHORT).show();
                } else {
                    mEAPIdentity.setText(EAPIdentity);
                }
            }
        });
   }


    private String get_EAP_identity(String intSsidName) {
        String result = null;

        /*Get the WifiService */
        WifiManager wifi = (WifiManager)getSystemService(WIFI_SERVICE);
        /*Get All WIfi configurations*/
        List<WifiConfiguration> configList = wifi.getConfiguredNetworks();
        /*Now we need to search appropriate configuration i.e. with name SSID_Name*/
        for(int i = 0;i<configList.size();i++)
        {
            if(configList.get(i).SSID.contentEquals(intSsidName))
            {
                /*We found the appropriate config now read all config details*/
                Iterator<WifiConfiguration> iter =  configList.iterator();
                WifiConfiguration config = configList.get(i);


                /*reflection magic*/
                /*These are the fields we are really interested in*/
                try
                {
                    // Let the magic start
                    Class[] wcClasses = WifiConfiguration.class.getClasses();
                    // null for overzealous java compiler
                    Class wcEnterpriseField = null;

                    for (Class wcClass : wcClasses)
                        if (wcClass.getName().equals(INT_ENTERPRISEFIELD_NAME))
                        {
                            wcEnterpriseField = wcClass;
                            break;
                        }
                    boolean noEnterpriseFieldType = false;
                    if(wcEnterpriseField == null)
                        noEnterpriseFieldType = true; // Cupcake/Donut access enterprise settings directly

                    Field wcefIdentity = null;
                    Field[] wcefFields = WifiConfiguration.class.getFields();
                    // Dispatching Field vars
                    for (Field wcefField : wcefFields)
                    {
                        if (wcefField.getName().trim().equals(INT_IDENTITY))
                            wcefIdentity = wcefField;
                    }
                    Method wcefValue = null;
                    if(!noEnterpriseFieldType)
                    {
                        for(Method m: wcEnterpriseField.getMethods())
                            //System.out.println(m.getName());
                            if(m.getName().trim().equals("value")){
                                wcefValue = m;
                                break;
                            }
                    }

                /*EAP Method*/
                    Object obj = null;
                /*Identity*/
                    if(!noEnterpriseFieldType)
                    {
                        result = (String) wcefValue.invoke(wcefIdentity.get(config), null);
                        Log.d("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "[EAP IDENTITY]" + result);
                    }
                    else
                    {
                        result = (String)wcefIdentity.get(config);
                    }

                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

            }
        }

        return result;
    }
}
