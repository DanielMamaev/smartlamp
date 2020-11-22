package com.smartlamp_v2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import yuku.ambilwarna.AmbilWarnaDialog;


public class MainActivity extends AppCompatActivity {

    private static final int REQ_ENABLE_BLUETOOTH = 1001;
    DBHelper dbHelper;

    SharedPreferences sPef;
    BottomNavigationView bottomNav;
    ActionBar actionBar;
    boolean color_spinner;
    final String LOG_TAG = "Mainn";

    public static String MQTTHOST = "a"; // tcp://postman.cloudmqtt.com:17110
    public static String USERNAME = "a"; // obtxfdbf
    public static String PASSWORD = "a"; // 6uD0FYaNQT5K
    MqttAndroidClient client;
    MqttConnectOptions options;
    boolean mqtt_connected = false;
    boolean send_mqtt_check = false;
    String mqtt_ed = "";
    String port_ed = "";

    //----Компоненты формы
    ScrollView lt_home;
    LinearLayout lt_graphs;
    ScrollView lt_control;
    ScrollView lt_settings;
    RelativeLayout main_layout;

    //ДОМ
    TextView temp_view;
    TextView hum_view;
    TextView dis_view;
    TextView bar_view;
    TextView text_sound;
    TextView text_light;
    TextView text_mov;
    LinearLayout sen_layout;
    LinearLayout graph_layout;
    LinearLayout spinner_layout;
    ArrayAdapter<String> adapter_ru;
    ArrayAdapter<String> adapter_en;
    ImageView image_sound;
    ImageView image_light;
    ImageView image_mov;
    ImageView imageView5;
    LinearLayout sign_text_layout;
    TextView text_sensors;
    TextView text_sing;
    TextView text_weather;
    LinearLayout weather_layout;
    TextView text_weather_prog;
    TextView text_prognosis;
    TextView text_chance;

    //ГРАФИКИ
    Spinner spinner_graph;

    LineChart chart_temp;
    LineChart chart_hum;
    LineChart chart_bar;
    LineChart chart_dis;

    LineData temp_data;
    LineData hum_data;
    LineData bar_data;
    LineData dis_data;

    LineDataSet temp_set;
    LineDataSet hum_set;
    LineDataSet bar_set;
    LineDataSet dis_set;
    int x;
    int x_d;
    int y_t, y_h, y_b, y_d;
    boolean draw_point = false;


    //УПРАВЛЕНИЕ
    ImageButton up_btn;
    ImageButton down_btn;
    ImageButton left_btn;
    ImageButton right_btn;
    ImageButton on_off_btn;
    ImageButton AP_btn;
    ImageButton indi_btn;
    ImageButton LP_btn;
    ImageButton sign_btn;
    ImageButton color_btn;
    SeekBar seekBar;
    Switch scroll_switch;

    //НАСТРОЙКИ
    Switch DT_switch;
    Switch BG_switch;
    EditText eT_MQTTHOST;
    EditText eT_PORT;
    EditText eT_USERNAME;
    EditText eT_PASSWORD;
    ImageButton save_btn;
    ImageButton clear_btn;
    TextView textView1;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    TextView textView_13;
    Spinner spinner_language;
    ArrayAdapter<String> adapter_lan;
    LinearLayout mqtt_layout;
    LinearLayout DT_layout;
    LinearLayout lan_layout;
    LinearLayout BG_layout;

    //----Переменные
    String move_pub = "move";
    String brig_pub = "brig";
    String color_pub = "color";

    String Hum_sub = "Hum";
    String Temp_sub = "Temp";
    String Dis_sub = "Dis";
    String Bar_sub = "Bar";
    String Sign_sub = "Sign";
    String Mov_sub = "Mov";
    String Light_sub = "Light";
    String VU_sub = "VU";
    String Chance_sub = "Chance";

    String temp = "";
    String hum = "";
    String dis = "";
    String bar = "";
    String sign = "";
    String chance = "";

    int currentColor;
    String send_color = "";

    String[] graph_ru = {"Температура", "Влажность", "Давление", "Расстояние"};
    String[] graph_en = {"Temperature", "Humidity", "Atmospheric pressure", "Distance"};
    String[] lan = {"Русский", "English"};

    String[] chance_mas = {" Без изменений", " Возможно ухудшение", " Возможно улучшение",
            " Будет заметное ухудшение", " Будет заметное улучшение"};
    String[] chance_en_mas = {" Unchanged", " Possible deterioration", " Possible to improve",
            "There will be a noticeable deterioration", "There will be a noticeable improvement"};


    byte sign_on_off;

    public final String TAG = getClass().getSimpleName();
    BluetoothAdapter mBluetoothAdapter;
    private ProgressDialog mProgressDialog;
    private ArrayList<BluetoothDevice> mDevices = new ArrayList<>();
    private ListView listDevices;
    private DeviceListAdapter mDeviceListAdapter;
    //private BluetoothSocket mBluetoothSocket;
    //private OutputStream mOutputStream;

    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    int[] intData = new int[9];     // массив численных значений после парсинга
    boolean isConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            Toast.makeText(this, "Bluetooth не поддерживается", Toast.LENGTH_LONG).show();
        mDeviceListAdapter = new DeviceListAdapter(this, R.layout.device_item, mDevices);


        dbHelper = new DBHelper(this);

        main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        lt_home = (ScrollView) findViewById(R.id.home_layout);
        lt_graphs = (LinearLayout) findViewById(R.id.graphs_layout);
        lt_control = (ScrollView) findViewById(R.id.control_layout);
        lt_settings = (ScrollView) findViewById(R.id.settings_layout);

        actionBar = getSupportActionBar();
        //actionBar.setIcon(getResources().getDrawable(R.drawable.ic_barometer));
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        sPef = getPreferences(MODE_PRIVATE);


