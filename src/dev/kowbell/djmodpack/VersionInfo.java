package dev.kowbell.djmodpack;

public class VersionInfo implements Comparable<VersionInfo> {
    public final int major;
    public final int minor;
    public final int patch;
    public final String suffix;
    public final String raw;

    public VersionInfo() {
        major = 0;
        minor = 0;
        patch = 0;
        suffix = "";
        raw = "";
    }

    public VersionInfo(String parse) throws NumberFormatException {
        int tmpMajor = 0;
        int tmpMinor = 0;
        int tmpPatch = 0;
        String tmpSuffix = "";
        String tmpRaw = parse;

        String[] splits = parse.split("(\\.|-)");
        if (splits.length == 0 || splits[0].isEmpty()) {
            System.out.printf("VersionInfo Warning: was told to parse invalid string '%s' - defaulting to zeroes\n", parse);
        } else {
            tmpMajor = Integer.parseInt(splits[0]);

            if (splits.length >= 2)
                tmpMinor = Integer.parseInt(splits[1]);

            if (splits.length >= 3)
                tmpPatch = Integer.parseInt(splits[2]);

            if (splits.length >= 4)
                tmpSuffix = splits[3];
        }


        major = tmpMajor;
        minor = tmpMinor;
        patch = tmpPatch;
        suffix = tmpSuffix;
        raw = tmpRaw;
    }

    public boolean isZero() {
        return major == 0 && minor == 0 && patch == 0;
    }

    @Override
    public int compareTo(VersionInfo o) {
        if (major != o.major)
            return major - o.major;
        else if (minor != o.minor)
            return minor - o.minor;
        else
            return patch - o.patch;
    }

    @Override
    public String toString() {
        String versionString = major + "." + minor + "." + patch;
        if (suffix.length() > 0)
            versionString += "-" + suffix;
        return  versionString;
    }
}
