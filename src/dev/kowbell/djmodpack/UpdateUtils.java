package dev.kowbell.djmodpack;

import dev.kowbell.utils.Swatch;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UpdateUtils {

    public static class ReleaseInfo implements Comparable<ReleaseInfo>  {
        public JSONObject json;
        public VersionInfo version;
        public int size;
        public String downloadURL;

        public ReleaseInfo(JSONObject object) {
            json = object;
            version = new VersionInfo(object.getString("tag_name"));

            JSONArray assets = object.getJSONArray("assets");
            for (Object o : assets) {
                JSONObject jo = (JSONObject)o;
                if (jo.getString("name").endsWith(".zip") == false)
                    continue;
                size = jo.getInt("size");
                downloadURL = jo.getString("browser_download_url");
            }
        }

        @Override
        public int compareTo(ReleaseInfo o) {
            return version.compareTo(o.version);
        }
    }

    public static String APIEndpoint = "https://api.github.com/repos/Kowbell/djmodpack/releases";

    public static ReleaseInfo CheckForUpdate() {

        App.getInstance().ToggleReady(false, "Checking for update...");
        LocalMinecraftInstall minecraftInstall = LocalMinecraftInstall.GetLocalMinecraftInstall();

        ReleaseInfo mostRecentRelease = GetMostRecentReleaseInfo();

        if (mostRecentRelease.version.compareTo(minecraftInstall.installedVersion) > 0) {
            System.out.println("Update available!");

//            App.getInstance().PresentText(GetUpdatePresentationText(minecraftInstall, mostRecentRelease));
//
//            App.getInstance().ToggleReady(false, "Downloading update...");
//
//            DownloadRelease(mostRecentRelease);
//
//            App.getInstance().ToggleReady(true, "");

            return mostRecentRelease;
        } else {
            return null;
        }
    }


    public static String GetUpdatePresentationText(LocalMinecraftInstall localMinecraftInstall, ReleaseInfo releaseInfo) {
        StringBuilder sb = new StringBuilder();

        sb.append("New update available (").append(localMinecraftInstall.installedVersion).append(" -> ").append(releaseInfo.version).append(")");
        sb.append("\n\n\n");
        sb.append(releaseInfo.json.getString("body"));

        return sb.toString();
    }

    /**
     * see https://docs.github.com/en/rest/reference/repos#releases
     * @return
     */
    private static ReleaseInfo GetMostRecentReleaseInfo() {
        // do https request to get the releases data as json
        JSONArray r = null;
        try {
            URI uri = new URI(APIEndpoint);
            JSONTokener tokener = new JSONTokener(uri.toURL().openStream());
            r = new JSONArray(tokener);
        } catch (Exception e) {
            System.out.printf("Fuck! Caught exception: '%s'", e.getMessage());
        }
        // got json response

        // find the most recent update
        ReleaseInfo mostRecentRelease = null;
        for (Object o : r) {
            if (o instanceof JSONObject == false) {
                continue;
            }

            JSONObject jsonObj = (JSONObject)o;
            try {
                ReleaseInfo release = new ReleaseInfo(jsonObj);
                if (mostRecentRelease == null || release.compareTo(mostRecentRelease) > 0) {
                    mostRecentRelease = release;
                }
            } catch (JSONException e) {
                System.out.printf("Caught JSON exception '%s' parsing JSON '%s'\n",
                        e.getMessage(), jsonObj.toString(1));
            }
        }

        // System.out.printf("Most recent version: %s\n", mostRecentTagVersion);
        return mostRecentRelease;
    }



    public static void DownloadRelease(ReleaseInfo releaseInfo, DoubleConsumer updateProgress) {
        // download
        updateProgress.accept(0);
        ZipInputStream zipIn = null;
        try {
            URL updateURL = new URL(releaseInfo.downloadURL);
            updateProgress.accept(25);

            Swatch.StartTimer("OpenConnection");
            HttpURLConnection connection = (HttpURLConnection) updateURL.openConnection();
            Swatch.StopTimer("OpenConnection");

            connection.setRequestMethod("GET");
            updateProgress.accept(50);

            Swatch.StartTimer("GetInputStream");
            InputStream in = connection.getInputStream();
            Swatch.StopTimer("GetInputStream");

            Swatch.StartTimer("New ZipInputStream");
            zipIn = new ZipInputStream(in);
            Swatch.StopTimer("New ZipInputStream");

            updateProgress.accept(80);


        } catch (Exception e) {
            System.out.printf("Fuck! Exception while downloading .zip: '%s'", e.getMessage());
        }
        updateProgress.accept(100);
        // ...done downloading

        System.out.println("Downloaded Release Zip:");

//        try {
//            ZipEntry entry = zipIn.getNextEntry();
//            while(entry != null) {
//                // https://stackoverflow.com/questions/51285325/copying-entries-from-zipinputstream
//                System.out.println(entry.getName());
////                if (!entry.isDirectory()) {
////                    // if the entry is a file, extracts it
////                    System.out.println("===File===");
////
////                } else {
////                    System.out.println("===Directory===");
////                }
//                zipIn.closeEntry();
//                entry = zipIn.getNextEntry();
//
//            }
//        } catch (Exception e) {
//            System.out.printf("Fuck! Exception while parsing .zip: '%s'", e.getMessage());
//        }

    }


}
