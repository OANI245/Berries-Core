package berries.servermod.tcm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public interface UFEInfo {
    String MOD_ID = "tcm";
    //模组名称 Mod Name
    String MOD_NAME = "Tiancheng Mod";
    //模组版本 Mod Version
    String MOD_VERSION = ((Supplier<String>) () -> {
        try {
            return TCM.class.getMethod("getVersion").invoke(null).toString();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }).get();
    //模组Build版本 Mod Build Version
    int PNB_VERSION = ((Supplier<Integer>) () -> {
        try(InputStream s1 = TCM.class.getClassLoader().getResourceAsStream("pnb.txt");) {
            StringBuilder r = new StringBuilder();
            if (s1 != null) {
                for (byte b : s1.readAllBytes()) {
                    r.append((char) b);
                }
            } else {
                throw new AssertionError("Can't get the version of TCM");
            }
            return Integer.valueOf(r.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }).get();
    //最低支持Build
    int LOWEST_PNB = 1560;

    String[] CHANGE_LOGS = new String[] {
            "修复了几个bug"};

    boolean OPEN_TRIALS = false;
}
