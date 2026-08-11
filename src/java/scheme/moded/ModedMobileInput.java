package scheme.moded;

import arc.Core;
import arc.math.Angles;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Blocks;
import mindustry.core.World;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Schematic;
import mindustry.gen.Mechc;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.input.*;
import mindustry.input.Placement.NormalizeResult;
import mindustry.world.blocks.power.PowerNode;
import mi2u.input.InputOverwrite;
import scheme.ai.GammaAI;
import scheme.tools.DisabledTools;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.input.PlaceMode.*;
import static scheme.SchemeVars.*;

/** Last update - Feb 10, 2023 */
public class ModedMobileInput extends MobileInput implements ModedInputHandler, InputOverwrite {

    public boolean using, movementLocked, lastTouched, shootingLocked;

    public Player observed;

    public boolean ctrlBoost, mi2uBoost;
    public boolean ctrlShoot, mi2uShoot;
    public Vec2 shootXY = new Vec2();
    public boolean ctrlMove;
    public Vec2 mi2uMove = new Vec2();

    private boolean isRelease() {
        return lastTouched && !input.isTouched(0);
    }

    private boolean isTap() {
        return !lastTouched && input.isTouched(0);
    }



    @Override
    public void buildPlacementUI(Table table) {
        super.buildPlacementUI(table);

        var button = table.getChildren().get(table.getChildren().size - 1);
        button.clicked(() -> {
            if (m_schematics.isCursed(selectPlans) && !admins.isRestricted(DisabledTools.FLUSH)) admins.flush(selectPlans);
        });

        int size = button.getListeners().size;
        button.getListeners().swap(size - 1, size - 2);
    }

    @Override
    public void drawTop() {
        if (mode == schematicSelect) {
            drawSelection(lineStartX, lineStartY, lastLineX, lastLineY, maxSchematicSize);
            drawSize(lineStartX, lineStartY, lastLineX, lastLineY, maxSchematicSize);
        } else if (mode == breaking && lineMode)
            drawSize(lineStartX, lineStartY, tileX(), tileY(), maxSchematicSize);
        else if (mode == rebuildSelect)
            drawRebuildSelection(lineStartX, lineStartY, lastLineX, lastLineY);

        drawCommanded();


    }



    @Override
    public void update() {
        super.update();

        if (locked()) return;

        if (observed != null) {
            if (observed.unit() == null) return;
            camera.position.set(observed.unit()); // idk why, but unit moves smoother
            if (input.isTouched(0) && !scene.hasMouse()) observed = null;
        }




        buildInput();
        if (movementLocked) {
            if (player.unit() == null) return;
            drawLocked(player.unit().x, player.unit().y);
        }

        Unit unit = player.unit();
        if (ctrlBoost) player.boosting = mi2uBoost;
        if (ctrlShoot && unit != null) {
            player.shooting = mi2uShoot && !(unit instanceof Mechc && unit.isFlying());
            if (player.shooting) {
                unit.rotation(Angles.moveToward(unit.rotation(), Angles.angle(shootXY.x - unit.x, shootXY.y - unit.y), unit.type.rotateSpeed * unit.speedMultiplier() * Time.delta * 1.5f));
                player.mouseX = shootXY.x;
                player.mouseY = shootXY.y;
                unit.aim(player.mouseX, player.mouseY);
                unit.controlWeapons(true, player.shooting);
            }
        }
        if (ctrlMove && unit != null) unit.movePref(mi2uMove);
    }

    @Override
    protected void updateMovement(Unit unit) {
        if (ai.ai != null && !input.isTouched()) {
            if (!movementLocked) camera.position.set(unit.x, unit.y);
            ai.update();
        } else if (!movementLocked) super.updateMovement(unit);

        if (shootingLocked) {
            unit.aimLook(player.mouseX, player.mouseY);
            unit.controlWeapons(true, false);
            player.shooting = unit.isShooting = false;
        }
    }

    @Override
    public void useSchematic(Schematic schem, boolean checkHidden){
        selectPlans.clear();
        selectPlans.addAll(m_schematics.toPlans(schem, World.toTile(Core.camera.position.x), World.toTile(Core.camera.position.y)));
        lastSchematic = schem;
    }



    public boolean hasMoved(int x, int y) { return true; }

    // there is nothing because, you know, it's mobile
    public void changePanSpeed(float value) {}

    public void lockMovement() {
        movementLocked = !movementLocked;
    }

    public void lockShooting() {
        shootingLocked = !shootingLocked;
    }

    public void observe(Player target) {
        observed = target;
    }

    public void flush(Seq<BuildPlan> plans) {
        flushPlans(plans);
    }

    public InputHandler asHandler() {
        return this;
    }

    @Override
    public void boost(boolean boost) {
        ctrlBoost = true;
        mi2uBoost = boost;
    }

    @Override
    public void pan(boolean ctrl, float x, float y) {
        if (ctrl) camera.position.set(x, y);
    }

    @Override
    public void shoot(Vec2 vec, boolean shoot, boolean ctrl) {
        ctrlShoot = ctrl;
        shootXY.set(vec);
        mi2uShoot = shoot;
    }

    @Override
    public void move(Vec2 movement) {
        ctrlMove = true;
        mi2uMove.set(movement);
    }

    @Override
    public void clear() {
        ctrlBoost = false;
        ctrlShoot = false;
        ctrlMove = false;
        mi2uMove.setZero();
        shootXY.setZero();
    }
}
