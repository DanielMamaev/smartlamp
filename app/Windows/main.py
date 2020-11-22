#pyinstaller pyinstaller -F main.py --hidden-import plyer.platforms.win.notification

import sys  # sys нужен для передачи argv в QApplication

from PyQt5 import QtWidgets, QtGui
from PyQt5.QtCore import QThread
from PyQt5.QtWidgets import QColorDialog
import configparser
import platform
import os

import plyer

import design  # Это наш конвертированный файл дизайна

import paho.mqtt.client as mqtt
#postman.cloudmqtt.com
#17110
#obtxfdbf
#6uD0FYaNQT5K


class ExampleApp(QtWidgets.QMainWindow, design.Ui_MainWindow):
    def __init__(self):
        # Это здесь нужно для доступа к переменным, методам
        # и т.д. в файле design.py
        super().__init__()
        self.setupUi(self)  # Это нужно для инициализации нашего дизайна

        self.config = configparser.ConfigParser()
        self.config_namef = "settings.ini"
        self.config.read(self.config_namef)
        self.lineEdit_host.setText(self.config["SETTINGS"]["mqtt_host"])
        self.lineEdit_port.setText(self.config["SETTINGS"]["mqtt_port"])
        self.lineEdit_username.setText(self.config["SETTINGS"]["mqtt_username"])
        self.lineEdit_password.setText(self.config["SETTINGS"]["mqtt_password"])
        if self.config["SETTINGS"]["autoconnect"] == "True":
            self.checkBox_autoconnect.setChecked(True)
        if self.config["SETTINGS"]["autoconnect"] == "False":
            self.checkBox_autoconnect.setChecked(False)

        if self.config["SETTINGS"]["mode"] == "light":
            self.comboBox_background.setCurrentIndex(0)
            self.color_mode()
        if self.config["SETTINGS"]["mode"] == "dark":
            self.comboBox_background.setCurrentIndex(1)
            self.color_mode()

        if self.config["SETTINGS"]["language"] == "en":
            self.comboBox_language.setCurrentIndex(0)
        if self.config["SETTINGS"]["language"] == "ru":
            self.comboBox_language.setCurrentIndex(1)

        self.client = mqtt.Client("SmartLamp_Desktop")

        if self.checkBox_autoconnect.isChecked():
            self.connect_click()

        self.pushButton_connect.clicked.connect(self.connect_click)
        self.pushButton_disconnect.clicked.connect(self.disconnect_click)
        self.pushButton_exit.clicked.connect(self.exit_click)
        self.toolButton_brig.clicked.connect(self.brig_click)
        self.toolButton_on_off.clicked.connect(self.on_off_click)
        self.toolButton_right.clicked.connect(self.right_click)
        self.toolButton_left.clicked.connect(self.left_click)
        self.toolButton_up.clicked.connect(self.up_click)
        self.toolButton_down.clicked.connect(self.down_click)
        self.toolButton_color.clicked.connect(self.color_click)
        self.toolButton_autoplay.clicked.connect(self.autoplay_click)
        self.toolButton_indi.clicked.connect(self.hash_click)
        self.toolButton_sound.clicked.connect(self.sound_click)
        self.toolButton_security.clicked.connect(self.sign_click)
        self.verticalSlider_brig.valueChanged[int].connect(self.changeValue)
        self.checkBox_autoconnect.stateChanged.connect(self.autoconnect_check)
        self.comboBox_background.activated.connect(self.color_mode)
        self.comboBox_language.activated.connect(self.language_mode)

        self.sign_check = ""


        """if (mov == 8)sign_check = true;
        if (mov == 9)sign_check = false;"""

    def language_mode(self):
        if self.comboBox_language.currentIndex() == 0:
            self.config.set("SETTINGS", "language", "en")
            with open(self.config_namef, 'w') as configfile:
                self.config.write(configfile)
        if self.comboBox_language.currentIndex() == 1:
            self.config.set("SETTINGS", "language", "ru")
            with open(self.config_namef, 'w') as configfile:
                self.config.write(configfile)

    def color_mode(self):
        if self.comboBox_background.currentIndex() == 0:
            self.config.set("SETTINGS", "mode", "light")
            with open(self.config_namef, 'w') as configfile:
                self.config.write(configfile)
            self.tab_main.setStyleSheet("QWidget{ background-color: white;}")
            self.centralwidget.setStyleSheet("QWidget{ background-color: Gainsboro;}")
            self.label_temp.setStyleSheet("QWidget{ color: black;}")
            self.label_hum.setStyleSheet("QWidget{ color: black;}")
            self.label_dis.setStyleSheet("QWidget{ color: black;}")
            self.label_bar.setStyleSheet("QWidget{ color: black;}")
            self.label_chance.setStyleSheet("QWidget{ color: black;}")


        if self.comboBox_background.currentIndex() == 1:
            self.config.set("SETTINGS", "mode", "dark")
            with open(self.config_namef, 'w') as configfile:
                self.config.write(configfile)
            self.tabWidget.setStyleSheet("QWidget{color: Gainsboro;}")
            self.tab_main.setStyleSheet("QWidget{ background-color: #2B2B2B; color: white;} ")
            self.centralwidget.setStyleSheet("QWidget{ background-color: #2B2B2B; color: Gainsboro;}")
            self.label_temp.setStyleSheet("QWidget{ color: white;}")
            self.label_hum.setStyleSheet("QWidget{ color: white;}")
            self.label_dis.setStyleSheet("QWidget{ color: white;}")
            self.label_bar.setStyleSheet("QWidget{ color: white;}")
            self.label_chance.setStyleSheet("QWidget{ color: white;}")
            self.pushButton_exit.setStyleSheet("QWidget{ background-color: red; color: Black;}")
            self.pushButton_disconnect.setStyleSheet("QWidget{ background-color: red; color: Black;}")
            self.pushButton_connect.setStyleSheet("QWidget{ background-color: green; color: Black;}")
            self.comboBox_background.setStyleSheet("QWidget{ color: white;}")
            self.comboBox_language.setStyleSheet("QWidget{ color: white;}")
            self.lineEdit_host.setStyleSheet("QWidget{ background-color: #3B3B3B; }")
            self.lineEdit_port.setStyleSheet("QWidget{ background-color: #3B3B3B; }")
            self.lineEdit_username.setStyleSheet("QWidget{ background-color: #3B3B3B; }")
            self.lineEdit_password.setStyleSheet("QWidget{ background-color: #3B3B3B; }")

    def autoconnect_check(self, state):
        if not state == self.checkBox_autoconnect.isChecked():
            self.config.set("SETTINGS", "autoconnect", "True")
            with open(self.config_namef, 'w') as configfile:
                self.config.write(configfile)
        else:
            self.config.set("SETTINGS", "autoconnect", "False")
            with open(self.config_namef, 'w') as configfile:
                self.config.write(configfile)


    def connect_click(self):
        if not self.client.is_connected():
            mqtt_host = self.lineEdit_host.text()
            mqtt_port = int(self.lineEdit_port.text())
            mqtt_user = self.lineEdit_username.text()
            mqtt_pass = self.lineEdit_password.text()
            self.mqtt_thread_instance = mqtt_thread(mainwindow=self)
            if (not mqtt_host == "") and (not mqtt_port == ""):
                self.client.connect(mqtt_host, mqtt_port, 60)
            self.client.username_pw_set(mqtt_user, mqtt_pass)

            self.config.set("SETTINGS", "mqtt_host", mqtt_host)
            self.config.set("SETTINGS", "mqtt_port", str(mqtt_port))
            self.config.set("SETTINGS", "mqtt_username", mqtt_user)
            self.config.set("SETTINGS", "mqtt_password", mqtt_pass)
            with open(self.config_namef, 'w') as configfile:
                self.config.write(configfile)

            self.mqtt_thread_instance.start()


    def disconnect_click(self):
        self.client.disconnect()
        self.client.username_pw_set("", "")
        print("Disconnected!")

    def exit_click(self):
        sys.exit()

    def on_off_click(self):
        self.client.publish("move", "1")

    def right_click(self):
        self.client.publish("move", "2")

    def left_click(self):
        self.client.publish("move", "3")

    def autoplay_click(self):
        self.client.publish("move", "4")

    def up_click(self):
        self.client.publish("move", "5")

    def down_click(self):
        self.client.publish("move", "6")

    def hash_click(self):
        self.client.publish("move", "7")

    def sign_click(self):
        if self.sign_check == "9":
            self.client.publish("move", "8")
        else:
            self.client.publish("move", "9")

    def sound_click(self):
        self.client.publish("move", "100")

    def restart_click(self):
        self.client.publish("move", "0")
        pass

    def color_click(self):
        color = QColorDialog.getColor().name()
        hex = color.replace("#", "")
        if not hex == "000000":
            self.client.publish("color", hex)

    def brig_click(self):
        self.client.publish("brig", "255")

    def changeValue(self, value):
        self.client.publish("brig", str(value))

    def push_notifications(self, title, message, ico):
        plt = platform.system()
        command = ""
        if plt == "Darwin":
            command = "osascript -e 'display notification" + message + "with title" + title
            os.system(command)
        elif plt == "Linux":
            command = f"notify-send " + title + " " + message
            os.system(command)
        elif plt == "Windows":
            plyer.notification.notify(title, message, "SmartLamp", ico)







