package scheme;

import arc.files.Fi;
import arc.util.Http;
import arc.util.Http.HttpResponse;
import arc.util.Time;
import arc.util.io.Streams;
import arc.util.serialization.Jval;
import mindustry.mod.Mods.LoadedMod;

import static arc.Core.*;
import static mindustry.Vars.*;

/** Checks and installs releases published in the project's GitHub repository. */
public final class SchemeUpdater {
    public static final String repo = "MikhaelCat/Scheme-Zize";
    private static final String latestUrl = "https://api.github.com/repos/" + repo + "/releases/latest";

    private static LoadedMod mod;
    private static String download;
    private static float progress;

    private SchemeUpdater() {}

    public static void load() {
        mod = mods.getMod(Main.class);
        if (mod == null) return;
        mod.meta.repo = repo;
        mod.setRepo(repo);

        // Do not block startup; a missing release or offline GitHub must never break the client.
        Time.run(120f, SchemeUpdater::check);
    }

    public static void check() {
        if (mod == null || headless) return;
        Http.get(latestUrl, response -> {
            Jval json = Jval.read(response.getResultAsString());
            String latest = json.getString("tag_name", "").replaceFirst("^[vV]", "");
            if (latest.isEmpty() || !isNewer(latest, mod.meta.version)) return;
            for (Jval asset : json.get("assets").asArray()) {
                String name = asset.getString("name", "").toLowerCase();
                if (name.endsWith(".zip") || name.endsWith(".jar")) {
                    download = asset.getString("browser_download_url", "");
                    break;
                }
            }
            if (download != null && !download.isEmpty()) {
                ui.showCustomConfirm("@updater.name",
                    bundle.format("updater.info", mod.meta.version, latest),
                    "@updater.load", "@ok", SchemeUpdater::update, () -> {});
            }
        }, error -> Main.log("Update check skipped: " + error.getMessage()));
    }

    private static boolean isNewer(String latest, String current) {
        try {
            String[] l = latest.split("\\."), c = current.split("\\.");
            for (int i = 0; i < Math.max(l.length, c.length); i++) {
                int lv = i < l.length ? Integer.parseInt(l[i].replaceAll("\\D.*", "")) : 0;
                int cv = i < c.length ? Integer.parseInt(c[i].replaceAll("\\D.*", "")) : 0;
                if (lv != cv) return lv > cv;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private static void update() {
        ui.loadfrag.show("@downloading");
        ui.loadfrag.setProgress(() -> progress);
        Http.get(download, SchemeUpdater::handle, Main::error);
    }

    private static void handle(HttpResponse response) {
        try {
            Fi file = tmpDirectory.child("scheme-zize-update.zip");
            Streams.copyProgress(response.getResultAsStream(), file.write(false), response.getContentLength(), 4096,
                value -> progress = value);
            mods.importMod(file).setRepo(repo);
            file.delete();
            app.post(ui.loadfrag::hide);
            ui.showInfoOnHidden("@mods.reloadexit", app::exit);
        } catch (Throwable error) {
            Main.error(error);
            app.post(ui.loadfrag::hide);
        }
    }
}
