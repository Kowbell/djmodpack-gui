package dev.kowbell.djmodpack;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

public class LegacyInstallPatcher {

    public static void DoLegacyProfileUpdate() {

    }


//
//    public static JSONObject SearchForMissingModpackProfile(JSONObject profilesJson) {
//        JSONObject allProfiles = profilesJson.getJSONObject("profiles");
//        JSONObject profileJson = null;
//        Iterator<String> key = allProfiles.keys();
//        do {
//            String profileKey = key.next();
//            JSONObject profileObj = allProfiles.getJSONObject(profileKey);
//
//            boolean name = StringUtils.containsIgnoreCase(profileObj.getString("name"), "djmodpack");
//            boolean dir = profileObj.has("gameDir") &&
//                    StringUtils.containsIgnoreCase(profileObj.getString("gameDir"), "djmodpack");
//
//            if (name || dir) {
//                profileJson = profileObj;
//                LegacyInstallPatcher.UpdateProfile(profileKey, profilesJson);
//                break;
//            }
//        } while (key.hasNext());
//
//        return profileJson;
//    }
//
//    public static void UpdateProfile(String profileKey, JSONObject profilesJson) {
//        File profilesJsonFile = LocalMinecraftInstall.GetProfilesJson();
//
//        // don't wanna change the thing we're working on
//        // make sure users don't have that file open
//        JSONObject profilesCopyJson = new JSONObject(profilesJson.toString());
//
//        JSONObject allProfiles = profilesCopyJson.getJSONObject("profiles");
//        JSONObject updatedProfile = allProfiles.getJSONObject(profileKey);
////        updatedProfile.append("lastVersionId", )
////
////
////        allProfiles.remove(profileKey);
////        allProfiles.append(updatedProfile);
////
////
////
////        InputStream profilesStream = new FileInputStream(profilesJsonFile.toString());
////        Scanner scanner = new Scanner(profilesStream);
////
////
////        boolean isInProfilesBlock = false;
////        while (scanner.hasNext()) {
////            String line = scanner.next();
////
////            if (line.contains("\"profiles\"")) {
////                isInProfilesBlock = true;
////
////            }
////        }
//
////        JSONTokener tokener = new JSONTokener(profilesStream);
////        JSONObject profilesJson = new JSONObject(tokener);
////        JSONObject allProfiles = profilesJson.getJSONObject("profiles");
//
//
//
//    }
}
