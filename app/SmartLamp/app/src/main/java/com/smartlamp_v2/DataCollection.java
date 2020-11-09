package com.smartlamp_v2;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class DataCollection extends Service {

    final String LOG_TAG = "myLogs";

    public static String MQTTHOST = "a"; // tcp://postman.cloudmqtt.com:17110
    public static String USERNAME = "a"; // obtxfdbf
    public static String PASSWORD = "a"; // 6uD0FYaNQT5K
    MqttAndroidClient client;
    MqttConnectOptions options;

    final String Temp_sub = "Lamp/Temp";
    final String Hum_sub = "Lamp/Hum";
    final String Bar_sub = "Lamp/Bar";
    String temp, hum, bar;

    DBHelper dbHelper;
    Thread t;

    boolean on_off_ser;


    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");

        MQTTHOST = MainActivity.MQTTHOST;
        USERNAME = MainActivity.USERNAME;
        PASSWORD = MainActivity.PASSWORD;

        dbHelper = new DBHelper(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");

        t = new Thread(new Runnable() {
            public void run() {
                if (MQTTHOST.equals("") || PASSWORD.equals("") || USERNAME.equals("")) {
                } else {
                    MQTT_void();
                }
            }
        });
        t.start();


        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        dbHelper.close();
        on_off_ser = false;
        Log.d(LOG_TAG, "onDestroy");

        super.onDestroy();

    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }


    public void MQTT_void() {

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, clientId);

        options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());


        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(LOG_TAG, "We are connected");
                    on_off_ser=true;
                    setSubscription();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(LOG_TAG, "NOT connected");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                viewData(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }

    public void setSubscription() {
        try {
            client.subscribe(Temp_sub, 0);
            client.subscribe(Hum_sub, 0);
            client.subscribe(Bar_sub, 0);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void viewData(String topic, MqttMessage message) {
        if (on_off_ser) {

            boolean DBcheck = false;
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            if (topic.equals(Temp_sub)) {
                temp = new String(message.getPayload());
            }

            if (topic.equals(Hum_sub)) {
                hum = new String(message.getPayload());
            }

            if (topic.equals(Bar_sub)) {
                bar = new String(message.getPayload());
                DBcheck = true;
            }

            if (DBcheck) {
                contentValues.put(DBHelper.KEY_TEMP, temp);
                contentValues.put(DBHelper.KEY_HUM, hum);
                contentValues.put(DBHelper.KEY_BAR, bar);
                database.insert(DBHelper.TABLE_DATA_SEN, null, contentValues);

                Cursor cursor = database.query(DBHelper.TABLE_DATA_SEN, null, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
                    int tempIndex = cursor.getColumnIndex(DBHelper.KEY_TEMP);
                    int humIndex = cursor.getColumnIndex(DBHelper.KEY_HUM);
                    int barIndex = cursor.getColumnIndex(DBHelper.KEY_BAR);
                    do {
                        Log.d(LOG_TAG, "ID = " + cursor.getInt(idIndex) +
                                ", temp = " + cursor.getInt(tempIndex) +
                                ", hum = " + cursor.getInt(humIndex) +
                                ", bar = " + cursor.getInt(barIndex));
                    } while (cursor.moveToNext());
                } else
                    Log.d(LOG_TAG, "0 rows");

                cursor.close();
            }
        }
    }

}