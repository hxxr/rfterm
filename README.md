# RFTERM
#### Table of Contents
* [Description](#description)
* [Video Demonstration](#video-demonstration)
* [Setup](#setup)
* App Usage
  * [Overview](#overview)
  * [The toolbar](#the-toolbar)
  * [Mouse tracking](#mouse-tracking)
* Post-Setup Troubleshooting
  * [Garbage characters like "`AT^SQPORT? AT AT AT`" appearing](#garbage-characters-like-atsqport-at-at-at-appearing)
  * [Typed characters do not appear in terminal](#typed-characters-do-not-appear-in-terminal)
  * [Stuck on "Connecting" in the app for more than 30 seconds](#stuck-on-connecting-in-the-app-for-more-than-30-seconds)
  * [Full-screen terminal applications (like Vim) are the wrong size](#full-screen-terminal-applications-like-vim-are-the-wrong-size)
  * [App crashes when rotating device screen while connected](#app-crashes-when-rotating-device-screen-while-connected)

## Description
An Android app designed to connect to a Linux shell through the Bluetooth RFCOMM protocol (like connecting to a device using SSH, but using Bluetooth instead of internet).

There are already a large amount of Android apps designed to connect to devices through Bluetooth RFCOMM (like [BlueTerm](https://play.google.com/store/apps/details?id=es.pymasde.blueterm)), but all of these apps have very poor support of full-screen terminal applications such as terminal games (NetHack, Bastet) and even text-editing software (Vim, Emacs, nano), and also do not support keys like the control key or the function keys. On the other hand Android SSH apps like [Termius](https://play.google.com/store/apps/details?id=com.server.auditor.ssh.client) are very good at supporting these things. RFTERM aims to provide the same functionality that these apps do through Bluetooth instead of SSH.

The app also supports mouse tracking. Some programs (like Midnight Commander) track the mouse, you can tap the screen to interact with these programs.

## Video Demonstration
TODO

## Setup
To connect to a shell remotely through Bluetooth from your Android device using RFCOMM, you have to install software on both your Android device and the Linux device you want to connect to.

Go to [the releases page](https://github.com/hxxr/rfterm/releases/) and download the latest RFTERM.apk and install it on your Android device.

Then you need to pair your Android device and Linux device. You only have to do this once.

If your Linux device has a graphical interface you can use it to pair. `blueman-applet` seems to work better than the default Bluetooth settings application in GNOME.

If your Linux device does not have a graphical interface or if the graphical pairing tool did not work, you can use `bluetoothctl` instead to pair (make sure your Android device is on the Bluetooth settings screen and is searching for devices):
<pre><code>user@my-linux-device:~$ <b>bluetoothctl</b>
[NEW] Controller 92:54:C1:58:76:8E my-linux-device [default]
[bluetooth]# <b>power on</b>
Changing power on succeeded
[bluetooth]# <b>agent on</b>
Agent registered
[bluetooth]# <b>scan on</b>
Discovery started
[CHG] Controller 92:54:C1:58:76:8E Discovering: yes
[NEW] Device 00:57:C4:32:D7:3F my-android-device
[bluetooth]# <b>pair 00:57:C4:32:D7:3F</b>
Attempting to pair with 00:57:C4:32:D7:3F
[CHG] Device 00:57:C4:32:D7:3F Connected: yes
Request confirmation
[agent] Confirm passkey 240728 (yes/no): <b>yes</b>
[CHG] Device 00:57:C4:32:D7:3F Modalias: bluetooth v00C4p13A1d1000
[CHG] Device 00:57:C4:32:D7:3F UUIDs: 00001105-0000-1000-8000-00805f9b34fb
[CHG] Device 00:57:C4:32:D7:3F UUIDs: 0000110a-0000-1000-8000-00805f9b34fb
[CHG] Device 00:57:C4:32:D7:3F UUIDs: 0000110c-0000-1000-8000-00805f9b34fb
[CHG] Device 00:57:C4:32:D7:3F UUIDs: 0000110e-0000-1000-8000-00805f9b34fb
[CHG] Device 00:57:C4:32:D7:3F UUIDs: 00001112-0000-1000-8000-00805f9b34fb
[CHG] Device 00:57:C4:32:D7:3F UUIDs: 00001116-0000-1000-8000-00805f9b34fb
[CHG] Device 00:57:C4:32:D7:3F UUIDs: 0000111f-0000-1000-8000-00805f9b34fb
[CHG] Device 00:57:C4:32:D7:3F UUIDs: 0000112f-0000-1000-8000-00805f9b34fb
[CHG] Device 00:57:C4:32:D7:3F UUIDs: 00001132-0000-1000-8000-00805f9b34fb
[CHG] Device 00:57:C4:32:D7:3F UUIDs: 00001200-0000-1000-8000-00805f9b34fb
[CHG] Device 00:57:C4:32:D7:3F UUIDs: 00001800-0000-1000-8000-00805f9b34fb
[CHG] Device 00:57:C4:32:D7:3F UUIDs: 00001801-0000-1000-8000-00805f9b34fb
[CHG] Device 00:57:C4:32:D7:3F UUIDs: 16bcfd00-253f-c348-e831-0db3e334d580
[CHG] Device 00:57:C4:32:D7:3F UUIDs: abbafc00-e56a-484c-b832-8b17cf6cbfe8
[CHG] Device 00:57:C4:32:D7:3F Paired: yes
Pairing successful
[CHG] Device 00:57:C4:32:D7:3F Connected: no
[my-android-device]# <b>exit</b>
Agent unregistered
[DEL] Controller 92:54:C1:58:76:8E my-linux-device [default]
user@my-linux-device:~$
</code></pre>

Make sure you change the MAC address when you enter `pair <mac-address>` so that it reflects the MAC address of your actual Android device.

If you don't already have one set up, your Linux device also needs a daemon script so it can accept connections from your Android device. Your Linux device must support (and have installed) BlueZ, the default Bluetooth stack for Linux devices. Run these commands to set up the RFCOMM daemon. You can add them to your startup script.
```bash
# Restart BlueZ in compatibility mode
# sdptool usually does not work otherwise
sudo rfkill block bluetooth
sudo killall bluetoothd
sudo bluetoothd -C &
sudo rfkill unblock bluetooth

# Create a new serial port to connnect to
# NOTE: The command worked if it printed
# "Serial Port service registered"
# If it printed nothing then it did not work
sudo sdptool add sp

# Set up daemon to accept incoming RFCOMM connections
sudo rfcomm -S -E -A watch rfcomm0 0 sh -c "setsid getty rfcomm0 115200" > /dev/null &
```
Add the following lines near the beginning of the `.bashrc` file of the user you want to login as:
```bash
export TERM=xterm-256color
resize > /dev/null
```
Now try using the app to connect to this Linux device.

## App Usage
#### Overview
Upon loading the app and turning on Bluetooth, the screen will list all devices you have paired to. Remember that you can only connect to devices that you have paired your Android device with.

<img src="https://i.imgur.com/lvG7kbl.png" width="300"/>

Tapping on a device that you have set up to handle RFCOMM connections will connect to it and present you with a terminal.

<img src="https://i.imgur.com/vU9GCuu.png" width="400" />

There is no scrollbar, however you can still scroll by dragging the screen up and down.

#### The toolbar
The blue bar near the keyboard has some supplementary buttons. The toolbar can be hidden by tapping on the screen. On the left there are six buttons (the latter three are revealed by pressing the circular button):
* CTRL - Keys pressed while this is enabled have their character codes bitwise ANDed with 31, just like what happens when you use control on your keyboard.
* FUNC - While enabled you can press the number keys on your keyboard to type F1 through F10 (0 is F10).
* TAB - Types a tab character to trigger autocompletion in a shell.
* ESC - Types an escape character (ASCII character 27).
* INS - Types the equivalent of the insert key on your keyboard.
* DEL - Types the equivalent of the delete key on your keyboard.

In the middle section of the toolbar are arrow keys and some shortcut characters that currently cannot be changed.

On the right are five buttons:
* Shift (upwards arrow) - Serves the same function as the shift key on the on-screen keyboard (i.e. does not type "#" when 3 is pressed with shift). Some applications track when the shift key is held down, for those applications you have to use this toolbar button because the Android API does not provide a way to detect the shift key state.
* Meta (diamond shape) - Keys pressed while this is enabled are preceded by an escape character (ASCII character 27) just like when you use meta or alt on your keyboard.
* Keyboard - Toggles the keyboard.
* Exit - Closes the connection.
* Options (three dots) - Currently unused.

The toolbar changes colour depending on the environment created by whatever application is running in the terminal. There are a total of eight colours.

<img src="https://i.imgur.com/GxM8lFP.png" width="400" />

<img src="https://i.imgur.com/7ZvgdBr.png" width="400" />

<img src="https://i.imgur.com/GMiYlKF.png" width="400" />

#### Mouse tracking
Some applications can track the position of the mouse. This feature is not supported by all terminal emulators (PuTTY is one of those that don't). RFTERM is partially aware of mouse tracking.

In order to click on part of the screen just tap on the desired part of the screen. You can also drag your finger on the screen. This is sufficient to interface with most applications that do mouse tracking.

## Post-Setup Troubleshooting
#### Garbage characters like "`AT^SQPORT? AT AT AT`" appearing
These are called AT commands and are used to communicate with modems. In this case they are undesirable. To disable them add this line to the file `/etc/udev/rules.d/90-rfcomm.rules` on your Linux device (create the file if it does not exist):
```
KERNEL=="rfcomm[0-9]*",ENV{ID_MM_DEVICE_IGNORE}="1"
```
Then try connecting again.

#### Typed characters do not appear in terminal
Enable echo by running this command after the `rfcomm` command:
```bash
sudo stty -F /dev/rfcomm0 115200 echo &
```

#### Stuck on "Connecting" in the app for more than 30 seconds
Restart your Android device. Bluetooth can be finicky sometimes.

#### Full-screen terminal applications (like Vim) are the wrong size
Run the command `resize` from in the app.

#### App crashes when rotating device screen while connected
Yes. I have not yet implemented the code to resize the terminal when the Android device is rotated.
