package scheme.ui;

import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextField;
import arc.scene.ui.ImageButton.ImageButtonStyle;
import arc.scene.ui.TextField.TextFieldFilter;
import arc.scene.ui.TextField.TextFieldStyle;
import arc.scene.ui.layout.*;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Log;
import arc.util.Scaling;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.game.Rules;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import scheme.ai.GammaAI;
import scheme.ai.NetMinerAI;
import scheme.ai.GammaAI.Updater;
import scheme.tools.DisabledTools;
import scheme.ui.PlayerListFragment.TooltipLocker;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;
import static scheme.ai.GammaAI.Updater.*;
import static scheme.tools.admins.AdminsTools.restricted;

public class HudFragment {

    /** Just a short reference to a variable with a long name. */
    public static final ImageButtonStyle style = Styles.clearNonei, check = Styles.clearNoneTogglei;
    public static final TextFieldStyle input = new TextFieldStyle() {{
        font = Fonts.def;
        fontColor = Color.white;
        selection = Tex.selection;
        cursor = Tex.cursor;
    }};

    public FlipButton mobiles = new FlipButton();
    public FlipButton building = new FlipButton();

    /** PlacementFragment and OverlayMarker. */
    public Element[] block = new Element[3];
    public TextField size;

    public void build(Group parent) {
        Events.run(WorldLoadEvent.class, this::updateBlocks);
        Events.run(UnlockEvent.class, this::updateBlocks);
        parent.fill(cont -> { // Shield Bar
            cont.name = "shieldbar";
            cont.top().left();

            cont.touchable = Touchable.disabled;
            cont.visible(() -> ui.hudfrag.shown && !ui.minimapfrag.shown() && !state.isEditor());

            float dif = Scl.scl() % .5f == 0 ? 0f : 1f; // there are also a lot of magic numbers
            cont.add(new HexBar(() -> units.shield() / units.maxShield, icon -> icon.image(player::icon).scaling(Scaling.bounded).grow().maxWidth(54f))).size(92.2f + dif / 2, 80f).padLeft(18.2f - dif).padTop(mobile ? 69f : 0f);
        });

        parent.fill(cont -> { // Gamma UI
            cont.name = "gammaui";
            cont.top().right();

            cont.visible(() -> ui.hudfrag.shown && !ui.minimapfrag.shown() && ai.ai instanceof GammaAI);

            cont.table(Tex.pane, pad -> {
                pad.defaults().growX();

                new TextSlider(0, 600, 20, 80, value -> bundle.format("gamma.range", GammaAI.range = value)).build(pad).row();
                new TextSlider(0, 110, 10, 100, value -> bundle.format("gamma.speed", GammaAI.speed = value)).build(pad).row();
                pad.table(mode -> {
                    setMove(mode, none);
                    setMove(mode, follow);
                    setMove(mode, cursor);
                    setMove(mode, circle);
                }).row();
                pad.table(mode -> {
                    setBuild(mode, none);
                    setBuild(mode, help);
                    setBuild(mode, destroy);
                    setBuild(mode, repair);
                }).row();
                pad.labelWrap(GammaAI.tooltip).labelAlign(2, 8).pad(8f, 0f, 8f, 0f).width(150f).get().getStyle().fontColor = Color.lightGray;
            }).width(150f).margin(0f).update(pad -> pad.setTranslation(0f, settings.getBool("minimap") ? -Scl.scl(mobile ? 272f : 188f) : 0f)).row();
        });

        parent.fill(cont -> { // Mono UI
            cont.name = "monoui";
            cont.top().right();

            cont.visible(() -> ui.hudfrag.shown && !ui.minimapfrag.shown() && ai.ai instanceof NetMinerAI);

            cont.table(Tex.pane, pad -> {
                pad.table(mode -> Events.run(UnitChangeEvent.class, () -> {
                    mode.clear();
                    mode.button(Icon.line, check, () -> NetMinerAI.priorityItem = null).checked(t -> NetMinerAI.priorityItem == null).size(37.5f);

                    Unit unit = player.unit();
                    if (unit != null) {
                        content.items().each(item -> item.hardness <= unit.type.mineTier && indexer.hasOre(item), item -> {
                            setItem(mode, item);
                            if (mode.getChildren().size % 4 == 0) mode.row();
                        });
                    }

                })).left().row();
                pad.labelWrap(GammaAI.tooltip).labelAlign(2, 8).pad(8f, 0f, 8f, 0f).width(150f).get().getStyle().fontColor = Color.lightGray;
            }).width(150f).margin(0f).update(pad -> pad.setTranslation(0f, settings.getBool("minimap") ? -Scl.scl(mobile ? 272f : 188f) : 0f)).row();
        });

        parent.fill(cont -> { // Wave Approaching
            cont.name = "waveapproaching";
            cont.bottom();

            cont.table(Styles.black6, pad -> {
                pad.add("@approaching.info").labelAlign(Align.center, Align.center).update(label -> label.setColor(Color.white.cpy().lerp(Color.scarlet, Mathf.absin(10f, 1f)))).padRight(6f);
                pad.button(Icon.info, style, approaching::show).grow();
                pad.button(Icon.eyeOffSmall, style, () -> settings.put("approachenabled", false)).grow();
            }).margin(6f).padBottom(mobile ? 350f : 100f).update(pad -> {
                pad.color.a = Mathf.lerpDelta(pad.color.a, Mathf.num(
                        settings.getBool("approachenabled") && state.wavetime > 600f && state.wavetime < 1800f
                ), .1f);
                pad.touchable = pad.color.a > .001f ? Touchable.childrenOnly : Touchable.disabled; // ingeniously
            }).get().color.a(0f); // hide on startup
        });

        parent.fill(cont -> { // Admin Buttons
            cont.name = "adminbuttons";
            cont.bottom().right();
            cont.visible(() -> ui.hudfrag.shown && !ui.minimapfrag.shown());

            cont.table(pad -> {
                pad.defaults().size(63.5f);
                pad.button(Icon.lock, style, m_input::lockMovement);
                pad.button(Icon.admin, style, () -> adminscfg.show());
                pad.button(Icon.logic, style, () -> ai.select());
                pad.row();
                pad.button(Icon.fileText, style, () -> {
                    if (!admins.isRestricted(DisabledTools.RULESETTER) && !admins.unusable()) rulesetter.show();
                });
                pad.button(atlas.drawable("status-overdrive"), style, () -> {
                    if (!admins.isRestricted(DisabledTools.TELEPORT)) admins.teleport(true);
                });
            }).margin(0f).update(pad -> {
                if (block[0] == null) return;
                pad.setTranslation(Scl.scl(0f) - block[0].getWidth(), 0f);
            });
        });
    }






