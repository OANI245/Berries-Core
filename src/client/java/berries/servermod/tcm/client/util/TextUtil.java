package berries.servermod.tcm.client.util;

import org.mtr.mod.InitClient;
import org.mtr.mod.data.IGui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextUtil {
    private TextUtil() {
    }

    public static String getCjkParts(String src) {
        return getCjkMatching(src, true);
    }

    public static String getNonCjkParts(String src) {
        return getCjkMatching(src, false);
    }

    public static String getExtraParts(String src) {
        return getExtraMatching(src, true);
    }

    public static String getNonExtraParts(String src) {
        return getExtraMatching(src, false);
    }

    public static String getNonCjkAndExtraParts(String src) {
        String extraParts = getExtraMatching(src, true).trim();
        return getCjkMatching(src, false).trim() + (extraParts.isEmpty() ? "" : "|" + extraParts);
    }

    public static boolean isCjk(String src) {
        return IGui.isCjk(src);
    }

    public static String cycleString(String mtrString) {
        return cycleString(mtrString, 60);
    }

    public static String cycleString(String mtrString, int switchDuration) {
        if(mtrString == null) return "";

        List<String> split = new ArrayList<>(Arrays.asList(mtrString.split("\\|")));
        if(split.isEmpty()) return "";

        return split.get(((int) InitClient.getGameTick() / switchDuration) % split.size());
    }

    private static String getExtraMatching(String src, boolean extra) {
        if (src.contains("||")) {
            return src.split("\\|\\|", 2)[extra ? 1 : 0].trim();
        } else {
            return extra ? "" : src;
        }
    }

    private static String getCjkMatching(String src, boolean isCJK) {
        if (src.contains("||")) src = src.split("\\|\\|", 2)[0];
        String[] stringSplit = src.split("\\|");
        StringBuilder result = new StringBuilder();

        for (final String stringSplitPart : stringSplit) {
            if (IGui.isCjk(stringSplitPart) == isCJK) {
                if (result.length() > 0) result.append(' ');
                result.append(stringSplitPart);
            }
        }
        return result.toString().trim();
    }
}
