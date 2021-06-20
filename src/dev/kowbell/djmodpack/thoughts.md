

## Install flow:
Complete user experience for installing DJModpack should be:
### Pre-Gui

1. User downloads a djmodpack-installer.zip
2. User runs file "1 - (platform) Java 8 Installer.foo"
3. User runs file "djmodpack2-gui.jar"

### This Program

1. This program searches for an existing djmodpack install

#### Autoinstalled DJModpack folder found

1. Download the newest update & extract to tmp
2. Copy any update-vF.F/ contents in order

#### No DJModpack found

**Download Forge first!**

1. Download forge jar
2. Tell user to run it, or see if we can auto-run it?

Use URL https://maven.minecraftforge.net/net/minecraftforge/forge/1.16.5-36.1.25/forge-1.16.5-36.1.25-installer.jar

**...then install**

1. Download the newest update & extract to tmp
2. Make djmodpack.minecraft folder
3. Copy base/ contents
4. Copy all update-vF.F/ contents in order
5. Add to profiles.json

#### Legacy DJModpack folder found

1. Follow the "No modpack found" flow
2. Then copy the legacy modpack's user files to it
3. Update old profiles.json entry ("DEPRECATED")

Make sure to let the player know concisely what will happen. 

