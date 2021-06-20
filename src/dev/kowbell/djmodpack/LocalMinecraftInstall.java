package dev.kowbell.djmodpack;

import com.formdev.flatlaf.json.Json;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

public class LocalMinecraftInstall {
    public File launcherProfiles;

    public JSONObject modpackProfile;
    public VersionInfo installedVersion;
    public File modpackGameDir;

    private static String ProfileKey = "DJModpack2";
    private static String ProfileNamePrefix = "DJModpack2";

    private LocalMinecraftInstall(JSONObject inModpackProfile) {
        modpackProfile = inModpackProfile;
        modpackGameDir = new File(modpackProfile.getString("gameDir"));

        try {
            String name = modpackProfile.getString("name");
            String versionString = name.replace(ProfileNamePrefix + "-", "");
            installedVersion = new VersionInfo(versionString);
        } catch (Exception e) {
            System.out.printf("Could not parse djmodpack profile version from '%s' - defaulting to version zero\n", modpackProfile.getString("lastVersionId"));
            installedVersion = new VersionInfo();
        }
    }

    public void updateVersion(VersionInfo newVersion) {
        installedVersion = newVersion;
        UpdateProfilesJson(GenerateProfileJson(installedVersion));
    }

    public static JSONObject GenerateProfileJson(VersionInfo version) {
        JSONObject profileJson = new JSONObject();
        profileJson.put("lastVersionId", "1.16.5-forge-36.1.25");
        profileJson.put("name", ProfileNamePrefix + "-" + version);
        profileJson.put("gameDir", GetExpectedGameDir().toString());

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        profileJson.put("created", nowAsISO);
        profileJson.put("lastUsed", nowAsISO);

        int ramGb = App.GetRamGb();

        profileJson.put("javaArgs", "-Xmx" + ramGb + "G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M");
        return profileJson;
    }

    public static JSONObject LoadProfilesFileJson() {
        File profilesJsonFile = GetProfilesJson();
        String profilesJsonContents = null;
        try {
            profilesJsonContents = FileUtils.readFileToString(profilesJsonFile, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return new JSONObject(profilesJsonContents);
    }

    public static boolean UpdateProfilesJson(JSONObject profileJson) {
        // Load json data...
        JSONObject profilesFileJson = LoadProfilesFileJson();
        JSONObject allProfiles = profilesFileJson.getJSONObject("profiles");

        allProfiles.put(ProfileKey, profileJson);

        String updatedProfilesJson = profilesFileJson.toString(2);

        // Backup & write...
        try {
            File profilesJsonFile = GetProfilesJson();

            File backupFile = new File(profilesJsonFile.getParent(), "launcher_profiles.json.bak");
            System.out.printf("Backup profiles file '%s' ->  '%s'\n...", profilesJsonFile, backupFile);
            FileUtils.copyFile(profilesJsonFile, backupFile);

            PrintWriter out = new PrintWriter(profilesJsonFile);
            out.write(updatedProfilesJson);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static LocalMinecraftInstall inst = null;
    public static LocalMinecraftInstall GetLocalMinecraftInstall() {
        if (inst == null) {
            JSONObject existingProfile = GetModpackProfile();
            if (existingProfile != null) {
                inst = new LocalMinecraftInstall(existingProfile);
            }
        }
        return inst;
    }

    private static File dotMinecraft = null;
    public static File GetDotMinecraftFolder() {
        if (dotMinecraft == null) {
            if (SystemUtils.IS_OS_WINDOWS) {
                dotMinecraft = GetDotMinecraftFolder_Windows();
            } else if (SystemUtils.IS_OS_MAC) {
                dotMinecraft = GetDotMinecraftFolder_macOS();
            } else {
                throw new NotImplementedException("Your OS is not supported!");
            }

        }

        return dotMinecraft;

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
            e.printStackTrace();
        }

        return dotMinecraft;
    }

    private static File GetDotMinecraftFolder_macOS() {
        File dotMinecraft = null;
        try {
            String appDataDir = System.getProperty("user.home") + "/Library/Application Support";

            File appData = new File(appDataDir);
            for (File f : appData.listFiles()) {
                // NOTE: it isn't '.minecraft' on macos, just 'minecraft'
                if (f.getPath().endsWith("minecraft")) {
                    dotMinecraft = f;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        JSONObject profilesFileJson = LoadProfilesFileJson();
        JSONObject allProfiles = profilesFileJson.getJSONObject("profiles");

        try {
            return allProfiles.getJSONObject(ProfileKey);
        } catch (Exception e) {
            return null;
        }
    }

    public static JSONObject GetLegacyProfile() {
        JSONObject legacyProfileJson = null;

        JSONObject profilesJson = LoadProfilesFileJson();
        JSONObject allProfiles = profilesJson.getJSONObject("profiles");

        Iterator<String> key = allProfiles.keys();
        do {
            String profileKey = key.next();
            if (profileKey.equals(ProfileKey))
                continue;

            JSONObject profileObj = allProfiles.getJSONObject(profileKey);

            boolean name = StringUtils.containsIgnoreCase(profileObj.getString("name"), "djmodpack");
            boolean dir = profileObj.has("gameDir") &&
                    StringUtils.containsIgnoreCase(profileObj.getString("gameDir"), "djmodpack");

            if (name || dir) {
                legacyProfileJson = profileObj;
                break;
            }
        } while (key.hasNext());

        return legacyProfileJson;
    }

    public static File GetExpectedGameDir() {
        File dotMinecraft = GetDotMinecraftFolder();
        File versions = new File(dotMinecraft, "versions");
        return new File(versions, "djmodpack2.minecraft");
    }

    public static LocalMinecraftInstall SetupNewMinecraftInstall() {
        JSONObject versionZeroProfile = LocalMinecraftInstall.GenerateProfileJson(new VersionInfo(""));
        LocalMinecraftInstall.UpdateProfilesJson(versionZeroProfile);
        return GetLocalMinecraftInstall();
    }
}
