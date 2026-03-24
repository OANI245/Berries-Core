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
    int LOWEST_PNB = 249;

    String[] CHANGE_LOGS = new String[] {"新增一个“崩溃页面”，可在游戏崩溃后看到（F3+C不行）", "将未进入世界的设置背景由纯色改为全景图", "修复使用GUI中的按钮执行命令时，命令会输出为聊天信息的BUG"};

    boolean OPEN_TRIALS = false;
}
