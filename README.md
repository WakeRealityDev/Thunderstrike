Wake Reality's Thunderstrike
===============================
Quick and dirty example app / proof of conept of how to interface RemGlk JSON data exchange with the Thunderword app. It also demonstrates how to specify the data file you want and for Thunderword to launch the story.

Intended for Android Studio 2.2.3

Quickstart:

1. Thunderword Experimental installed on your Android device.
2. Place testing stories in a shared folder like /sdcard/mygames with ADB push or download via web browser. Glulx stories are recommended. The Git interpreter is the default for Glulx data files.
3. Edit the code in this project to have the correct paths to your game data files from step 2.
4. Build and deploy the app onto your device with Android Studio. If permissions are correct, a button to open RemoteSimpleActivity should appear.
5. On the RemoteSimpleActivity there are two green TextView 'buttons' at the top (labeled LaunchA / B), press one to instruct Thunderword to load one of the datafiles you setup in step 2. You can press these buttons at any time to force Thunderword to restart the interpreter (it will end the story and start the story data file provided).
6. You shold start to get JSON and game story data to appear on your screen shortly after pressing the LaunchA TextView.

This is primitive in user interface and is intended to demonstrate the techniques for interfacing with Thundword engines.

From here on out, it's up to you how to make best use of the JSON that is being provided to your app.

This is early and rushed, more should be coming in the future. Thank you!


License
==========
The code in this Thunderstrike project is licensed Apache 2.0 to allow you to easily mix with any of the Android SDK samples provided by Google.
