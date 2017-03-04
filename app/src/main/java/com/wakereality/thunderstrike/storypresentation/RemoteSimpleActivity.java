package com.wakereality.thunderstrike.storypresentation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.wakereality.thunderstrike.BuildConfig;
import com.wakereality.thunderstrike.R;
import com.wakereality.thunderstrike.dataexchange.EngineConst;
import com.wakereality.thunderstrike.sendreceive.RemGlkInputSender;
import com.wakereality.thunderstrike.dataexchange.EventEngineRunningStatus;
import com.wakereality.thunderstrike.dataexchange.EventIncomingRemGlkPayload;
import com.wakereality.thunderstrike.dataexchange.PayloadToEngine;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Apache 2.0 license on this source file
 * (c) Copyright 2017, Stephen A Gutknecht, All Rights Reserved
 *
 * This class is a demonstration of basic Input/Output to Thunderword app using RemGlk JSON
 *   and various engine control BroadcastReceivers. Code contributions / documentation work
 *   are welcome to further the community examples.
 *
 * This is a demonstration app for software developers to make their own client apps to work
 *   in partnership with the Thunderword app.
 *
 * Note: this source code does not honor line-length limits, reformat if you so desire.
 *
 * The JSON parsing in this example is primitive and uses built-in JSONObject/JSONArray structures
 *   Anyone is free to use alternate libraries for more sophisticated mapping of the RemGlk JSON.
 *
 * ToDo: #1 add 'clear' TextView buttons to wipe what's on the output for the person using this app.
 * ToDo: #2 the RemGlk JSON for init of screen size and features is not currently under control of the outside app.
 * ToDo: #3 convention for picking a specific favored interpreter for the same type of binary story file, Example: "Git" or "Glulxe" for "Glulx" binaries.
 * ToDo: #4 convention on parameter to Thunderword regarding prompting user to end running story instead of forcing it closed when launching a new story.
 *    (Noteworthy that if it is your own app that started the story, you should keep track of if it is running through the provided Engine State codes)
 * ToDo: #5 convention / example of how to gracefully exit a story (with no start of a new one).
 */

public class RemoteSimpleActivity extends AppCompatActivity {

    protected ViewGroup rootView0;
    protected TextView topStatusTextView0;
    protected TextView storyOutputRawTextView0;
    protected TextView remGlkInfoOutputTextView0;
    protected RemGlkInputSender inputSender;
    protected GlkInputForWindow inputForSingleGlkWindow = new GlkInputForWindow();
    protected EditText inputEditText0;
    protected int clearScreenCount = 0;
    protected int inputLastGenSend = -1;
    protected int redrawCount = 0;
    protected int thunderWordRemoteEngineStateCode = -1;
    protected int remGlkUpdateGeneration = -1;

    private class GlkInputForWindow {
        public int windowId;
        public int gen;
        public boolean lineInputMode;
        public int maxlen;

        @Override
        public String toString() {
            return "win " + windowId + " gen " + gen + " line? " + lineInputMode;
        }
    }

