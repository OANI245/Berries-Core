import MOD_INFO_DATA from "./info_data.js";
import {applyTheme, argbFromHex, themeFromSourceColor} from "./bundle.js";

const theme = themeFromSourceColor(argbFromHex("#f38400"), []);
const systemDark = window.matchMedia("(prefers-color-scheme: dark)");

function load(bl) {
    if (bl) {
        MOD_INFO_DATA.fetchData();
    }
    setTimeout(() => {
        jQuery("#layer1").fadeIn(1600);
        jQuery("#layer2").fadeIn(1600);
        jQuery("#main").fadeIn(800);

        let isDark = systemDark.matches;
        applyTheme(theme, {target: document.body, dark: isDark});
        systemDark.addEventListener("change",
            e =>
                applyTheme(theme, {target: document.body, dark: e.matches})
        );
    }, 100)
}

function download() {
    window.location.replace("/download");
}

document.body.onload = () => load(window.location.pathname === "/");
document.getElementById("download-button").onclick = download;
export default {load, download};