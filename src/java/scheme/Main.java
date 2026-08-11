package scheme;

import arc.util.Log;
import mindustry.game.Schematics;
import arc.Events;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import scheme.input.SBinding;
import scheme.moded.ModedSchematics;
import scheme.tools.MessageQueue;
import scheme.tools.RainbowTeam;
import scheme.tools.ServerUtils;
import scheme.tools.UpdateContent;
import scheme.ui.MapResizeFix;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class Main extends Mod {

    public Main() {
        maxSchematicSize = 512;

        if (headless || schematics == null) return;

        if (schematics.getClass().getSimpleName().startsWith("Moded")) return;

        assets.load(schematics = m_schematics = new ModedSchematics());
        assets.unload(Schematics.class.getSimpleName());
    }

    @Override
    public void init() {
        if (headless) {
            ServerIntegration.load();
            ServerSide.load();
            log("Server-side loaded. Put this mod on the server for multiplayer admin tools.");
            return;
        }

        ServerIntegration.load();
        ServerSide.load();

        SchemeVars.load();
        MapResizeFix.load();
        MessageQueue.load();
        RainbowTeam.load();
        SBinding.load();
        ui.schematics = schemas;
        ui.listfrag = listfrag;

        units.load();
        builds.load();

        m_settings.apply();

        hudfrag.build(ui.hudGroup);
        listfrag.build(ui.hudGroup);

        control.setInput(m_input.asHandler());
        Events.on(EventType.ClientLoadEvent.class, e -> control.setInput(m_input.asHandler()));
        renderer.addEnvRenderer(0, render::draw);

        if (m_schematics != null && m_schematics.requiresDialog) ui.showOkText("@rename.name", "@rename.text", () -> {});

        UpdateContent.update();
        SchemeUpdater.load();
    }

    public static void log(String info) {
        app.post(() -> Log.infoTag("Scheme", info));
    }

    public static void error(Throwable info) {
        app.post(() -> Log.err("Scheme", info));
    }

    public static void copy(String text) {
        if (text == null) return;
        app.setClipboardText(text);
        ui.showInfoFade("@copied");
    }
}
