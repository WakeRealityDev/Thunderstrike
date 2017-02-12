Wake Reality's Thunderstrike
===============================
Quick and dirty example app / proof of concept of how to interface RemGlk JSON data exchange with the Thunderword app. It also demonstrates how to specify the data file you want and how to trigger Thunderword to launch the story.

Intended for Android Studio 2.2.3

Quickstart:

1. Thunderword Experimental app should first be installed on your Android device.
2. Place testing stories in a shared folder like /sdcard/myfiction with ADB push or download via web browser. Glulx stories are recommended. The Git interpreter is the default for Glulx data files.
3. Edit the code in this Thunderstrike project to have the correct paths to your game data files from step 2. Look for the method named launchStoryClick in RemoteSimpleActivity.java
4. Build and deploy this Thunderstrike app onto your device with Android Studio. If file permissions are correct, a button to open RemoteSimpleActivity should appear.
5. On the RemoteSimpleActivity there are two green TextView 'buttons' at the top (labeled LaunchA / B), press one to instruct Thunderword to load one of the datafiles you setup in step 2. You can press these buttons at any time to force Thunderword to restart the interpreter (it will end the story and start the story data file provided).
6. You should start to get JSON and game story data to appear on your screen shortly after pressing the LaunchA TextView.

This is primitive in user interface and is intended to demonstrate the techniques for interfacing to Thunderword engines via JSON sharing / Broadcasts. What to look for in this source code:

1. AndroidManifest.xml has the strings you need for broadcast receiving the JSON in your own apps.
2. The sendBroadcast intent for instructing Thunderword to open your story data file.
3. The Engine State codes to know if Thunderword has shut down your story
4. Use of GreenRobot EventBus 3.0 for changing of threads and passing data into the activity (this is not required in any way, but is how this Thunderstrike app was coded). https://github.com/greenrobot/EventBus
5. Uses only the built-in JSONObject / JSONArray parsing. Your own app could use higher performance libraries if you wish (Android community has several options for JSON processing).
6. The sample does not process Glk windows, timers, graphics and barely processes input. That's the JSON programming work you would do. Thunderword knows how to do these on it's own, but the whole point of this Thunderstrike app is for you to have the liberty to work with the raw JSON and present it as you see best.
7. The two stories I tested Thunderstrike with are "Rover's Day Out" and "Counterfeit Monkey", both can be downloaded here: http://ifdb.tads.org/
8. If there are graphics images in the story, the /sdcard/ path filenames will not be correctly - look in the directory that RemGlk says holds the images and you will find them with a different naming convention. Thunderword internally compensates for this, but the shared JSON does not and you will have to adjust.
9.  ... more tips & observations to come ....

From here on out, it's up to you how to make best use of the JSON that is being provided to your app.

This is early and rushed, more should be coming in the future. Your sample code contributions are welcome! Thank you!


The JSON
==========
The JSON is from a modified version of RemGlk that is inside Thunderword. The modifications are basically { added colors support, Glk style hint tuples on Glk windows, status related to the loading and unloading of the interpreter engine, errors and debugging output related to incomplete code }. The good news is that the field names and values are the same as the stock RemGlk - so you can use an unmodified version on your desktop system if you wish to work with the output in different ways. More documentation on the additions to the RemGlk JSON data is forthcoming.

The standard RemGlk JSON is documented here:  http://eblong.com/zarf/glk/remglk/docs.html

NOTE: Right now thoe code probably yields no control over the RemGlk INIT to set the desired screen size emulation of RemGlk - Thunderword is doing that on it's own. Future updates should provide a way to specify those parameters along with the data file.


Target Audience
=================
This is intended for intermediate to advanced developers who wish to make their own Android presentation layer - bypassing the one built into Thunderword.


License
==========
The code in this Thunderstrike project is licensed Apache 2.0 to allow you to easily mix with any of the Android SDK samples provided by Google.
