package scheme.ui.dialogs;

import mindustry.gen.Call;
import mindustry.gen.Icon;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class SettingsMenuDialog {

    public SettingsMenuDialog() {
        ui.settings.hidden(this::apply);
        ui.settings.addCategory("@category.mod.name", Icon.book, table -> {
            if (!mobile) table.sliderPref("panspeedmul", 4, 4, 20, value -> value / 4f + "x");


            if (!mobile) table.checkPref("mobilebuttons", true);

//            table.checkPref("crashreports", true); in dev

            table.areaTextPref("subtitle", "I am using Scheme-Zize btw");
        });
    }

    public void apply() {
        m_input.changePanSpeed(settings.getInt("panspeedmul"));

        Call.serverPacketReliable("MySubtitle", settings.getString("subtitle"));
    }

}
