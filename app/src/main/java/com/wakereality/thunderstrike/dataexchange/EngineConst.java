package com.wakereality.thunderstrike.dataexchange;

import android.util.SparseArray;

/**
 */

public class EngineConst {
    public static final int ENGINE_UNKNOWN = 0;
    public static final int ENGINE_GLULX_GLULXE = 1;
    public static final int ENGINE_GLULX_GIT = 2;
    public static final int ENGINE_GLULX_DEFAULT = 99;
    public static final int ENGINE_Z_BOCFEL = 100;
    public static final int ENGINE_Z_FROTZ = 101;
    public static final int ENGINE_Z_NITFOL = 102;
    public static final int ENGINE_Z_DEFAULT = 199;
    public static final int ENGINE_TADS_ANY = 200;
    public static final int ENGINE_SCOTT_ADAMS = 300;
    public static final int ENGINE_ADVSYS = 400;
    public static final int ENGINE_LEVEL9 = 500;


    public static final SparseArray<String> engineLibraryName = new SparseArray<>();

    static {
        engineLibraryName.put(ENGINE_GLULX_GLULXE,     "glulxe");
        engineLibraryName.put(ENGINE_GLULX_GIT,        "git");
        engineLibraryName.put(ENGINE_GLULX_DEFAULT,    "git");
        engineLibraryName.put(ENGINE_Z_BOCFEL,         "bocfel");
        engineLibraryName.put(ENGINE_Z_FROTZ,          "frotz");
        engineLibraryName.put(ENGINE_Z_NITFOL,         "nitfol");
        //  NOTE on default choice - nitfol can't take zblorb, only .z files? It presents help info on .zblorb
        engineLibraryName.put(ENGINE_Z_DEFAULT,        "bocfel");
        engineLibraryName.put(ENGINE_TADS_ANY,         "tads");
        engineLibraryName.put(ENGINE_SCOTT_ADAMS,      "scott");
        engineLibraryName.put(ENGINE_ADVSYS,           "advsys");
        engineLibraryName.put(ENGINE_LEVEL9,           "level9");
    }


    public static final String LAUNCH_PARAM_KEY_LAUNCHINTIAITED_WHEN = "launchIntiatedWhen";
    public static final String LAUNCH_PARAM_KEY_LAUNCHCODE = "launchCode";
    public static final String LAUNCH_PARAM_KEY_ENGINECODE = "engineCode";
    public static final String LAUNCH_PARAM_KEY_LAUNCHSTORY = "launch";

    public static final int LAUNCH_PARAM_LAUNCHCODE_AUTO = 1;
    public static final int LAUNCH_PARAM_UNKNOWN_VALUE = -1;
    public static final int LAUNCH_PARAM_LAUNCHSTORY_YES = 1;

    public static final String ENGINE_FRIENDLYNAME_ZMACHINE = "Z-Machine";
    public static final String ENGINE_FRIENDLYNAME_GLULX = "Glulx";
    public static final String ENGINE_FRIENDLYNAME_UNKNOWN_BLORB = "blorb";

    public static final int PAYLOAD_TO_ENGINE_USE_GENERAL_PURPOSE = 0;
    public static final int PAYLOAD_TO_ENGINE_USE_REMGLK_INIT = 1;
    public static final int PAYLOAD_TO_ENGINE_USE_REMGLK_STOP_ENGINE = 2;
}
