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
    UserCommandsToEngine userCommandsToEngine;

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
            PayloadToEngine eventPayloadToEngine = new PayloadToEngine(tempJSONObject.toString(), EngineConst.PAYLOAD_TO_ENGINE_USE_GENERAL_PURPOSE, inputForSingleGlkWindow.windowId, thisGen);
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
            userCommandsToEngine = new UserCommandsToEngine();
            onCreateLayoutSpecific();
            // Setup a local sender for player input to Thunderword app.
            inputSender = new RemGlkInputSender(getApplicationContext());
            // Test that Thunderword app is installed and has desired engines available.
            userCommandsToEngine.queryRemoteStoryEngineProviders(getApplicationContext());
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

    public void launchStoryClick(final View view) {
        //int myLaunchToken = launchToken.incrementAndGet();
        //Log.i("RemoteSimple", "click on launchStoryClick button, launchToken" + myLaunchToken);

        animateClickedView(view);

        setupForNewStoryIncoming();
        userCommandsToEngine.launchStoryClick(view);
    }


    public void closeThunderwordClick(View view) {
        animateClickedView(view);
        userCommandsToEngine.closeThunderword(this, view.getId() == R.id.closeThunderwordAppTextView0);
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

