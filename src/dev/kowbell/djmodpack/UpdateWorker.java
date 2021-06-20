package dev.kowbell.djmodpack;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;


public class UpdateWorker extends SwingWorker<UpdateWorker.EReturnStatus, Void> {

    public enum EUpdateStatus {
        NOT_STARTED,
        INITIALIZING_NEW_PROFILE,
        CHECKING_FOR_UPDATE,
        DOWNLOADING_UPDATE,
        UNPACKING_UPDATE,
        INSTALLING_UPDATE,
        MIGRATING_LEGACY_PROFILE,
        COMPLETE
    }

    public enum EReturnStatus {
        Success,
        DoUpdate_ApplyFailed,
        LegacyMigration_CopyFileFail,
        AmbiguousFuckup
    }

    public EUpdateStatus updateStatus = EUpdateStatus.NOT_STARTED;
    private UpdateUtils.ReleaseInfo latestUpdate;
    private LocalMinecraftInstall minecraftInstall;

    @Override
    protected EReturnStatus doInBackground() throws Exception {

        EReturnStatus returnStatus = EReturnStatus.Success;
        UpdateState(EUpdateStatus.NOT_STARTED);

        try {
            minecraftInstall = LocalMinecraftInstall.GetLocalMinecraftInstall();

            if (minecraftInstall != null) {
                returnStatus = doUpdate();
            } else {
                returnStatus = doFreshInstall();
                if (returnStatus == EReturnStatus.Success &&
                        LocalMinecraftInstall.GetLegacyProfile() != null) {
                    returnStatus = doLegacyMigration();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return EReturnStatus.AmbiguousFuckup;
        }

        UpdateState(EUpdateStatus.COMPLETE);


        return returnStatus;
    }

    private EReturnStatus doUpdate() {
        UpdateState(EUpdateStatus.CHECKING_FOR_UPDATE);
        latestUpdate = UpdateUtils.CheckForUpdate();
        if (latestUpdate == null) {
            return EReturnStatus.Success;
        }

        UpdateState(EUpdateStatus.DOWNLOADING_UPDATE);
        ZipInputStream zip = UpdateUtils.DownloadRelease(latestUpdate, App::UpdateProgressStatic);
        UpdateState(EUpdateStatus.UNPACKING_UPDATE);
        File unpacked = UpdateUtils.UnpackUpdate(zip);


        UpdateState(EUpdateStatus.INSTALLING_UPDATE);
        // Try to apply updates, no matter what delete tmp folder!
        try {
            if (applyUpdates(LocalMinecraftInstall.GetLocalMinecraftInstall(), unpacked) == false)
                return EReturnStatus.DoUpdate_ApplyFailed;
        } catch (Exception e) {
            FileUtils.deleteQuietly(unpacked);
            throw e;
        } finally {
            // todo uhh can this be cleaner?
            FileUtils.deleteQuietly(unpacked);
        }

        return EReturnStatus.Success;
    }

    private EReturnStatus doFreshInstall() {
        UpdateState(EUpdateStatus.INITIALIZING_NEW_PROFILE);
        LocalMinecraftInstall.SetupNewMinecraftInstall();

        EReturnStatus didUpdate = doUpdate();
        if (didUpdate != EReturnStatus.Success)
            return didUpdate;

        return EReturnStatus.Success;
    }

    private EReturnStatus doLegacyMigration() {
        UpdateState(EUpdateStatus.MIGRATING_LEGACY_PROFILE);
        JSONObject legacyProfile = LocalMinecraftInstall.GetLegacyProfile();
        System.out.printf("Got legacy minecraft profile, we're migrating!\n");

        LocalMinecraftInstall localInstall = LocalMinecraftInstall.GetLocalMinecraftInstall();

        String gameDir = legacyProfile.getString("gameDir");

        String[] backupFileStrings = new String[] {
                "options.txt",
                "optionsof.txt",
                "optionsshaders.txt",
                "knownkeys.txt",
                "servers.dat",
                "saves/",
                "screenshots/",
                "journeymap/"
        };

        for (String string : backupFileStrings) {
            File backupFile = new File(gameDir, string);
            if (backupFile.exists() == false)
                continue;

            try {
                if (backupFile.isDirectory()) {
                    FileUtils.copyDirectoryToDirectory(backupFile, localInstall.modpackGameDir);
                    System.out.printf("...Legacy Migration: copy dir '%s'...\n", backupFile);
                } else if (backupFile.isFile()) {
                    FileUtils.copyFileToDirectory(backupFile, localInstall.modpackGameDir);
                    System.out.printf("...Legacy Migration: copy file '%s'...\n", backupFile);
                }
            } catch (Exception e) {
                System.out.printf("EXCEPTION for backup file '%s':\n", backupFile);
                e.printStackTrace();
                return EReturnStatus.LegacyMigration_CopyFileFail;
            }
        }

        return EReturnStatus.Success;
    }

    private boolean applyUpdates(LocalMinecraftInstall install, File extractedUpdate) {
        VersionInfo installedVersion = install.installedVersion;

        File[] extractedFiles = extractedUpdate.listFiles();

        File dotMinecraft = null;
        for (File file : extractedFiles) {
            if (file.getName().endsWith("djmodpack2.minecraft")) {
                dotMinecraft = file;
                break;
            }
        }

        if (dotMinecraft == null) {
            // TODO HACK THIS IS EMBARASSING
            // but fuck, i'm not adding "throws shit" to every fucking method signature
            new FileNotFoundException("Missing djmodpack2.minecraft folder in extracted zip!").printStackTrace();
            return false;
        }


        File baseFolder = null;
        ArrayList<File> updates = new ArrayList<File>();
        File[] dotMinecraftFiles = dotMinecraft.listFiles();

        VersionInfo mostRecentUpdate = null;

        for (File file : dotMinecraftFiles) {
            if (file.getName().endsWith("base")) {
                baseFolder = file;
            } else if (file.getName().contains("update-v")) {
                String updateVersionStr = file.getName().replace("update-v", "");
                VersionInfo updateVersion = new VersionInfo(updateVersionStr);
                if (updateVersion.compareTo(install.installedVersion) > 0) {
                    System.out.printf("Enqueue update folder '%s' to apply to install version %s\n", file, install.installedVersion);
                    updates.add(file);

                    if (mostRecentUpdate == null || updateVersion.compareTo(mostRecentUpdate) > 0)
                        mostRecentUpdate = updateVersion;
                } else {
                    System.out.printf("Skip update folder '%s' for install version %s\n", file, install.installedVersion);
                }
            } else {
                System.out.printf("WARN: ignoring unpacked djmodpack2.minecraft file/dir '%s'\n", file);
            }
        }

        // Install base?
        if (install.installedVersion.isZero()) {
            if (baseFolder == null) {
                // TODO HACK THIS IS EMBARASSING
                // but fuck, i'm not adding "throws shit" to every fucking method signature
                new FileNotFoundException("Missing base/ folder in extracted djmodpack2.minecraft folder!").printStackTrace();
                return false;
            }

            try {
                FileUtils.copyDirectory(baseFolder, install.modpackGameDir);
                System.out.printf("Copied base modpack contents from '%s' -> '%s'\n", baseFolder, install.modpackGameDir);
            } catch (Exception e) {
                System.out.printf("EXCEPTION copying base modpack contents from '%s' -> '%s'\n", baseFolder, install.modpackGameDir);
                e.printStackTrace();
                return false;
            }
        } else {
            System.out.printf("Not installing base - installed version is non-zero '%s'\n", install.installedVersion);
        }

        // Install updates...


        if (updates.size() > 0) {
            for (File update : updates) {
                try {
                    FileUtils.copyDirectory(update, install.modpackGameDir);
                    System.out.printf("Copied update contents from '%s' -> '%s'\n", update, install.modpackGameDir);
                } catch (Exception e) {
                    System.out.printf("EXCEPTION copying update contents from '%s' -> '%s'\n", update, install.modpackGameDir);
                    e.printStackTrace();
                    return false;
                }
            }

            System.out.printf("Applied %d updates to djmodpack2 install (v%s) in folder '%s'\n",
                    updates.size(), install.installedVersion, install.modpackGameDir);

            install.updateVersion(mostRecentUpdate);

        } else {
            System.out.printf("No updates to apply (install version = %s, most recent update = %s)\n", installedVersion, mostRecentUpdate);
        }

        return true;
    }

    @Override
    public void done() {
        App.getInstance().ToggleReady(true, "");
    }

    StringBuilder uiStatusHistory = new StringBuilder();
    private void UpdateState(EUpdateStatus inUpdateStatus) {
        updateStatus = inUpdateStatus;

        if (uiStatusHistory.length() > 0)
            uiStatusHistory.append("done!\n");
        uiStatusHistory.append(inUpdateStatus.toString()/*.replace('_', ' ')*/);
        if (updateStatus != EUpdateStatus.COMPLETE)
            uiStatusHistory.append("... ");

        updateUI();
    }



    public void updateUI() {
        StringBuilder sb = new StringBuilder();

        sb.append(uiStatusHistory);

        if (updateStatus == EUpdateStatus.COMPLETE) {
            sb.append("\n\n\n");
            sb.append("YOU CAN NOW LAUNCH THE GAME :)\n");
        }

        sb.append("\n\n\n");
        if (latestUpdate != null)
             sb.append(latestUpdate.json.getString("body"));

        App.getInstance().PresentText(sb.toString());

        if (updateStatus == EUpdateStatus.COMPLETE) {
            App.getInstance().ToggleReady(true, "");
        } else {
            App.getInstance().ToggleReady(false, updateStatus.toString()/*.replace('_', ' ')*/ + "...");
        }
    }
}