        //--------НАХОЖДЕНИЕ ЭЛЕМЕНТОВ

        //ДОМ
        sen_layout = (LinearLayout) findViewById(R.id.sen_layout);
        temp_view = (TextView) findViewById(R.id.temp_view);
        hum_view = (TextView) findViewById(R.id.hum_view);
        dis_view = (TextView) findViewById(R.id.dis_view);
        bar_view = (TextView) findViewById(R.id.bar_view);
        temp_view.setTextColor(Color.BLACK);
        hum_view.setTextColor(Color.BLACK);
        bar_view.setTextColor(Color.BLACK);
        dis_view.setTextColor(Color.BLACK);
        image_sound = (ImageView) findViewById(R.id.image_sound);
        image_light = (ImageView) findViewById(R.id.image_light);
        image_mov = (ImageView) findViewById(R.id.image_mov);
        imageView5 = (ImageView) findViewById(R.id.imageView5);
        text_sound = (TextView) findViewById(R.id.text_sound);
        text_light = (TextView) findViewById(R.id.text_light);
        text_mov = (TextView) findViewById(R.id.text_mov);
        sign_text_layout = (LinearLayout) findViewById(R.id.sign_text_layout);
        weather_layout = (LinearLayout) findViewById(R.id.weather_layout);
        text_sensors = (TextView) findViewById(R.id.text_sensors);
        text_sing = (TextView) findViewById(R.id.text_sign);
        text_weather = (TextView) findViewById(R.id.text_weather);
        text_weather.setTextColor(Color.GRAY);
        text_sensors.setTextColor(Color.GRAY);
        text_sing.setTextColor(Color.GRAY);
        text_weather_prog = (TextView) findViewById(R.id.text_weather_prog);
        text_prognosis = (TextView) findViewById(R.id.text_prognosis);
        text_weather_prog.setTextColor(Color.BLACK);
        text_prognosis.setTextColor(Color.BLACK);
        text_chance = (TextView) findViewById(R.id.text_chance);
        text_chance.setTextColor(Color.BLACK);


