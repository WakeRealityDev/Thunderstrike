package com.wakereality.thunderstrike;

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

import com.wakereality.thunderstrike.dataexchange.EventEngineRunningStatus;
import com.wakereality.thunderstrike.dataexchange.EventIncomingRemGlkPayload;
import com.wakereality.thunderstrike.dataexchange.PayloadToEngine;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Apache 2.0 license on this source file
 * (c) Copyright 2017, Stephen A Gutknecht, All Rights Reserved
 *
 * This class is a demonstration of basic Input/Output to Thunderword app using RemGlk JSON
 *   and various engine control BroadcastReceivers.
 *
 * This is a demonstration app for software developers to make their own client apps to work
 *   in partnership with the main Thunderword app.
 *
 * Note: this source code does not honor line-length limits, reformat if you so desire.
 *
 * The JSON parsing in this example is primitive and uses built-in JSONObject/JSONArray structures
 *   Anyone is free to use alternate libraries for more sophisticated mapping of the RemGlk JSON.
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

    protected void postRemGlkInputFromPlayer(String inputText) {
        JSONObject tempJSONObject = new JSONObject();
        int thisGen = inputForSingleGlkWindow.gen;
        try {
            // Tip: RemGlk documentation describes these fields and their format
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
            PayloadToEngine eventPayloadToEngine = new PayloadToEngine(tempJSONObject.toString());
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
            setupThunderClapRemoteExtras();
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
                        // RemGlk will not like empty input
                        postRemGlkInputFromPlayer(latestText.toString());
                        editable.clear();
                    }
                } else {
                    // ToDo: there is no conversion of RemGlk codes for special keys.
                    if (latestText.toString().length() > 0) {
                        postRemGlkInputFromPlayer(latestText.toString());
                        editable.clear();
                    }
                }
            }
        });
    }

    public void setupThunderClapRemoteExtras() {
        inputSender = new RemGlkInputSender(getApplicationContext());
    }


    protected int redrawCount = 0;
    protected int thunderWordRemoteEngineStateCode = -1;
    protected int remGlkUpdateGeneration = -1;

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
        redrawEngineOutput();
    }

    public void launchStoryClick(View view) {
        Log.i("RemoteSimple", "click on launchStoryClick button");
        // clear output on screen
        storyOutputRawTextView0.setText("");
        remGlkInfoOutputTextView0.setText("");
        // Reset input generation
        inputLastGenSend = -1;
        remGlkUpdateGeneration = -1;

        Intent intent = new Intent();
        // Tell Android to start Thunderword app if not already running.
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        // Tell Thunderword Glulx story data file, it will pick default engine in absence
        //   of additional launch parameters.
        intent.setAction("interactivefiction.engine.glulx");
        // Readable/accessible Data file for the story.
        // Future: Alternately, your app can be content provider for secure exchange of data file.
        switch (view.getId()) {
            case R.id.launchStoryPickATextView0:
                intent.putExtra("path", "/sdcard/storyGames0/rover.gblorb");
                break;
            case R.id.launchStoryPickBTextView0:
                intent.putExtra("path", "/sdcard/storyGames_TechTest0/CounterfeitMonkey_release6.gblorb");
                break;
        }
        // Tell Thunderword to be headless, no screen activity & only RemGlk data exchange.
        intent.putExtra("activity", 0);
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
                    // sample from story Counterfeit Monkey version 6:
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
                            // Blank lines, blank paragraphs
                            if (windowContentTextParagraph.toString().equals("{}"))
                            {
                                storyOutputRawTextView0.append("\n");
                            }
                            if (windowContentTextParagraph.has("append"))
                            {
                                if (windowContentTextParagraph.getBoolean("append")) {
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
                    // sample from story Counterfeit Monkey version 6:
                    // {"id":24,"gen":1,"type":"line","maxlen":256}
                    if (inputEntry.has("type")) {
                        inputForSingleGlkWindow.lineInputMode = false;
                        if (inputEntry.getString("type").equals("line")) {
                            inputForSingleGlkWindow.lineInputMode = true;
                        }
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

    public void primitiveProcessingRemGlkStanza(JSONObject jsonStanza) {
        try {
            if (jsonStanza.has("type")) {
                switch (jsonStanza.getString("type")) {
                    case "error":
                        break;
                    case "update":
                        primitiveProcessingRemGlkUpdate(jsonStanza);
                        break;
                    case "blorbstatus":
                    case "remglk_exit":
                    case "remglk_status":
                    // ToDo: Warning, this name could change given the inconsistent prefix.
                    case "RemGlk_debug":
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
    ===============================================================================================
    SECTION: Incoming background events from BroadcastReceivers and Services
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
                    e.printStackTrace();
                }
                break;
        }
    }
}

