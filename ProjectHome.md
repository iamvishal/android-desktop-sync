After having spent frustrating amount of time gathering data manually from the Android emulator and no way to keep the emulator's clipboard to be in sync with the desktop's - I've written this program

There are 2 components in this
Desktop (Server)
Android (client)

Bi-directional clipboard sync will work on your regular Android device and Desktop (Windows/Laptop) too

# The components explained #

## Server side component ##
Usually runs on your desktop machine (I’ve tested this on Windows) – Written in Java
This has a server socket and whenever receives an input data – updates the desktop clipboard.
## Mobile Side client ##
Runs on your Android Emulator or your device (should be connected to the PC through a network usually WiFi – Written in Java (using Android SDK) – Target set to 2.2 Froyo
This has a live thread checking if the device clipboard has been updated, If yes – It’ll establish a network connection to the server component to dump the latest clipboard.