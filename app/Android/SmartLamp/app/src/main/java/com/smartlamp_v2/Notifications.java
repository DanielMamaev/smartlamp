package com.smartlamp_v2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static com.github.mikephil.charting.charts.Chart.LOG_TAG;

public class Notifications extends Service {
    Notification notification;
    NotificationManager notificationManager;

    public static String MQTTHOST = "a"; // tcp://postman.cloudmqtt.com:17110
    public static String USERNAME = "a"; // obtxfdbf
    public static String PASSWORD = "a"; // 6uD0FYaNQT5K
    MqttAndroidClient client;
    MqttConnectOptions options;

    String Mov_sub = "Lamp/Mov";
    String Light_sub = "Lamp/Light";
    String VU_sub = "Lamp/VU";

    boolean vu_check=true;
    boolean light_check=true;
    boolean mov_check=true;

    public final static String FILE_NAME = "filename";

    public void onCreate() {
        MQTTHOST = MainActivity.MQTTHOST;
        USERNAME = MainActivity.USERNAME;
        PASSWORD = MainActivity.PASSWORD;
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {


        Thread t = new Thread(new Runnable() {
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


        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
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
            client.subscribe(Mov_sub, 0);
            client.subscribe(Light_sub, 0);
            client.subscribe(VU_sub, 0);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void viewData(String topic, MqttMessage message) {




        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (topic.equals(VU_sub)) {
            String vu = new String(message.getPayload());
            if (vu.equals("1")){
                if (vu_check) {
                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.drawable.ic_microphone)
                                    .setContentTitle("Звук")
                                    .setContentText("Завиксирован какой то звук!")
                                    .setContentIntent(resultPendingIntent)
                                    .setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_ALL);
                    notification = builder.build();
                    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(1, notification);
                    vu_check=false;
                }
            }
        }

        if (topic.equals(Light_sub)) {
            String light = new String(message.getPayload());
            if (light.equals("1")) {
                if (light_check) {
                    NotificationCompat.Builder builder2 =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.drawable.ic_sun)
                                    .setContentTitle("Свет")
                                    .setContentText("Зафиксирован свет!")
                                    .setContentIntent(resultPendingIntent)
                                    .setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_ALL);
                    notification = builder2.build();
                    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(2, notification);
                    light_check=false;
                }
            }
        }

        if (topic.equals(Mov_sub)) {
            String mov = new String(message.getPayload());
            if (mov.equals("1")) {
                if (mov_check) {
                    NotificationCompat.Builder builder3 =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.drawable.ic_running)
                                    .setContentTitle("Движение")
                                    .setContentText("Зафиксированно движение!")
                                    .setContentIntent(resultPendingIntent)
                                    .setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_ALL);
                    notification = builder3.build();
                    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(3, notification);
                    mov_check=false;
                }
            }
        }
    }
}