        //ГРАФИК
        spinner_graph = (Spinner) findViewById(R.id.spinner_graph);
        adapter_ru = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, graph_ru);
        adapter_ru.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_en = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, graph_en);
        adapter_en.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        settings_graph();

        graph_layout = (LinearLayout) findViewById(R.id.graph_layout);

        //УПРАВЛЕНИЕ
        up_btn = findViewById(R.id.up_btn);
        down_btn = findViewById(R.id.down_btn);
        left_btn = findViewById(R.id.left_btn);
        right_btn = findViewById(R.id.right_btn);
        on_off_btn = findViewById(R.id.on_off_btn);
        AP_btn = findViewById(R.id.AP_btn);
        indi_btn = findViewById(R.id.indi_btn);
        LP_btn = findViewById(R.id.LP_btn);
        sign_btn = findViewById(R.id.sign_btn);
        color_btn = findViewById(R.id.color_btn);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);


        //НАСТРОЙКИ
        mqtt_layout = findViewById(R.id.mqtt_layout);
        DT_layout = findViewById(R.id.DT_layout);
        lan_layout = findViewById(R.id.lan_layout);
        BG_layout = findViewById(R.id.BG_layout);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView_13 = (TextView) findViewById(R.id.textView13);
        textView1.setTextColor(Color.GRAY);
        textView2.setTextColor(Color.GRAY);
        textView3.setTextColor(Color.GRAY);
        textView4.setTextColor(Color.GRAY);
        textView_13.setTextColor(Color.GRAY);
        eT_MQTTHOST = findViewById(R.id.editText2);
        eT_PORT = findViewById(R.id.editText3);
        eT_USERNAME = findViewById(R.id.editText4);
        eT_PASSWORD = findViewById(R.id.editText5);
        save_btn = findViewById(R.id.save_btn);
        clear_btn = findViewById(R.id.clear_btn);

        BG_switch = findViewById(R.id.switch_BG);
        if (sPef.getString("check_BG", "").equals("BG_on")) {
            BG_switch.setChecked(true);
        }
        if (sPef.getString("check_BG", "").equals("BG_off")) {
            BG_switch.setChecked(false);
        }

        DT_switch = findViewById(R.id.switch_DT);
        if (sPef.getString("check_DT", "").equals("DT_on")) {
            color_spinner = true;
            DT_switch.setChecked(true);
            setColor(true);
        }
        if (sPef.getString("check_DT", "").equals("DT_off")) {
            DT_switch.setChecked(false);
            color_spinner = false;
            setColor(false);
        }

        spinner_language = (Spinner) findViewById(R.id.spinner_lan);
        adapter_lan = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lan);
        adapter_lan.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_language.setAdapter(adapter_lan);

        spinners();
        if (sPef.getString("select_item", "").equals("ru")) {
            spinner_language.setSelection(0, true);
        }
        if (sPef.getString("select_item", "").equals("en")) {
            spinner_language.setSelection(1, true);
        }


        save_clear();
        control_btn();


        MQTTHOST = sPef.getString("mqqt_prom", "");
        USERNAME = sPef.getString("username_prom", "");
        PASSWORD = sPef.getString("password_prom", "");
        loadText();


        switch_void();


        //getData();

        /*if (MQTTHOST.equals("") || PASSWORD.equals("") || USERNAME.equals("")) {
        } else MQTT_void();*/
    }


    //-------------------------- MENU WIFI AND BLUETOOTH ------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        String item_title = (String) item.getTitle();
        if (item_title.equals("WIFI")) {
            if (isConnected) connectedThread.write("102");
            Toast.makeText(this, "Переключение на WiFi", Toast.LENGTH_SHORT).show();

            if (connectThread != null) {
                connectThread.cancel();
            }
            if (connectedThread != null) {
                connectedThread.cancel();
            }

        }

        if (item_title.equals("BT")) {
            Toast.makeText(this, "Переключение на Bluetooth", Toast.LENGTH_SHORT).show();
            enableBluetooth();
            searchDevices();
        }

        if (item_title.equals("REC")) {
            pub(move_pub, "0");
            Toast.makeText(this, "Смена точки доступа", Toast.LENGTH_SHORT).show();
        }

        //
        return super.onOptionsItemSelected(item);
    }


    //-------------------------- BLUETOOTH ------------------------------------

    void send_mqtt() {
        if (mqtt_connected) {
            Log.d(LOG_TAG, "mqtt ok");
            if (isConnected) {
                Log.d(LOG_TAG, "bt ok");
                connectedThread.write("m" + mqtt_ed + ";");
                connectedThread.write(port_ed + ";");
                connectedThread.write(USERNAME + ";");
                connectedThread.write(PASSWORD);

                send_mqtt_check = false;

                if (connectThread != null) {
                    connectThread.cancel();
                }
                if (connectedThread != null) {
                    connectedThread.cancel();
                }
            }
        }
    }

    public void enableBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQ_ENABLE_BLUETOOTH);
        }
    }

    private BroadcastReceiver mRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                //Toast.makeText(MainActivity.this, "Начало поиска", Toast.LENGTH_SHORT).show();
                mProgressDialog = ProgressDialog.show(
                        MainActivity.this, "Поиск устройств", " Пожалуйста подождите...");
            }
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                //Toast.makeText(MainActivity.this, "Поиск закончен", Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
                showListDevices();
            }
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                //Toast.makeText(MainActivity.this, "Что-то нашел", Toast.LENGTH_SHORT).show();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    if (!mDevices.contains(device))
                        mDeviceListAdapter.add(device);
                }
            }
        }
    };

    public void searchDevices() {

        checkPermissionLocations();

        if (!mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.startDiscovery();
        }

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter.startDiscovery();
        }
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mRecevier, filter);
    }

    private void checkPermissionLocations() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int check = checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            check += checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

            if (check != 0) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1002);
            }
        }
    }

    private void showListDevices() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Найденые устройства");

        View view = getLayoutInflater().inflate(R.layout.list_devices_view, null);
        listDevices = view.findViewById(R.id.list_devices);
        listDevices.setAdapter(mDeviceListAdapter);
        listDevices.setOnItemClickListener(ItemOnClickListener);

        builder.setView(view);
        builder.setNegativeButton("OK", null);
        builder.create();
        builder.show();
    }

    private AdapterView.OnItemClickListener ItemOnClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            BluetoothDevice device = mDevices.get(position);
            if (device != null) {
                connectThread = new ConnectThread(device);
                connectThread.start();
            }
        }
    };

    public class ConnectThread extends Thread {
        private BluetoothSocket bluetoothSocket = null;
        private boolean success = false;

        public ConnectThread(BluetoothDevice device) {
            try {
                Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                bluetoothSocket = (BluetoothSocket) method.invoke(device, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                bluetoothSocket.connect();
                success = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Подключились!", Toast.LENGTH_SHORT).show();
                        if (isConnected) connectedThread.write("101");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Ошибка подключения!", Toast.LENGTH_SHORT).show();
                    }
                });
                cancel();
            }
            if (success) {
                connectedThread = new ConnectedThread(bluetoothSocket);
                connectedThread.start();
            }
        }


        public void cancel() {
            try {
                bluetoothSocket.close();
                success = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream inputStream;
        private final OutputStream outputStream;


        boolean recievedFlag;
        boolean getStarted;
        byte indexx;
        String string_convert = "";

        public ConnectedThread(BluetoothSocket bluetoothSocket) {
            InputStream inputStream = null;
            OutputStream outputStream = null;


            try {
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.inputStream = inputStream;
            this.outputStream = outputStream;
            isConnected = true;
        }

        @Override
        public void run() {
            BufferedInputStream bis = new BufferedInputStream(inputStream);

            while (isConnected) {
                try {
                    char incomingByte = (char) bis.read();
                    if (getStarted) {                         // если приняли начальный символ (парсинг разрешён)
                        if (incomingByte != ' ' && incomingByte != ';') {   // если это не пробел И не конец
                            string_convert += incomingByte;       // складываем в строку
                        } else {                                // если это пробел или ; конец пакета
                            intData[indexx] = Integer.parseInt(string_convert);  // преобразуем строку в int и кладём в массив
                            string_convert = "";                  // очищаем строку
                            indexx++;                              // переходим к парсингу следующего элемента массива
                        }
                    }
                    if (incomingByte == '$') {                // если это $
                        getStarted = true;                      // поднимаем флаг, что можно парсить
                        indexx = 0;                              // сбрасываем индекс
                        string_convert = "";                    // очищаем строку
                    }
                    if (incomingByte == ';') {                // если таки приняли ; - конец парсинга
                        getStarted = false;                     // сброс
                        recievedFlag = true;                    // флаг на принятие
                    }

                    if (recievedFlag) {
                        recievedFlag = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(MainActivity.this, String.valueOf(intData[0]), Toast.LENGTH_SHORT).show();
                                bluetooth_data();
                                x++;
                            }
                        });
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void write(String command) {
            byte[] buffer = command.getBytes();

            if (outputStream != null) {
                try {
                    outputStream.write(buffer);
                    outputStream.flush();


                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Команда не отослана!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        }

        public void cancel() {
            try {
                isConnected = false;
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void bluetooth_data() {
        y_t = intData[0];
        y_h = intData[1];
        y_b = intData[2];
        y_d = intData[3];
        int chance_int = intData[4];
        int sign_int = intData[5];
        int vu_int = intData[6];
        int light_int = intData[7];
        int mov_int = intData[8];

        String[] chacne_lan = new String[5];
        if (sPef.getString("select_item", "").equals("ru")) {
            for (int i = 0; i < 5; i++) {
                chacne_lan[i] = chance_mas[i];
            }
        }
        if (sPef.getString("select_item", "").equals("en")) {
            for (int i = 0; i < 5; i++) {
                chacne_lan[i] = chance_en_mas[i];
            }
        }
        if ((-20 <= chance_int) && (chance_int <= 20)) {
            text_prognosis.setText(chacne_lan[0]);
        }
        if ((chance_int >= -59) && (chance_int <= -21)) {
            text_prognosis.setText(chacne_lan[1]);
        }
        if ((chance_int <= 59) && (chance_int >= 21)) {
            text_prognosis.setText(chacne_lan[2]);
        }
        if (chance_int <= -60) {
            text_prognosis.setText(chacne_lan[3]);
        }
        if (chance_int >= 60) {
            text_prognosis.setText(chacne_lan[4]);
        }

        temp_view.setText(y_t + " °C");
        hum_view.setText(y_h + " %");
        dis_view.setText(y_d + " cm");
        bar_view.setText(y_b + " мм.рт.ст");
        text_chance.setText(chance_int + " %");

        temp_data.addEntry(new Entry(x, y_t), 0);
        temp_data.notifyDataChanged();
        chart_temp.notifyDataSetChanged();
        chart_temp.moveViewToX(temp_data.getEntryCount());

        hum_data.addEntry(new Entry(x, y_h), 0);
        hum_data.notifyDataChanged();
        chart_hum.notifyDataSetChanged();
        chart_hum.moveViewToX(hum_data.getEntryCount());

        bar_data.addEntry(new Entry(x, y_b), 0);
        bar_data.notifyDataChanged();
        chart_bar.notifyDataSetChanged();
        chart_bar.moveViewToX(bar_data.getEntryCount());

        dis_data.addEntry(new Entry(x, y_d), 0);
        dis_data.notifyDataChanged();
        chart_dis.notifyDataSetChanged();
        chart_dis.moveViewToX(dis_data.getEntryCount());


        if (sign_int == 8) {
            sign_on_off = 9;
            sign_btn.setImageResource(R.drawable.ic_eye);
            imageView5.setImageResource(R.drawable.ic_check);
        }
        if (sign_int == 9) {
            sign_on_off = 8;
            sign_btn.setImageResource(R.drawable.ic_eye_off);
            imageView5.setImageResource(R.drawable.ic_no_check);
        }

        if (vu_int == 1) image_sound.setImageResource(R.drawable.ic_warning);
        if (vu_int == 0) image_sound.setImageResource(R.drawable.ic_microphone);

        if (light_int == 1) image_light.setImageResource(R.drawable.ic_sun);
        if (light_int == 0) image_light.setImageResource(R.drawable.ic_sun_off);

        if (mov_int == 1) image_mov.setImageResource(R.drawable.ic_warning);
        if (mov_int == 0) image_mov.setImageResource(R.drawable.ic_running);
    }

    //----------------------------BOTTOM NAVIGATION VIEW--------------------------------------------
    public BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.home_page:
                    lt_home.setVisibility(View.VISIBLE);
                    lt_graphs.setVisibility(View.INVISIBLE);
                    lt_control.setVisibility(View.INVISIBLE);
                    lt_settings.setVisibility(View.INVISIBLE);
                    break;
                case R.id.graph_page:
                    lt_home.setVisibility(View.INVISIBLE);
                    lt_graphs.setVisibility(View.VISIBLE);
                    lt_control.setVisibility(View.INVISIBLE);
                    lt_settings.setVisibility(View.INVISIBLE);
                    break;
                case R.id.control_page:
                    lt_home.setVisibility(View.INVISIBLE);
                    lt_graphs.setVisibility(View.INVISIBLE);
                    lt_control.setVisibility(View.VISIBLE);
                    lt_settings.setVisibility(View.INVISIBLE);
                    break;
                case R.id.settings_page:
                    lt_home.setVisibility(View.INVISIBLE);
                    lt_graphs.setVisibility(View.INVISIBLE);
                    lt_control.setVisibility(View.INVISIBLE);
                    lt_settings.setVisibility(View.VISIBLE);
                    break;
            }
            return true;
        }
    };


    //----------------------------MQTT--------------------------------------------------------------
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
                    Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_LONG).show();
                    Log.d(LOG_TAG, "Connected!");
                    mqtt_connected = true;
                    if (send_mqtt_check) send_mqtt();
                    setSubscription();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "No connected!", Toast.LENGTH_LONG).show();
                    mqtt_connected = false;
                    if (send_mqtt_check) send_mqtt();
                    Log.d(LOG_TAG, "No Connected!");
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
            client.subscribe(Dis_sub, 0);
            client.subscribe(Bar_sub, 0);
            client.subscribe(Sign_sub, 0);
            client.subscribe(Mov_sub, 0);
            client.subscribe(Light_sub, 0);
            client.subscribe(VU_sub, 0);
            client.subscribe(Chance_sub, 0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void viewData(String topic, MqttMessage message) {


        if (topic.equals(Temp_sub)) {
            temp = new String(message.getPayload());
            y_t = Integer.parseInt(temp);
        }
        if (topic.equals(Hum_sub)) {
            hum = new String(message.getPayload());
            y_h = Integer.parseInt(hum);
        }

        if (topic.equals(Bar_sub)) {
            bar = new String(message.getPayload());
            y_b = Integer.parseInt(bar);
            draw_point = true;
        }
        if
        (topic.equals(Dis_sub)) {
            dis = new String(message.getPayload());
            y_d = Integer.parseInt(dis);

            dis_data.addEntry(new Entry(x_d, y_d), 0);
            dis_data.notifyDataChanged();
            chart_dis.notifyDataSetChanged();
            chart_dis.moveViewToX(dis_data.getEntryCount());

            x_d++;
        }

        if (draw_point) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    temp_data.addEntry(new Entry(x, y_t), 0);
                    temp_data.notifyDataChanged();
                    chart_temp.notifyDataSetChanged();
                    chart_temp.moveViewToX(temp_data.getEntryCount());

                    hum_data.addEntry(new Entry(x, y_h), 0);
                    hum_data.notifyDataChanged();
                    chart_hum.notifyDataSetChanged();
                    chart_hum.moveViewToX(hum_data.getEntryCount());

                    bar_data.addEntry(new Entry(x, y_b), 0);
                    bar_data.notifyDataChanged();
                    chart_bar.notifyDataSetChanged();
                    chart_bar.moveViewToX(bar_data.getEntryCount());

                    draw_point = false;
                    x++;
                }
            });
            t.start();
        }

        if (topic.equals(VU_sub)) {
            String vu = new String(message.getPayload());
            if (vu.equals("1")) image_sound.setImageResource(R.drawable.ic_warning);
            if (vu.equals("0")) image_sound.setImageResource(R.drawable.ic_microphone);
        }

        if (topic.equals(Light_sub)) {
            String light = new String(message.getPayload());
            if (light.equals("1")) image_light.setImageResource(R.drawable.ic_sun);
            if (light.equals("0")) image_light.setImageResource(R.drawable.ic_sun_off);
        }

        if (topic.equals(Mov_sub)) {
            String mov = new String(message.getPayload());
            if (mov.equals("1")) image_mov.setImageResource(R.drawable.ic_warning);
            if (mov.equals("0")) image_mov.setImageResource(R.drawable.ic_running);
        }

        if (topic.equals(Chance_sub)) {
            chance = new String(message.getPayload());
            int chance_int = Integer.parseInt(chance);
            String[] chacne_lan = new String[5];

            if (sPef.getString("select_item", "").equals("ru")) {
                for (int i = 0; i < 5; i++) {
                    chacne_lan[i] = chance_mas[i];
                }
            }
            if (sPef.getString("select_item", "").equals("en")) {
                for (int i = 0; i < 5; i++) {
                    chacne_lan[i] = chance_en_mas[i];
                }
            }


            if ((-20 <= chance_int) && (chance_int <= 20)) {
                text_prognosis.setText(chacne_lan[0]);
            }
            if ((chance_int >= -59) && (chance_int <= -21)) {
                text_prognosis.setText(chacne_lan[1]);
            }
            if ((chance_int <= 59) && (chance_int >= 21)) {
                text_prognosis.setText(chacne_lan[2]);
            }
            if (chance_int <= -60) {
                text_prognosis.setText(chacne_lan[3]);
            }
            if (chance_int >= 60) {
                text_prognosis.setText(chacne_lan[4]);
            }
        }

        temp_view.setText(temp + " °C");
        hum_view.setText(hum + " %");
        dis_view.setText(dis + " cm");
        bar_view.setText(bar + " мм.рт.ст");
        text_chance.setText(chance + " %");

        if (topic.equals(Sign_sub)) {
            sign = new String(message.getPayload());

            String sing_check = new String(message.getPayload());
            if (sing_check.equals("8")) {
                sign_btn.setImageResource(R.drawable.ic_eye);
                imageView5.setImageResource(R.drawable.ic_check);
            }
            if (sing_check.equals("9")) {
                sign_btn.setImageResource(R.drawable.ic_eye_off);
                imageView5.setImageResource(R.drawable.ic_no_check);
            }
        }
    }

    public void pub(String topic, String message) {
        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public SeekBar.OnSeekBarChangeListener seekBarChangeListener
            = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            String topic = brig_pub;
            String message = String.valueOf(seekBar.getProgress());
            pub(topic, message);

            if (isConnected) connectedThread.write("b" + message);
            //setMessage(message);
        }
    };


    //-----------------------------------BUTTON-----------------------------------------------------
    public void save_clear() {
        View.OnClickListener OnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sPef = getPreferences(MODE_PRIVATE);
                switch (v.getId()) {
                    case R.id.save_btn:
                        mqtt_ed = eT_MQTTHOST.getText().toString();
                        port_ed = eT_PORT.getText().toString();
                        USERNAME = eT_USERNAME.getText().toString();
                        PASSWORD = eT_PASSWORD.getText().toString();

                        MQTTHOST = "tcp://" + mqtt_ed + ":" + port_ed;
                        SharedPreferences.Editor ed = sPef.edit();
                        ed.putString("mqqt_prom", MQTTHOST);
                        ed.putString("mqqt_ed_prom", mqtt_ed);
                        ed.putString("port_prom", port_ed);
                        ed.putString("username_prom", USERNAME);
                        ed.putString("password_prom", PASSWORD);
                        ed.apply();
                        Toast.makeText(MainActivity.this, "Сохранил", Toast.LENGTH_LONG).show();
                        send_mqtt_check = true;

                        if (MQTTHOST.equals("") || PASSWORD.equals("") || USERNAME.equals("")) {
                        } else MQTT_void();


                        break;
                    case R.id.clear_btn:
                        eT_MQTTHOST.setText("");
                        eT_PORT.setText("");
                        eT_USERNAME.setText("");
                        eT_PASSWORD.setText("");

                        SharedPreferences.Editor edt = sPef.edit();
                        edt.putString("mqqt_ed_prom", eT_MQTTHOST.getText().toString());
                        edt.putString("port_prom", eT_PORT.getText().toString());
                        edt.putString("username_prom", eT_USERNAME.getText().toString());
                        edt.putString("password_prom", eT_PASSWORD.getText().toString());
                        edt.apply();

                        Toast.makeText(MainActivity.this, "Очистил", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
        save_btn.setOnClickListener(OnClickListener);
        clear_btn.setOnClickListener(OnClickListener);
    }

    public void control_btn() {
        View.OnClickListener ClickListener_control = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String topic = move_pub;
                String message = "";
                switch (view.getId()) {
                    case R.id.on_off_btn:
                        message = "1";
                        pub(topic, message);
                        break;
                    case R.id.right_btn:
                        message = "2";
                        pub(topic, message);
                        break;
                    case R.id.left_btn:
                        message = "3";
                        pub(topic, message);
                        break;
                    case R.id.AP_btn:
                        message = "4";
                        pub(topic, message);
                        break;
                    case R.id.up_btn:
                        message = "5";
                        pub(topic, message);
                        break;
                    case R.id.down_btn:
                        message = "6";
                        pub(topic, message);
                        break;
                    case R.id.indi_btn:
                        message = "7";
                        pub(topic, message);
                        break;
                    case R.id.sign_btn:
                        if (sign.equals("9")) {
                            message = "8";
                            pub(topic, message);
                        }

                        if (sign.equals("8")) {
                            message = "9";
                            pub(topic, message);
                        }

                        if (sign_on_off == 8) message = "8";
                        if (sign_on_off == 9) message = "9";
                        break;
                    case R.id.LP_btn:
                        message = "100";
                        pub(topic, message);
                        break;
                    case R.id.color_btn:
                        currentColor = ContextCompat.getColor(MainActivity.this, R.color.openDialog);
                        openDialog();
                        break;
                }
                if (isConnected) connectedThread.write(message);
            }
        };


        up_btn.setOnClickListener(ClickListener_control);
        down_btn.setOnClickListener(ClickListener_control);
        left_btn.setOnClickListener(ClickListener_control);
        right_btn.setOnClickListener(ClickListener_control);
        AP_btn.setOnClickListener(ClickListener_control);
        indi_btn.setOnClickListener(ClickListener_control);
        LP_btn.setOnClickListener(ClickListener_control);
        sign_btn.setOnClickListener(ClickListener_control);
        on_off_btn.setOnClickListener(ClickListener_control);
        color_btn.setOnClickListener(ClickListener_control);
    }

    public void openDialog() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, currentColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                send_color = Integer.toHexString(color);
                send_color = send_color.substring(2);
                String message = send_color;
                String topic = color_pub;
                pub(topic, message);

                if (isConnected) connectedThread.write("c" + message);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }
        });
        dialog.show();
    }


    //--------------------------------SPINNERS------------------------------------------------------
    public void spinners() {

        spinner_language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setlan(i);

                if (color_spinner)
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.WHITE);
                if (!color_spinner)
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        spinner_graph.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //String[] choose = getResources().getStringArray(R.array.graph_list);
                //choose[i];
                if (color_spinner)
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.WHITE);
                if (!color_spinner)
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                //((TextView) adapterView.getChildAt(0)).setTextSize(12);
                switch (i) {
                    case 0:
                        chart_temp.setVisibility(View.VISIBLE);
                        chart_hum.setVisibility(View.GONE);
                        chart_bar.setVisibility(View.GONE);
                        chart_dis.setVisibility(View.GONE);
                        break;
                    case 1:
                        chart_temp.setVisibility(View.GONE);
                        chart_hum.setVisibility(View.VISIBLE);
                        chart_bar.setVisibility(View.GONE);
                        chart_dis.setVisibility(View.GONE);
                        break;
                    case 2:
                        chart_temp.setVisibility(View.GONE);
                        chart_hum.setVisibility(View.GONE);
                        chart_bar.setVisibility(View.VISIBLE);
                        chart_dis.setVisibility(View.GONE);
                        break;
                    case 3:
                        chart_temp.setVisibility(View.GONE);
                        chart_hum.setVisibility(View.GONE);
                        chart_bar.setVisibility(View.GONE);
                        chart_dis.setVisibility(View.VISIBLE);
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void setlan(int i) {
        SharedPreferences.Editor ed = sPef.edit();
        switch (i) {
            case 0:
                textView1.setText(getResources().getText(R.string.label_address));
                textView2.setText(getResources().getText(R.string.label_port));
                textView3.setText(getResources().getText(R.string.label_name));
                textView4.setText(getResources().getText(R.string.label_password));
                textView_13.setText(getResources().getText(R.string.label_language));
                DT_switch.setText(getResources().getText(R.string.label_DT));
                BG_switch.setText(getResources().getText(R.string.label_BG_mode));
                spinner_graph.setAdapter(adapter_ru);
                text_sensors.setText(getResources().getText(R.string.label_sensors));
                text_weather.setText(getResources().getText(R.string.label_weather));
                text_sing.setText(getResources().getText(R.string.label_signaling));
                text_sound.setText(getResources().getText(R.string.label_sound));
                text_light.setText(getResources().getText(R.string.label_light));
                text_mov.setText(getResources().getText(R.string.label_move));
                text_weather_prog.setText(getResources().getText(R.string.label_weather_prog));

                ed.putString("select_item", "ru");
                ed.apply();
                break;
            case 1:
                textView1.setText(getResources().getText(R.string.label_address_en));
                textView2.setText(getResources().getText(R.string.label_port_en));
                textView3.setText(getResources().getText(R.string.label_name_en));
                textView4.setText(getResources().getText(R.string.label_password_en));
                DT_switch.setText(getResources().getText(R.string.label_DT_en));
                textView_13.setText(getResources().getText(R.string.label_language_en));
                BG_switch.setText(getResources().getText(R.string.label_BG_mode_en));
                spinner_graph.setAdapter(adapter_en);
                text_sensors.setText(getResources().getText(R.string.label_sensors_en));
                text_weather.setText(getResources().getText(R.string.label_weather_en));
                text_sing.setText(getResources().getText(R.string.label_signaling_en));
                text_sound.setText(getResources().getText(R.string.label_sound_en));
                text_light.setText(getResources().getText(R.string.label_light_en));
                text_mov.setText(getResources().getText(R.string.label_move_en));
                text_weather_prog.setText(getResources().getText(R.string.label_weather_prog_en));

                ed.putString("select_item", "en");
                ed.apply();
                break;
        }
    }


    //-----------------------------------------SWITCH-----------------------------------------------

    public void switch_void() {
        View.OnClickListener ClickListener_control = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {

                    case R.id.switch_DT:
                        if (DT_switch.isChecked()) {
                            setColor(true);
                            SharedPreferences.Editor ed = sPef.edit();
                            ed.putString("check_DT", "DT_on");
                            color_spinner = true;
                            ed.apply();
                            restartActivity();
                        }
                        if (!DT_switch.isChecked()) {
                            SharedPreferences.Editor ed = sPef.edit();
                            setColor(false);
                            ed.putString("check_DT", "DT_off");
                            color_spinner = false;
                            ed.apply();
                            restartActivity();
                        }
                        break;

                    case R.id.switch_BG:
                        if (BG_switch.isChecked()) {
                            SharedPreferences.Editor ed = sPef.edit();
                            ed.putString("check_BG", "BG_on");
                            ed.apply();
                        }
                        if (!BG_switch.isChecked()) {
                            SharedPreferences.Editor ed = sPef.edit();
                            ed.putString("check_BG", "BG_off");
                            ed.apply();
                        }
                        break;
                }
            }
        };
        DT_switch.setOnClickListener(ClickListener_control);
        BG_switch.setOnClickListener(ClickListener_control);
    }

    public void setColor(boolean check) {
        color_spinner = check;
        if (check) {
            main_layout.setBackgroundColor(getResources().getColor(R.color.bg_activity_black));
            bottomNav.setBackgroundColor(getResources().getColor(R.color.bg_layout_black));
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bg_layout_black)));


            //ДОМ
            temp_view.setTextColor(Color.WHITE);
            hum_view.setTextColor(Color.WHITE);
            bar_view.setTextColor(Color.WHITE);
            dis_view.setTextColor(Color.WHITE);
            sen_layout.setBackgroundColor(getResources().getColor(R.color.bg_layout_black));
            text_sound.setTextColor(Color.WHITE);
            text_light.setTextColor(Color.WHITE);
            text_mov.setTextColor(Color.WHITE);
            sign_text_layout.setBackgroundColor(getResources().getColor(R.color.bg_layout_black));
            weather_layout.setBackgroundColor(getResources().getColor(R.color.bg_layout_black));
            text_weather_prog.setTextColor(Color.WHITE);
            text_prognosis.setTextColor(Color.WHITE);
            text_chance.setTextColor(Color.WHITE);

            //УПРАВЛЕНИЕ
            graph_layout.setBackgroundColor(getResources().getColor(R.color.bg_layout_black));

            //НАСТРОЙКИ
            textView_13.setTextColor(Color.GRAY);
            eT_MQTTHOST.setTextColor(Color.WHITE);
            eT_PORT.setTextColor(Color.WHITE);
            eT_USERNAME.setTextColor(Color.WHITE);
            eT_PASSWORD.setTextColor(Color.WHITE);
            DT_switch.setTextColor(Color.WHITE);
            BG_switch.setTextColor(Color.WHITE);
            mqtt_layout.setBackgroundColor(getResources().getColor(R.color.bg_layout_black));
            DT_layout.setBackgroundColor(getResources().getColor(R.color.bg_layout_black));
            lan_layout.setBackgroundColor(getResources().getColor(R.color.bg_layout_black));
            BG_layout.setBackgroundColor(getResources().getColor(R.color.bg_layout_black));


        } else {
            main_layout.setBackgroundColor(Color.WHITE);
            bottomNav.setBackgroundColor(Color.WHITE);
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));


            //ДОМ
            temp_view.setTextColor(Color.BLACK);
            hum_view.setTextColor(Color.BLACK);
            bar_view.setTextColor(Color.BLACK);
            dis_view.setTextColor(Color.BLACK);
            sen_layout.setBackgroundColor(getResources().getColor(R.color.bg_layout_grey));
            text_sound.setTextColor(Color.BLACK);
            text_light.setTextColor(Color.BLACK);
            text_mov.setTextColor(Color.BLACK);
            sign_text_layout.setBackgroundColor(getResources().getColor(R.color.bg_layout_grey));
            weather_layout.setBackgroundColor(getResources().getColor(R.color.bg_layout_grey));
            text_weather_prog.setTextColor(Color.BLACK);
            text_prognosis.setTextColor(Color.BLACK);
            text_chance.setTextColor(Color.BLACK);


            //УПРАВЛЕНИЕ
            graph_layout.setBackgroundColor(Color.WHITE);

            //НАСТРОЙКИ
            textView_13.setTextColor(Color.BLACK);
            eT_MQTTHOST.setTextColor(Color.BLACK);
            eT_PORT.setTextColor(Color.BLACK);
            eT_USERNAME.setTextColor(Color.BLACK);
            eT_PASSWORD.setTextColor(Color.BLACK);
            DT_switch.setTextColor(Color.BLACK);
            BG_switch.setTextColor(Color.BLACK);
            mqtt_layout.setBackgroundColor(Color.WHITE);
            DT_layout.setBackgroundColor(Color.WHITE);
            lan_layout.setBackgroundColor(Color.WHITE);
            BG_layout.setBackgroundColor(Color.WHITE);
        }
    }


    //----------------------------------ПРОЧЕЕ------------------------------------------------------
    public void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void loadText() {
        //eT_MQTTHOST.setText(sPef.getString("mqqt_prom", ""));
        eT_MQTTHOST.setText(sPef.getString("mqqt_ed_prom", ""));
        eT_PORT.setText(sPef.getString("port_prom", ""));
        eT_USERNAME.setText(sPef.getString("username_prom", ""));
        eT_PASSWORD.setText(sPef.getString("password_prom", ""));
    }

    public void settings_graph() {
        chart_temp = (LineChart) findViewById(R.id.chart_temp);
        chart_hum = (LineChart) findViewById(R.id.chart_hum);
        chart_bar = (LineChart) findViewById(R.id.chart_bar);
        chart_dis = (LineChart) findViewById(R.id.chart_dis);

        Legend legend_temp = chart_temp.getLegend();
        Legend legend_hum = chart_hum.getLegend();
        Legend legend_bar = chart_bar.getLegend();
        Legend legend_dis = chart_dis.getLegend();

        Description description = new Description();
        description.setText("");

        temp_set = new LineDataSet(null, "Temperature");
        hum_set = new LineDataSet(null, "Humidity");
        bar_set = new LineDataSet(null, "Pressure");
        dis_set = new LineDataSet(null, "Distance");

        temp_data = new LineData();
        hum_data = new LineData();
        bar_data = new LineData();
        dis_data = new LineData();

        temp_data.addDataSet(temp_set);
        hum_data.addDataSet(hum_set);
        bar_data.addDataSet(bar_set);
        dis_data.addDataSet(dis_set);

        chart_temp.setData(temp_data);
        chart_hum.setData(hum_data);
        chart_bar.setData(bar_data);
        chart_dis.setData(dis_data);

        temp_set.setColor(Color.RED);
        temp_set.setDrawCircleHole(false);
        temp_set.setCircleColor(Color.RED);
        temp_set.setValueTextSize(0f);
        temp_set.setLineWidth(3f);
        chart_temp.getAxisRight().setEnabled(false);
        chart_temp.getAxisLeft().setGridColor(Color.GRAY);
        chart_temp.getAxisLeft().setTextColor(Color.GRAY);
        chart_temp.getAxisLeft().setTextSize(12f);
        chart_temp.getXAxis().setEnabled(false);
        chart_temp.setDescription(description);
        legend_temp.setTextColor(Color.GRAY);

        hum_set.setColor(Color.BLUE);
        hum_set.setDrawCircleHole(false);
        hum_set.setCircleColor(Color.BLUE);
        hum_set.setValueTextSize(0f);
        hum_set.setLineWidth(3f);
        chart_hum.getAxisRight().setEnabled(false);
        chart_hum.getAxisLeft().setGridColor(Color.GRAY);
        chart_hum.getAxisLeft().setTextColor(Color.GRAY);
        chart_hum.getAxisLeft().setTextSize(12f);
        chart_hum.getXAxis().setEnabled(false);
        chart_hum.setDescription(description);
        legend_hum.setTextColor(Color.GRAY);

        bar_set.setColor(Color.MAGENTA);
        bar_set.setDrawCircleHole(false);
        bar_set.setCircleColor(Color.MAGENTA);
        bar_set.setValueTextSize(0f);
        bar_set.setLineWidth(3f);
        chart_bar.getAxisRight().setEnabled(false);
        chart_bar.getAxisLeft().setGridColor(Color.GRAY);
        chart_bar.getAxisLeft().setTextColor(Color.GRAY);
        chart_bar.getAxisLeft().setTextSize(12f);
        chart_bar.getXAxis().setEnabled(false);
        chart_bar.setDescription(description);
        legend_bar.setTextColor(Color.GRAY);

        dis_set.setColor(Color.CYAN);
        dis_set.setDrawCircleHole(false);
        dis_set.setCircleColor(Color.CYAN);
        dis_set.setValueTextSize(0f);
        dis_set.setLineWidth(3f);
        chart_dis.getAxisRight().setEnabled(false);
        chart_dis.getAxisLeft().setGridColor(Color.GRAY);
        chart_dis.getAxisLeft().setTextColor(Color.GRAY);
        chart_dis.getAxisLeft().setTextSize(12f);
        chart_dis.getXAxis().setEnabled(false);
        chart_dis.setDescription(description);
        legend_dis.setTextColor(Color.GRAY);
    }

    @Override
    protected void onDestroy() {
        if (BG_switch.isChecked()) {
            //startService(new Intent(this, DataCollection.class));
        }

        if (isConnected) unregisterReceiver(mRecevier);
        if (connectThread != null) {
            connectThread.cancel();
        }
        if (connectedThread != null) {
            connectedThread.cancel();
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (MQTTHOST.equals("") || PASSWORD.equals("") || USERNAME.equals("")) {
        } else MQTT_void();
        super.onResume();
    }

    public void getData() {
        stopService(new Intent(this, DataCollection.class));

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        //ContentValues contentValues = new ContentValues();

        Cursor cursor = database.query(DBHelper.TABLE_DATA_SEN, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int tempIndex = cursor.getColumnIndex(DBHelper.KEY_TEMP);
            int humIndex = cursor.getColumnIndex(DBHelper.KEY_HUM);
            int barIndex = cursor.getColumnIndex(DBHelper.KEY_BAR);

            do {
                temp_data.addEntry(new Entry(x, cursor.getInt(tempIndex)), 0);
                temp_data.notifyDataChanged();
                chart_temp.notifyDataSetChanged();
                chart_temp.moveViewToX(temp_data.getEntryCount());

                hum_data.addEntry(new Entry(x, cursor.getInt(humIndex)), 0);
                hum_data.notifyDataChanged();
                chart_hum.notifyDataSetChanged();
                chart_hum.moveViewToX(hum_data.getEntryCount());

                bar_data.addEntry(new Entry(x, cursor.getInt(barIndex)), 0);
                bar_data.notifyDataChanged();
                chart_bar.notifyDataSetChanged();
                chart_bar.moveViewToX(bar_data.getEntryCount());

                x++;
            } while (cursor.moveToNext());

            cursor.close();
        }

    }


}
