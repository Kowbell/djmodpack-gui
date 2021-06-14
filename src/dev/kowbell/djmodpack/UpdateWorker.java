package dev.kowbell.djmodpack;

import javax.swing.*;



public class UpdateWorker extends SwingWorker<Integer, Void> {



    @Override
    protected Integer doInBackground() throws Exception {
        // TODO this should be more state-machiney


        UpdateUtils.ReleaseInfo update = UpdateUtils.CheckForUpdate();
        if (update == null) {
            return 0;
        }

        LocalMinecraftInstall minecraftInstall = LocalMinecraftInstall.GetLocalMinecraftInstall();
        App.getInstance().PresentText(UpdateUtils.GetUpdatePresentationText(minecraftInstall, update));
        App.getInstance().ToggleReady(false, "Downloading update...");

        UpdateUtils.DownloadRelease(update, App::UpdateProgressStatic );



        return 0;
    }

    @Override
    public void done() {
        App.getInstance().ToggleReady(true, "");
    }
}
