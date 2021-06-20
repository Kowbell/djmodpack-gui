package dev.kowbell.djmodpack;

import dev.kowbell.utils.Swatch;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    public static String APIEndpoint = "https://api.github.com/repos/Kowbell/djmodpack2/releases";

    public static ReleaseInfo CheckForUpdate() {

        App.getInstance().ToggleReady(false, "Checking for update...");
        LocalMinecraftInstall minecraftInstall = LocalMinecraftInstall.GetLocalMinecraftInstall();

        ReleaseInfo mostRecentRelease = GetMostRecentReleaseInfo();

        if (mostRecentRelease.version.compareTo(minecraftInstall.installedVersion) > 0) {
            System.out.printf("Update available: %s -> %s\n", minecraftInstall.installedVersion, mostRecentRelease.version);

//            App.getInstance().PresentText(GetUpdatePresentationText(minecraftInstall, mostRecentRelease));
//
//            App.getInstance().ToggleReady(false, "Downloading update...");
//
//            DownloadRelease(mostRecentRelease);
//
//            App.getInstance().ToggleReady(true, "");

            return mostRecentRelease;
        } else {
            System.out.printf("No updated available: local %s == most recent %s\n", minecraftInstall.installedVersion, mostRecentRelease.version);
            return null;
        }
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // System.out.printf("Most recent version: %s\n", mostRecentTagVersion);
        return mostRecentRelease;
    }



    public static ZipInputStream DownloadRelease(ReleaseInfo releaseInfo, DoubleConsumer updateProgress) {
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



        return zipIn;
    }

    public static File UnpackUpdate(ZipInputStream inUpdateZip) {
        LocalMinecraftInstall localMinecraftInstall = LocalMinecraftInstall.GetLocalMinecraftInstall();
        File dotMinecraft = LocalMinecraftInstall.GetDotMinecraftFolder();
        File extractionDir = new File(new File(dotMinecraft, "versions"), "djmodpack2-TMP");

        try {
            if (extractionDir.exists()) {
                System.out.printf("Removing old tmp folder '%s'...\n", extractionDir.toString());

                FileUtils.deleteDirectory(extractionDir);
            }

            if (extractionDir.mkdirs() == false)
                throw new IOException("Failed to make extraction directory '" + extractionDir.toString() + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.printf("Extracting downloaded zip to '%s'...\n", extractionDir.toString());


        // https://stackoverflow.com/a/10851168
        try {

            ZipEntry entry;

            while ((entry = inUpdateZip.getNextEntry()) != null) {


                // Create a file on HDD in the destinationPath directory
                // destinationPath is a "root" folder, where you want to extract your ZIP file
                File entryFile = new File(extractionDir, entry.getName());
                if (entry.isDirectory()) {

                    if (entryFile.exists()) {
                        throw new IOException("Directory " + entryFile.toString());
                    } else {
                        entryFile.mkdirs();
                        System.out.printf("Extraction: Make directory '%s'...\n", entryFile.toString());
                    }

                } else {

                    // Make sure all folders exists (they should, but the safer, the better ;-))
                    if (entryFile.getParentFile() != null && !entryFile.getParentFile().exists()) {
                        entryFile.getParentFile().mkdirs();
                    }

                    // Create file on disk...
                    if (!entryFile.exists()) {
                        entryFile.createNewFile();
                        System.out.printf("Extraction: Copy file '%s'...\n", entryFile);
                    }

                    // and rewrite data from stream
                    OutputStream os = null;
                    try {
                        os = new FileOutputStream(entryFile);
                        IOUtils.copy(inUpdateZip, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inUpdateZip);
        }

        return extractionDir;
    }

    public static void InstallUpdate(ZipInputStream inUpdateZip) {
        // ...


        ArrayList<String> updatesToInstall = new ArrayList<String>();

        LocalMinecraftInstall localMinecraftInstall = LocalMinecraftInstall.GetLocalMinecraftInstall();



        // Enumerate Zip:
        try {
            ZipEntry entry = inUpdateZip.getNextEntry();
            while(entry != null) {
                // https://stackoverflow.com/questions/51285325/copying-entries-from-zipinputstream

                String entryName = entry.getName();
                if (entry.isDirectory() && entryName.startsWith("update")) {
                    String entryVersionStr = entryName.replace("update-v", "");
                    VersionInfo entryVersion = new VersionInfo(entryVersionStr);

                    if (entryVersion.compareTo(localMinecraftInstall.installedVersion) > 0) {
                        System.out.printf("Queuing update %s (from dir name '%s')", entryVersion.toString(), entryName);

                    } else {
                        System.out.printf("Skipping update %s (from dir name '%s') (game is already %s)",
                                entryVersion.toString(), entryName, localMinecraftInstall.installedVersion);
                    }
                }
                inUpdateZip.closeEntry();
                entry = inUpdateZip.getNextEntry();
            }
        } catch (Exception e) {
            System.out.printf("Fuck! Exception while parsing .zip: '%s'", e.getMessage());
        }
        // ...done enumerating zip.


        // Apply updates...
        try {
            inUpdateZip.reset();
            ZipEntry entry = inUpdateZip.getNextEntry();
            while(entry != null) {
                // https://stackoverflow.com/questions/51285325/copying-entries-from-zipinputstream

                String entryName = entry.getName();
                if (entry.isDirectory() && entryName.startsWith("update")) {
                    String entryVersionStr = entryName.replace("update-v", "");
                    VersionInfo entryVersion = new VersionInfo(entryVersionStr);

                    if (entryVersion.compareTo(localMinecraftInstall.installedVersion) > 0) {
                        System.out.printf("Queuing update %s (from dir name '%s')", entryVersion.toString(), entryName);
                        updatesToInstall.add(entryName);
                    } else {
                        System.out.printf("Skipping update %s (from dir name '%s') (game is already %s)",
                                entryVersion.toString(), entryName, localMinecraftInstall.installedVersion);
                    }
                }
                inUpdateZip.closeEntry();
                entry = inUpdateZip.getNextEntry();
            }
        } catch (Exception e) {
            System.out.printf("Fuck! Exception while parsing .zip: '%s'", e.getMessage());
        }
        // ...done applying updates!

    }


}
