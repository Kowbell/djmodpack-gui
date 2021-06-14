package dev.kowbell.djmodpack;

public class VersionInfo implements Comparable<VersionInfo> {
    public final int major;
    public final int minor;
    public final String patch;
    public final String raw;

    public static VersionInfo ZERO = new VersionInfo("0");

    public VersionInfo(String parse) throws NumberFormatException {
        int tmpMajor = 0;
        int tmpMinor = 0;
        String tmpPatch = "";
        String tmpRaw = parse;

        String[] splits = parse.split("(\\.|-)");
        if (splits.length == 0)
            System.out.printf("VersionInfo Warning: was told to parse invalid string '%s' - defaulting to zeroes", parse);

        if (splits.length >= 1)
            tmpMajor = Integer.parseInt(splits[0]);

        if (splits.length >= 2)
            tmpMinor = Integer.parseInt(splits[1]);

        if (splits.length >= 3)
            tmpPatch = splits[2];

        major = tmpMajor;
        minor = tmpMinor;
        patch = tmpPatch;
        raw = tmpRaw;
    }

    @Override
    public int compareTo(VersionInfo o) {
        if (major != o.major)
            return major - o.major;
        else if (minor != o.minor)
            return minor - o.minor;
        else
            return patch.compareTo(o.patch);
    }

    @Override
    public String toString() {
        return raw;
    }
}