    private void setAction(Table table, Object icon, Runnable listener) {
        table.button(icon instanceof String name ? atlas.drawable("status-" + name) : (Drawable) icon, style, 37f, listener);
    }

    private void setMove(Table table, Updater move) {
        table.button(move.icon, check, () -> {
            GammaAI.move = move;
            ((GammaAI) ai.ai).cache();
        }).checked(t -> GammaAI.move == move).tooltip(move.tooltip()).size(37.5f);
    }

    private void setBuild(Table table, Updater build) {
        table.button(build.icon, check, () -> GammaAI.build = build).checked(t -> GammaAI.build == build).tooltip(build.tooltip()).size(37.5f);
    }

    private void setItem(Table table, Item item) {
        var icon = new TextureRegionDrawable(item.uiIcon); // I hate this
        table.button(icon, check, () -> NetMinerAI.priorityItem = item).checked(t -> NetMinerAI.priorityItem == item).size(37.5f);
    }

    private void updateBlocks() {
        app.post(() -> { // waiting for blockfrag rebuild
            block[0] = ui.hudGroup.find("inputTable");
            block[1] = ui.hudGroup.find("statustable");
            block[2] = ui.hudGroup.find("editor");

            if (block[0] != null) block[0] = block[0].parent.parent.parent;
        });
    }










}
