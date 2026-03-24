import FetchData from "./fetch.js";
import DEFINES from "./defines.js";

let loaded = false;

const MOD_INFO_DATA = new FetchData(
    DEFINES.url + "info",
    100000,
    true,
    () => !loaded,
    (result) => {
        const {mod_name: modName, mod_version: modVersion, mod_file_path: modFilePath} = result;

        try {
            let elementModName = document.getElementById("mod-name");
            let elementModVersion = document.getElementById("mod-version");
            let elementDownloadButton = document.getElementById("download-button");

            if (modName != null) {
                elementModName.innerHTML = modName.toString();
            }
            if (modVersion != null) {
                elementModVersion.innerHTML = "Version: " + modVersion.toString();
            }
            elementDownloadButton.disabled = (modFilePath == null);
        } catch (e) {
            console.error(e);
        }

        loaded = true;
    }
);

export default MOD_INFO_DATA;