#--------------------------------------- MQTT THREAD -------------------------------------------------------------------
class mqtt_thread(QThread):
    def __init__(self, mainwindow, parent=None):
        super().__init__()
        self.mainwindow = mainwindow
        self.vu_repeat = True
        self.light_repeat = False
        self.mov_repeat = True



    def run(self):
        self.mainwindow.client.on_connect = self.on_connect
        self.mainwindow.client.on_message = self.on_message
        self.mainwindow.client.loop_forever()


    def on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            print("Connected to MQTT!")

            self.mainwindow.client.subscribe("Temp")
            self.mainwindow.client.subscribe("Hum")
            self.mainwindow.client.subscribe("Dis")
            self.mainwindow.client.subscribe("Bar")
            self.mainwindow.client.subscribe("Chance")

            self.mainwindow.client.subscribe("color")
            self.mainwindow.client.subscribe("move")
            self.mainwindow.client.subscribe("brig")

            self.mainwindow.client.subscribe("Mov")
            self.mainwindow.client.subscribe("Light")
            self.mainwindow.client.subscribe("VU")
            self.mainwindow.client.subscribe("Sign")

        else:
            print("Bad connection MQTT. Returned code = ", rc)



    def on_message(self, client, userdata, msg):
        if msg.topic == "Temp":
            pay = str(msg.payload.decode("ascii"))
            self.mainwindow.label_temp.setText(pay + " °C")
            #print("Temp", pay)
            #self.mainwindow.push_notifications("it is title", "messageee")

        if msg.topic == "Hum":
            pay = str(msg.payload.decode("ascii"))
            self.mainwindow.label_hum.setText(pay + " %")
            #print("Hum", pay)

        if msg.topic == "Dis":
            pay = str(msg.payload.decode("ascii"))
            self.mainwindow.label_dis.setText(pay + " cm")
            #print("Dis", pay)

        if msg.topic == "Bar":
            pay = str(msg.payload.decode("ascii"))
            self.mainwindow.label_bar.setText(pay + " mmHg")
            #print("Bar", pay)

        if msg.topic == "Chance":
            pay = str(msg.payload.decode("ascii"))
            self.mainwindow.label_chance.setText(pay + " %")
            #print("Chance", pay)

        if msg.topic == "Mov":
            pay = str(msg.payload.decode("ascii"))
            if pay == "0":
                self.mov_repeat = True
                self.mainwindow.label_12.setPixmap(QtGui.QPixmap("icon/run_off_80.svg"))
            else:
                self.mainwindow.label_12.setPixmap(QtGui.QPixmap("icon/run_on_80.svg"))
                if self.mov_repeat:
                    self.mov_repeat = False
                    self.mainwindow.push_notifications("Moving", "Moving sensor triggered!", 'icon/run.ico')

        if msg.topic == "Light":
            pay = str(msg.payload.decode("ascii"))
            if pay == "0":
                self.light_repeat = True
                self.mainwindow.label_11.setPixmap(QtGui.QPixmap("icon/sun_off_80.svg"))
            else:
                self.mainwindow.label_11.setPixmap(QtGui.QPixmap("icon/sun_on_80.svg"))
                if self.mainwindow.sign_check == "8":
                    if self.light_repeat:
                        self.light_repeat = False
                        self.mainwindow.push_notifications("Light", "Light sensor triggered!", 'icon/sun.ico')

        if msg.topic == "VU":
            pay = str(msg.payload.decode("ascii"))
            if pay == "0":
                self.vu_repeat = True
                self.mainwindow.label_10.setPixmap(QtGui.QPixmap("icon/micro_off_80.svg"))
            else:
                self.mainwindow.label_10.setPixmap(QtGui.QPixmap("icon/micro_on_80.svg"))
                if self.vu_repeat:
                    self.vu_repeat = False
                    self.mainwindow.push_notifications("Sound", "Sound sensor triggered!", 'icon/micro.ico')



        if msg.topic == "Sign":
            pay = str(msg.payload.decode("ascii"))
            self.mainwindow.sign_check = pay
            if pay == "8":
                icon = QtGui.QIcon()
                icon.addPixmap(QtGui.QPixmap("icon/eye.svg"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
                self.mainwindow.toolButton_security.setIcon(icon)
            else:
                icon = QtGui.QIcon()
                icon.addPixmap(QtGui.QPixmap("icon/eye_off.svg"), QtGui.QIcon.Normal, QtGui.QIcon.Off)
                self.mainwindow.toolButton_security.setIcon(icon)



def main():
    app = QtWidgets.QApplication(sys.argv)  # Новый экземпляр QApplication
    app.setStyle("Fusion") #'Breeze', 'Oxygen', 'QtCurve', 'Windows', 'Fusion'
    window = ExampleApp()  # Создаём объект класса ExampleApp
    window.show()  # Показываем окно
    app.exec_()  # и запускаем приложение

if __name__ == '__main__':  # Если мы запускаем файл напрямую, а не импортируем
    main()  # то запускаем функцию main()