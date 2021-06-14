package dev.kowbell.djmodpack;

import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class LocalMinecraftInstall {
    public File dotMinecraft;
    public File launcherProfiles;

    public JSONObject modpackProfile;
    public VersionInfo installedVersion;
    public File modpackGameDir;

    private static String ProfileName = "DJModpack2";

    private LocalMinecraftInstall() {
        modpackProfile = GetModpackProfile();
        modpackGameDir = new File(modpackProfile.getString("gameDir"));

        try {
            installedVersion = new VersionInfo(modpackProfile.getString("lastVersionId"));
        } catch (Exception e) {
            System.out.printf("Could not parse djmodpack profile version from '%s' - defaulting to version zero", modpackProfile.getString("lastVersionId"));
            installedVersion = VersionInfo.ZERO;
        }
    }

    private static LocalMinecraftInstall inst = null;
    public static LocalMinecraftInstall GetLocalMinecraftInstall() {
        if (inst == null)
            inst = new LocalMinecraftInstall();
        return inst;
    }

    public static File GetDotMinecraftFolder() {

        if (SystemUtils.IS_OS_WINDOWS) {
            return GetDotMinecraftFolder_Windows();
        }

        throw new NotImplementedException("Your OS is not supported!");
    }

    private static File GetDotMinecraftFolder_Windows() {
        File dotMinecraft = null;
        try {
            String appDataDir = System.getenv("APPDATA");

            File appData = new File(appDataDir);
            for (File f : appData.listFiles()) {
                if (f.getPath().endsWith(".minecraft")) {
                    dotMinecraft = f;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.printf("Fuck! Caught exception while searching for Minecraft Path: '%s'", e.getMessage());
        }

        return dotMinecraft;
    }

    private static File profilesJson = null;
    public static File GetProfilesJson() {
        if (profilesJson == null) {
            File dotMinecraft = GetDotMinecraftFolder();
            for (File f : dotMinecraft.listFiles()) {
                if (f.getPath().endsWith("launcher_profiles.json")) {
                    profilesJson = f;
                    break;
                }
            }
            if (profilesJson == null)
                System.out.printf("Could not find launcher_profiles.json in '%s'", dotMinecraft.toString());
        }

        return profilesJson;
    }

    public static JSONObject GetModpackProfile() {
        File dotMinecraft = GetDotMinecraftFolder();

        File profilesJsonFile = GetProfilesJson();

        JSONObject profileJson = null;
        try {
            InputStream is = new FileInputStream(profilesJsonFile.toString());
            JSONTokener tokener = new JSONTokener(is);
            // is.close();
            // TODO can't close this or the json with get Stream Closed
            // resource leak!!

            JSONObject profilesJson = new JSONObject(tokener);
            JSONObject allProfiles = profilesJson.getJSONObject("profiles");

            if (allProfiles.has(ProfileName)) {
                profileJson = allProfiles.getJSONObject(ProfileName);
            } else {
                profileJson = SearchForMissingModpackProfile(profilesJson);
            }


        } catch (Exception e) {
            System.out.printf("Fuck! Caught exception opening/reading profiles.json '%s'\n", e);
            e.printStackTrace();
        }

        return profileJson;
    }

    private static JSONObject SearchForMissingModpackProfile(JSONObject profilesJson) {
        JSONObject allProfiles = profilesJson.getJSONObject("profiles");
        JSONObject profileJson = null;
        Iterator<String> key = allProfiles.keys();
        do {
            String profileKey = key.next();
            JSONObject profileObj = allProfiles.getJSONObject(profileKey);

            boolean name = StringUtils.containsIgnoreCase(profileObj.getString("name"), "djmodpack");
            boolean dir = profileObj.has("gameDir") &&
                    StringUtils.containsIgnoreCase(profileObj.getString("gameDir"), "djmodpack");

            if (name || dir) {
                profileJson = profileObj;
//                LegacyInstallPatcher.UpdateProfile(profileKey, profilesJson);
                break;
            }
        } while (key.hasNext());

        return profileJson;
    }
}
