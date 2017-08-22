package com.wakereality.thunderstrike.storypresentation;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.wakereality.thunderstrike.BuildConfig;
import com.wakereality.thunderstrike.R;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Stephen A. Gutknecht on 8/22/17.
 */

public class UserCommandsToEngine {

    protected static AtomicInteger launchToken = new AtomicInteger(0);

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
                intent.putExtra("path", "/sdcard/myfiction/SagebrushCactus.gblorb");
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
                //   This is a good story to test JSON presentation on, as it has many windows, color options
                //      Glk Style Hints, etc.  The Inform 7 6M62 story source code is available.
                // NOTE: hash is case sensitive, all lowercase
                // Story FlexibleWindowsUser1User2Styles0_R15.ulx
                intent.putExtra("datapick", "aaff415a0eac0df8aa073ee049f7842a609b82306c97c9f007b8e95ce7ab87ea");
                break;
            case R.id.launchStoryPickFTextView0:
                // All Things Devours R3 .z5
                intent.putExtra("datapick", "a0b0569c2f57a975f868242b9a1dfe400a75e6aef92d5480eb51c81a3150eb37");
                intent.setAction("interactivefiction.engine.zmachine");
                break;
            case R.id.launchStoryPickGTextView0:
                // Level 9 / V4 / Red Moon (second part of Time and Magik)
                // intent.putExtra("path", "/sdcard/myfiction/REDMOON.SNA");
                // Level 9 / V3 / Return to Eden / SHA-256 4b0f25bd26abb0e00f224bb72307b0bcc6f28d449590256a98502957979d6587
                // Return to Eden should trigger app to app payload compression as it has a large amount of pixel graphic elements.
                intent.putExtra("path", "/sdcard/myfiction/EDEN.SNA");
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
            case R.id.launchStoryPickNTextView0:
                // The Evil Chicken of Doom by Mel S, year 2002
                intent.putExtra("path", "/sdcard/myfiction/chicken.taf");
                intent.setAction("interactivefiction.engine.adrift");
                break;
            case R.id.launchStoryPickOTextView0:
                // FAIL TESTING, mismatch engine, sending an adrift game to z-machine engine.
                // The Evil Chicken of Doom by Mel S, year 2002
                intent.putExtra("path", "/sdcard/myfiction/chicken.taf");
                intent.setAction("interactivefiction.engine.zmachine");
                break;
            case R.id.launchStoryPickPTextView0:
                // FAIL TESTING, mismatch engine, sending Glulx game to z-machine engine.
                intent.putExtra("path", "/sdcard/myfiction/SagebrushCactus.gblorb");
                intent.setAction("interactivefiction.engine.zmachine");
                break;
        }

        // ToDo: A wise app would check that the data file exists before blindly sending it over to Thunderword.

        view.getContext().sendBroadcast(intent);
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
    public void closeThunderword(Context context, boolean totalAppShutdown) {
        Intent intent = new Intent();
        // If it is stopped, there would be no point to sending a kill command, right?
        // DISABLED: intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        // Inform the Engine Provider who to call back.
        intent.putExtra("sender", BuildConfig.APPLICATION_ID);

        // close command, this always assumes headless and will not prompt player.
        intent.setAction("interactivefiction.enginemeta.close");

        // Secret semaphore to Thunderword to insist on app total shutdown.
        if (totalAppShutdown) {
            intent.putExtra("xyzzy", "Joyce");
        }

        // helper method to pick which app is running the engine.
        intent = setIntentForOutsideEngineProviderApp(intent, 0 /* All Engine apps */);

        context.sendBroadcast(intent);
    }


    public void queryRemoteStoryEngineProviders(Context context) {
        Intent intent = new Intent();
        // Tell Android to start Thunderword app if not already running.
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("interactivefiction.enginemeta.runstory");
        context.sendBroadcast(intent);
    }
}
