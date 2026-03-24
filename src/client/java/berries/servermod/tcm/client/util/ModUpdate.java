package berries.servermod.tcm.client.util;

import berries.servermod.tcm.TCM;
import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.TCMClient;
import berries.servermod.tcm.client.screen.TCMMessageAndOKScreen;
import berries.servermod.tcm.client.screen.overlay.PrepOverlay;
import berries.servermod.tcm.util.TCMComponent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ModUpdate {
    private static final int BUFFER_SIZE = 2048;

    public static AtomicBoolean screens(boolean bl) {
        if (!bl) {
            Config.INSTANCE.isInstalling = true;
            Config.INSTANCE.saveConfig();
        }

        Minecraft mc = Minecraft.getInstance();
        AtomicBoolean shouldClose = new AtomicBoolean(false);
        PrepOverlay overlay = new PrepOverlay(
                mc, shouldClose,
                (o) -> {
                },
                true, false
        );
        mc.execute(() -> {
            if (!(mc.getOverlay() instanceof PrepOverlay)) {
                mc.setOverlay(overlay);
            }
        });

        ExecutorService ts1 = Executors.newSingleThreadExecutor();

        //try (ts1) {
            ts1.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple<EndCode, String> result = startUpdate((text) -> {
                    overlay.messageText = text;
                });

                if (result.getA() != EndCode.SUCCESS) {
                    TCM.LOGGER.error("Download is failed, result: {code: " + result.getA().name() + ", reason: \"" + result.getB() + "\"}");
                }

                mc.tell(() -> {
                    if (bl) {
                        mc.setScreen(new TCMMessageAndOKScreen(
                                TCMComponent.text("测试结束"),
                                TCMComponent.text("Back"),
                                (button) -> mc.setScreen(null),
                                true,
                                true
                        ));
                    } else {
                        if (result.getA() == EndCode.SUCCESS) {
                            mc.setScreen(new TCMMessageAndOKScreen(
                                    TCMComponent.translatable("gui.tcm.prep.update.success"),
                                    TCMComponent.translatable("gui.tcm.prep.update.close_button"),
                                    (button) -> {
                                        mc.stop();
                                    },
                                    true,
                                    false
                            ));
                        } else {
                            Config.INSTANCE.isInstalling = false;
                            Config.INSTANCE.saveConfig();
                            mc.setScreen(new TCMMessageAndOKScreen(
                                    TCMComponent.translatable("gui.tcm.prep.update.failed", result.getA() == EndCode.CONNECTION_TIMEOUT ? "Connection Timed Out" : result.getB()),
                                    TCMComponent.translatable("gui.tcm.prep.update.back_button"),
                                    (button) -> {
                                        mc.setScreen(null);
                                    },
                                    true,
                                    true
                            ));
                        }
                    }
                });

                shouldClose.set(true);
                ts1.shutdown();
            });
        //}
        return shouldClose;
    }

    public static Tuple<EndCode, String> startUpdate(Consumer<Component> event) {
        event.accept(TCMComponent.translatable("gui.tcm.prep.update.starting"));
        String url = "http://" + TCMClient.syncDirectionIp + "/download";
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .header("User-Agent", "Tiancheng Mod Updater")
                    .build();

            HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());

            if (resp.statusCode() != 200) {
                return new Tuple<>(EndCode.CONNECTION_ERROR, "Status code: " + resp.statusCode());
            }

            String fileName = getFileName(client);
            Path path = FabricLoader.getInstance().getConfigDir().getParent().resolve("mods/" + fileName + ".download");
            Path path2 = FabricLoader.getInstance().getConfigDir().getParent().resolve("mods/" + fileName);

            File file = new File(path.toUri());
            File renameToFile  = new File(path2.toUri());
            if (renameToFile.exists()) {
                path2 = FabricLoader.getInstance().getConfigDir().getParent().resolve("mods/" + "second-" + fileName);
            }

            boolean downloadTimeOut;
            int timeOutCount = 0;

            do {
                try (InputStream stream = resp.body()) {
                    long contentLength = resp.headers()
                            .firstValueAsLong("Content-Length")
                            .orElse(0L);

                    if (contentLength > 0L) {
                        download(stream, path, contentLength, (progress) -> event.accept(TCMComponent.translatable("gui.tcm.prep.update.progress", String.valueOf((int) progress))));
                    } else {
                        return new Tuple<>(EndCode.FILE_EMPTY, "Source file is empty.");
                    }
                    downloadTimeOut = false;
                } catch (InterruptedException ie) {
                    if (timeOutCount > 5) {
                        return new Tuple<>(EndCode.CONNECTION_TIMEOUT, "Download timed out.");
                    }
                    downloadTimeOut = true;
                    timeOutCount++;
                } catch (IOException e) {
                    return new Tuple<>(EndCode.DOWNLOAD_ERROR, e.getMessage());
                }
            } while (downloadTimeOut);

            file.renameTo(renameToFile);

            //client.close();
        } catch (URISyntaxException e) {
            return new Tuple<>(EndCode.URL_ERROR, e.getMessage());
        } catch (IOException e) {
            return new Tuple<>(EndCode.CONNECTION_ERROR, e.getMessage());
        } catch (InterruptedException e) {
            return new Tuple<>(EndCode.CONNECTION_TIMEOUT, e.getMessage());
        }

        event.accept(TCMComponent.translatable("gui.tcm.prep.update.install"));

        File tmpJava = new File(Minecraft.getInstance().gameDirectory.getPath() + File.separator + "Uninstaller.java");
        if (tmpJava.exists()) {
            tmpJava.delete();
        }

        try {
            tmpJava.createNewFile();
            try (FileWriter output = new FileWriter(tmpJava)) {
                output.write("import java.io.File; " +
                        "public class Uninstaller { " +
                        "public static void main(String[] args) { " +
                        "long createdTime = System.currentTimeMillis();" +
                        "File file = new File(args[0].replaceAll(\"\\\\?\", \" \").trim());" +
                        "if (!file.exists()) { System.exit(0); }" +
                        "while (!file.delete()) {" +
                        "if (System.currentTimeMillis() - createdTime > 60000) { System.out.println(\"ERROR: Timed Out\"); System.exit(-1); }" +
                        "try { Thread.sleep(1000); } catch (Exception e) { e.printStackTrace(); }" +
                        "System.gc();" +
                        "}" +
                        "}" +
                        "}");

                output.flush();
            }
            Process process;
            process = new ProcessBuilder()
                    .command("javac", "Uninstaller.java")
                    .directory(new File(Minecraft.getInstance().gameDirectory.getPath()))
                    .start();
            int ec = process.waitFor();
            byte[] bytes = process.getInputStream().readAllBytes();
            System.out.println(bytes);
            if (ec != 0) {
                throw new IOException("Failed to create file.");
            }

            tmpJava.delete();
        } catch (Exception e) {
            return new Tuple<>(EndCode.INSTALLING_ERROR, e.getMessage());
        }

        return new Tuple<>(EndCode.SUCCESS, "");
    }

    public static void download(InputStream input, Path path, long length, Consumer<Integer> event) throws IOException, InterruptedException {
        File file = new File(path.toUri());
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("Failed to delete file.");
            }
        }

        try (OutputStream output = Files.newOutputStream(path)) {
            long timeOut = 10000;

            byte[] buffer = new byte[BUFFER_SIZE];
            long readBytes = 0L;
            int read;

            ExecutorService executor = Executors.newSingleThreadExecutor();
            CompletionService<Object> completionService = new ExecutorCompletionService<>(executor);

            while ((read = input.read(buffer)) != -1) {
                final int finalRead = read;
                Future<Object> ft = completionService.submit(() -> {
                    output.write(buffer, 0, finalRead);
                    return null;
                });

                try {
                    completionService.poll(timeOut, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    ft.cancel(true);
                    throw e;
                }
                readBytes += read;

                double progress = (double) readBytes / length * 100;
                event.accept((int) Math.floor(progress));
            }

            //executor.close();
        } catch (Exception e) {
            file.delete();
            throw e;
        }
    }

    public static String getFileName(HttpClient client) throws URISyntaxException, IOException, InterruptedException {
        String url = "http://" + TCMClient.syncDirectionIp + "/info";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .header("User-Agent", "Tiancheng Mod Updater")
                .build();

        HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resp.body()))) {
            JsonElement element = JsonParser.parseReader(reader);
            JsonObject root = element.getAsJsonObject();
            if (root.has("mod_file_path")) {
                return root.get("mod_file_path").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "default.jar";
    }

    public enum EndCode {
        SUCCESS(0),
        CONNECTION_TIMEOUT(-1),
        RESPONSE_CREATE_ERROR(-2),
        CONNECTION_ERROR(-3),
        FILE_EMPTY(-4),
        URL_ERROR(-5),
        DOWNLOAD_ERROR(-6),
        INSTALLING_ERROR(-7),
        UNKNOWN_ERROR(-10);

        private final int code;

        private EndCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