    protected void postRemGlkInputFromPlayerToThunderword(String inputText) {
        JSONObject tempJSONObject = new JSONObject();
        int thisGen = inputForSingleGlkWindow.gen;
        try {
            // Tip: RemGlk documentation describes these fields and their format.
            tempJSONObject.put("type",   (inputForSingleGlkWindow.lineInputMode) ? "line" : "char");
            tempJSONObject.put("gen",    thisGen);
            tempJSONObject.put("window", inputForSingleGlkWindow.windowId);
            // ToDo: in char mode, use RemGlk special key names
            String inputTextModified = inputText;
            if (inputForSingleGlkWindow.lineInputMode) {
                inputTextModified = inputText.replace("\n", "");
            }
            tempJSONObject.put("value", inputTextModified);
        } catch (JSONException e) {
            Log.e("RemoteSimple", "JSONException formulating input", e);
        }

        if (inputLastGenSend == thisGen) {
            Log.w("RemoteSimple", "[dataToEngine] DUPLICATE_SEND_FAILURE, skip");
        } else {
            inputLastGenSend = thisGen;
            PayloadToEngine eventPayloadToEngine = new PayloadToEngine(tempJSONObject.toString(), EngineConst.PAYLOAD_TO_ENGINE_USE_GENERAL_PURPOSE);
            Log.i("RemoteSimple", "[dataToEngine] " + eventPayloadToEngine.payload);
            EventBus.getDefault().post(eventPayloadToEngine);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("RemoteSimple", "[activityPrep][storyActivity][startActivity] onCreate");
        super.onCreate(savedInstanceState);

        try {
            if (! EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
            onCreateLayoutSpecific();
            // Setup a local sender for player input to Thunderword app.
            inputSender = new RemGlkInputSender(getApplicationContext());
            // Test that Thunderword app is installed and has desired engines available.
            queryRemoteStoryEngineProviders();
        } catch (Exception e) {
            Log.e("RemoteSimple", "[activityPrep][storyActivity][startActivity] Exception in onCreate", e);
        }
    }

    protected void onCreateLayoutSpecific() {
        setContentView(R.layout.activity_thunderword_remote_simple);
        rootView0 = (ViewGroup) findViewById(R.id.activity_main);

        topStatusTextView0 = (TextView) rootView0.findViewById(R.id.topStatusTextView0);
        storyOutputRawTextView0 = (TextView) rootView0.findViewById(R.id.storyOutputRawTextView0);
        remGlkInfoOutputTextView0 =  (TextView) rootView0.findViewById(R.id.remGlkInfoOutputTextView0);
        inputEditText0 = (EditText) rootView0.findViewById(R.id.inputEditText0);
        inputEditText0.addTextChangedListener(new TextWatcher() {
            private CharSequence latestText = "";

            @Override
            public void beforeTextChanged(CharSequence input, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence input, int start, int before, int count) {
                latestText = input;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (inputForSingleGlkWindow.lineInputMode) {
                    // Strategy for line-only input mode is to stay in activity until [enter] key.
                    if (latestText.toString().endsWith("\n")) {
                        postRemGlkInputFromPlayerToThunderword(latestText.toString());
                        editable.clear();
                    }
                } else {
                    // ToDo: there is no conversion of RemGlk codes for special keys.
                    if (latestText.toString().length() > 0) {
                        postRemGlkInputFromPlayerToThunderword(latestText.toString());
                        editable.clear();
                    }
                }
            }
        });
    }


    public void redrawEngineOutput() {
        redrawCount++;
        topStatusTextView0.setText("");
        topStatusTextView0.append("Redraw #" + redrawCount);
        topStatusTextView0.append(" Engine State: " + thunderWordRemoteEngineStateCode);
        topStatusTextView0.append(" RemGlk Gen: " + remGlkUpdateGeneration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ToDO: May be double-checking registration, but thought it was best to show near paired onPause deregister
        // Try to be ready for events incoming before triggering any remote Thunderword activity.
        if (! EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        redrawEngineOutput();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister EventBus - this means that incoming Thunderword content will no longer be
        //  processed, that the JSON labor stops by this activity if the user switches away from
        //  the activity.  This may be desired or undesired depending on your app's intention and
        //  concern for battery usage/etc.
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void setupForNewStoryIncoming() {
        // clear output on screen.
        storyOutputRawTextView0.setText("");
        remGlkInfoOutputTextView0.setText("");
        // Reset input generation.
        inputLastGenSend = -1;
        remGlkUpdateGeneration = -1;
    }

    public void queryRemoteStoryEngineProviders() {
        Intent intent = new Intent();
        // Tell Android to start Thunderword app if not already running.
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("interactivefiction.enginemeta.runstory");
        getApplicationContext().sendBroadcast(intent);
    }


    public void animateClickedView(final View view) {
        // Poor man's animation to show visual feedback of click.
        view.setAlpha(0.2f);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setAlpha(1.0f);
            }
        }, 2200L);
    }

    public Intent setIntentForOutsideEngineProviderApp(Intent intent, int engineProviderPick) {
        // If you did want to pick a specific app to execute, be it one from Wake Reality or otherwise:
        switch (engineProviderPick) {
            case 1:   // Wake Reality's Thunderword [experimental] app
                intent.setPackage("com.wakereality.thunderword.experimental");
                break;
            case 2:   // Wake Reality's Thunderword app (to be released beta April 2, 2017)
                intent.setPackage("com.wakereality.thunderword");
                break;
            case 3:   // Wake Reality's Thunderword LIMIT_TEST app (try to solve issue on difficult devices)
                intent.setPackage("com.wakereality.thunderword.test");
                break;
            case 4:   // Other app
                intent.setPackage("" /* Other Interactive Fiction app that wants to be engine */);
                break;
            case 0:
            default:
                // Don't set it, and let the receiving engines decide who responds
                break;
        }
        return intent;
    }


    protected static AtomicInteger launchToken = new AtomicInteger(0);

    /*
    This is the code to demonstrate how to be a Launcher app and how to start stories in
      Thunderword from your own outside app.
      This example shows sharing of public files on /sdcard/ path, you will need to download
      and populate those data files before this app can work.

      "interactivefiction.enginemeta.runstory" is also a listening broadcast that will return
        broadcast of engines via "interactivefiction.enginemeta.storyengines"
     */
    public void launchStoryClick(final View view) {
        int myLaunchToken = launchToken.incrementAndGet();
        Log.i("RemoteSimple", "click on launchStoryClick button, launchToken" + myLaunchToken);

        animateClickedView(view);

        setupForNewStoryIncoming();

        Intent intent = new Intent();
        // Tell Android to start Thunderword app if not already running.
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        // Inform the Engine Provider who to call back.
        intent.putExtra("sender", BuildConfig.APPLICATION_ID);
        // Tell Thunderword Glulx story data file, it will pick default engine in absence
        //   of additional launch parameters. The name prefix was specifically selected to encourage
        //   the Android development community to use this pattern - and not exclusive to Thunderword.
        intent.setAction("interactivefiction.engine.glulx");

        // helper method to pick which app is running the engine.
        intent = setIntentForOutsideEngineProviderApp(intent, 0 /* All Engine apps */);

        // Tell Thunderword to be headless with a value of 0, no screen activity & only RemGlk data exchange.
        // NOTE: For purposes of testing a "launcher app" that is not headless, values of 1 is for
        //    the Bidirectional Scrolling activity, 2 is Standard scrolling Activity, 3 is TwoWindow activity
        //    Layout choices and preferences are still work-in-progress areas of Thunderword.
        intent.putExtra("activitycode", 0);

        intent.putExtra("launchtoken", "A" + myLaunchToken);

        // Tell Thunderword to not show Toast messages if data file is missing and on headless launch.
        // intent.putExtra("silent", true);
        // Tell Thunderword to not interrupt the running story automatically and prompt the player.
        // intent.putExtra("interrupt", false);

        // Readable/publicly accessible data file for the story to launch.
        // Future: Alternately, your app can be content provider for secure exchange of data file.
        //    Thunderword already has code to take files from content providers from the Android
        //    download manager. What's left to do is to establish a convention for peer apps.
        // "/sdcard/" path is part of why this sample code is described as "quick and dirty", it makes
        //   for a faster tutorial if you have a modern Android 5.1 and newer device. However,
        //   Google seems to want app to app exchange to use content providers.
        // For content provider libraries, I suggest https://github.com/commonsguy/cwac-provider
        //   Your source code contribution to this Thunderstrike samples are welcome!
        switch (view.getId()) {
            case R.id.launchStoryPickATextView0:
                intent.putExtra("path", "/sdcard/myfiction/rover.gblorb");
                break;
            case R.id.launchStoryPickBTextView0:
                intent.putExtra("path", "/sdcard/myfiction/CounterfeitMonkey_release6.gblorb");
                break;
            case R.id.launchStoryPickCTextView0:
                // Z-code / Z-Machine instead of Glulx code for sake of demonstration of launcher app.
                intent.putExtra("path", "/sdcard/myfiction/TheEmptyRoom.zblorb");
                intent.setAction("interactivefiction.engine.zmachine");
                break;
            case 1999999:
                // If you want to help test Thunderword, you could try data files for TADS2 / TADS3 / scott interpreter engines.
                intent.setAction("interactivefiction.engine.TADS");
                intent.setAction("interactivefiction.engine.ScottAdams");
                break;
            case R.id.launchStoryPickDTextView0:
                // With "Head" activity, not "headless" - this is example of launcher app.
                intent.putExtra("path", "/sdcard/myfiction/CounterfeitMonkey_release6.gblorb");
                intent.putExtra("activitycode", 1 /* Bidirectional Scrolling Activity */);
                break;
            case R.id.launchStoryPickETextView0:
                // SHA-256 hash of story data file that Thunderword may or may not know-of in it's
                //    database of installed/managed games.
                // In the absence of a "path" Extra, "datapick" will be checked
                // NOTE that this datapick example has story automation in Thunderword and will generate
                //   several actions / stream of JSON in the first 20 or so seconds.  This
                //   story is the same one on the Thunderword welcome screen menu option labeled
                //   "Launch Test T0" that was added in Thunderword release 121.
                //   This is a very good story to test JSON on, as it has many windows, color options
                //      Glk Style Hints, etc.  The Inform 7 6M62 story source code is available.
                // NOTE: hash is case sensitive, all lowercase
                intent.putExtra("datapick", "aaff415aeacdf8aa73ee049f7842a609b82306c97c9f07b8e95ce7ab87ea");
                break;
            case R.id.launchStoryPickFTextView0:
                // All Things Devours R3 .z5
                intent.putExtra("datapick", "a0b0569c2f57a975f868242b9a1dfe40a75e6aef92d5480eb51c81a3150eb37");
                intent.setAction("interactivefiction.engine.zmachine");
                break;
            case R.id.launchStoryPickGTextView0:
                // Level 9 / V4 / Red Moon (second part of Time and Magik)
                intent.putExtra("path", "/sdcard/myfiction/REDMOON.SNA");
                intent.setAction("interactivefiction.engine.level9");
                break;
            case R.id.launchStoryPickHTextView0:
                // SpirI7wrak.gblorb
                intent.putExtra("datapick", "211219d6f09d7f591f3c95e624753b3519f5d958e84f07aa59b55557d74cd649");
                break;
            case R.id.launchStoryPickITextView0:
                // Fate.z8
                intent.putExtra("datapick", "c11b4cd1ce9d709bea66a33d135b13e8a8937c2a05aa95c07601d083c912ae4c");
                break;
            case R.id.launchStoryPickJTextView0:
                // glkchess.gblorb
                intent.putExtra("datapick", "fad3b3342f8ee93808b284b9e72e5ffcfe443e5d4051c3b02ffe6721664da661");
                break;
            case R.id.launchStoryPickKTextView0:
                // baron_DU.z8
                intent.putExtra("datapick", "3e4d906015d9b787702033662f21fc7bddcbd64959c10b50cdd458fe53359a10");
                break;
            case R.id.launchStoryPickLTextView0:
                // baron_EN.z8
                intent.putExtra("datapick", "6c23fc11932eebb04fa9983ecf54e1062d5bd0ed50fc5923e35e471d9be158e7");
                break;
            case R.id.launchStoryPickMTextView0:
                // luna.gblorb
                intent.putExtra("datapick", "9e2e2945f6f913cdce1ea86a1adfaee79a9ecb10afa2a55d039f8ea1f2cd6f31");
                break;
        }

        // ToDo: A wise app would check that the data file exists before blindly sending it over to Thunderword.

        sendBroadcast(intent);
    }


    /*
    Why close the engine? It can take perhaps 1/2 of a second to unload an engine. From a user interface
    perspective, it may be best to close it when a user closes a screen to pick another story so that
    it is faster in opening the new story. Typical sequence:

     1. User closes Activity of your app to look at menu to pick new game
     2. You close Thunderword with this method as user exits activity
     3. User spends 15 seconds browsing and picking a new story
     4. Story/engine load is requested (this will now seem faster because the close was done back in step 2).

     And on app close, save RAM/battery.

    ToDo: a token from story launch that has to be passed back to close the game, a type of secure mode.
     */
    public void closeThunderwordClick(View view) {
        animateClickedView(view);

        Intent intent = new Intent();
        // If it is stopped, there would be no point to sending a kill command, right?
        // DISABLED: intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        // Inform the Engine Provider who to call back.
        intent.putExtra("sender", BuildConfig.APPLICATION_ID);

        // close command, this always assumes headless and will not prompt player.
        intent.setAction("interactivefiction.enginemeta.close");

        // helper method to pick which app is running the engine.
        intent = setIntentForOutsideEngineProviderApp(intent, 0 /* All Engine apps */);

        sendBroadcast(intent);
    }


    public void primitiveProcessingRemGlkUpdate(JSONObject jsonObject) {
        // raw debug:
        // storyOutputRawTextView0.append(jsonObject.toString() + "\n");

        int onUpdateGeneration = -1;
        try {

            if (jsonObject.has("gen")) {
                onUpdateGeneration = jsonObject.getInt("gen");
                remGlkUpdateGeneration = onUpdateGeneration;
            }

            if (jsonObject.has("windows")) {
                // ToDo: process Glk windows
            }

            if (jsonObject.has("content")) {
                JSONArray storyContentArray = jsonObject.getJSONArray("content");
                for (int topContentArrayIndex = 0; topContentArrayIndex < storyContentArray.length(); topContentArrayIndex++) {
                    JSONObject contentEntry = storyContentArray.getJSONObject(topContentArrayIndex);
                    Log.d("RemoteSimple", "contentEntry " + contentEntry.toString());
                    // Sample JSON from story Counterfeit Monkey version 6:
                    // {"id":24,"clear":true,"text":[{"append":true},{},{},{"content":[{"style":"normal","text":"Can you hear me? >> "}]}]}
                    if (contentEntry.has("clear")) {
                        if (contentEntry.getBoolean("clear")) {
                            clearScreenCount++;
                            storyOutputRawTextView0.setText("[clear screen #" + clearScreenCount + "]\n");
                        }
                    }
                    if (contentEntry.has("text")) {
                        JSONArray contentEntryTextArray = contentEntry.getJSONArray("text");
                        boolean appendMode = false;
                        for (int textForWindowIndex = 0; textForWindowIndex < contentEntryTextArray.length(); textForWindowIndex++) {
                            JSONObject windowContentTextParagraph = contentEntryTextArray.getJSONObject(textForWindowIndex);
                            // RemGlk has "content" field name at various levels
                            if (windowContentTextParagraph.has("content")) {
                                JSONArray singleContentTextFinalArray = windowContentTextParagraph.getJSONArray("content");
                                for (int singleTextIndex = 0; singleTextIndex < singleContentTextFinalArray.length(); singleTextIndex++) {
                                    JSONObject singleContentText = singleContentTextFinalArray.getJSONObject(singleTextIndex);
                                    if (singleContentText.has("text")) {
                                        String contentEntryStoryText = singleContentText.getString("text");
                                        // ToDo: smarter logic about newline, due to prompts and input echo backed.
                                        storyOutputRawTextView0.append(contentEntryStoryText + "\n");
                                    }
                                }

                            }
                            // Blank lines, blank paragraphs. See above "sample JSON from story Counterfeit Monkey".
                            if (windowContentTextParagraph.toString().equals("{}"))
                            {
                                storyOutputRawTextView0.append("\n");
                            }
                            if (windowContentTextParagraph.has("append"))
                            {
                                if (windowContentTextParagraph.getBoolean("append")) {
                                    // ToDo: value goes unchecked, the formating behavior / feature isn't implemented
                                    appendMode = true;
                                }
                            }
                        }
                    }
                }
            }

            if (jsonObject.has("input")) {
                JSONArray storyInputArray = jsonObject.getJSONArray("input");
                for (int inputArrayIndex = 0; inputArrayIndex < storyInputArray.length(); inputArrayIndex++) {
                    JSONObject inputEntry = storyInputArray.getJSONObject(inputArrayIndex);
                    Log.d("RemoteSimple", "inputEntry " + inputEntry.toString());
                    // Sample JSON from story Counterfeit Monkey version 6:
                    // {"id":24,"gen":1,"type":"line","maxlen":256}
                    if (inputEntry.has("type")) {
                        inputForSingleGlkWindow.lineInputMode = inputEntry.getString("type").equals("line");
                        if (inputEntry.has("gen")) {
                            inputForSingleGlkWindow.gen = inputEntry.getInt("gen");
                        }
                        if (inputEntry.has("id")) {
                            inputForSingleGlkWindow.windowId = inputEntry.getInt("id");
                        }

                        remGlkInfoOutputTextView0.append("input: " + inputForSingleGlkWindow.toString() + "\n");
                    }
                }
            }

        } catch (JSONException e) {
            Log.w("RemoteSimple", "Error processing JSON", e);
        }
    }

    /*
    It's labeled "primitive" because some would consider JSONObject/JSONArray field-level parsing
       as non-performant and tedious. Further, it's primitive in the sense that it skips RemGlk
       data from Thunderword about text colors, Glk style hints, Glk windows, Text Grids, images,
       and only seeks out the plain-text of a story in the Text Buffer windows.

       This is where the JSON processing starts, the head of each incoming stanza from RemGlk.
     */
    public void primitiveProcessingRemGlkStanza(JSONObject jsonStanza) {
        try {
            if (jsonStanza.has("type")) {
                // Some of these are non-standard types that Thunderword has added to the standard
                //   RemGlk types.
                switch (jsonStanza.getString("type")) {
                    case "error":
                        break;
                    case "update":
                        primitiveProcessingRemGlkUpdate(jsonStanza);
                        break;
                    case "blorbstatus":
                    case "remglk_exit":
                    case "remglk_status":
                    // ToDo: Warning, this name could change given the CAPS / inconsistent prefix.
                    case "RemGlk_debug":
                    case "remglk_debug":
                    case "EngineError":
                    case "blorberror":
                    case "debug_zcolors":
                        remGlkInfoOutputTextView0.append(jsonStanza.toString() + "\n");
                        break;
                    default:
                        remGlkInfoOutputTextView0.append("UNMATCHED_00: " + jsonStanza.toString() + "\n");
                        break;
                }
            } else {
                remGlkInfoOutputTextView0.append("NO_TYPE_00: " + jsonStanza.toString() + "\n");
            }
        } catch (JSONException e) {
            Log.w("RemoteSimple", "Error processing JSON", e);
        }

        redrawEngineOutput();
    }


    /*
    ================================================================================================
    SECTION: Incoming background events from BroadcastReceivers and Services
    These Events serve to allow thread choices of execution and to determine if the app is on-screen
       If the app is not on screen, this Activity will be closed and the Events will silently
       be dropped as there are no registered receivers.
    */

    /*
    Will switch to Main thread so that screen can be modified.
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventEngineRunningStatus event) {
        thunderWordRemoteEngineStateCode = event.stateCode;
        redrawEngineOutput();
    }

    /*
    Will switch to Main thread so that screen can be modified.
    OPTIMIZE: The JSON processing could be done on an ThreadMode.Async & post to TextView.
       This would be a good idea for very complex JSON processing / layout work.
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventIncomingRemGlkJPayload(EventIncomingRemGlkPayload event) {
        switch (event.format) {
            case 2:   // completely assembled & validated JSON stanza from RemGlk, not line by line.
                try {
                    JSONObject incomingRemGlkStanza = new JSONObject(event.payload);
                    // printJsonObject(incomingRemGlkStanza);
                    primitiveProcessingRemGlkStanza(incomingRemGlkStanza);
                } catch (JSONException e) {
                    Log.w("RemoteSimple", "Error processing JSON from raw RemGlk input source", e);
                }
                break;
        }
    }
}

