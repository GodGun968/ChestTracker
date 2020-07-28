package red.jackf.chesttracker.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.ButtonDisplayType;
import red.jackf.chesttracker.tracker.Location;
import red.jackf.chesttracker.tracker.LocationStorage;
import red.jackf.chesttracker.tracker.Tracker;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public class FavouriteButton extends TexturedButtonWidget {
    private static final Identifier TEXTURE = id("favourite_button.png");
    public static FavouriteButton current = null;
    private boolean toggleActive = false;

    public FavouriteButton() {
        super(0, 0, 9, 9, 0, 0, 9, TEXTURE, 18, 18, (button -> ((FavouriteButton) button).toggleActive()));
        LocationStorage storage = LocationStorage.get();
        World world = MinecraftClient.getInstance().world;
        if (storage != null && world != null && Tracker.getInstance().getLastInteractedPos() != null) {
            Location loc = storage.getStorage(world.getRegistryKey().getValue()).lookupFast(Tracker.getInstance().getLastInteractedPos());
            if (loc != null)
                toggleActive = loc.isFavourite();
        }
    }

    public static void setup() {
        ClothClientHooks.SCREEN_INIT_POST.register((client, screen, screenHooks) -> {
            if (Tracker.getInstance().validScreenToTrack(screen)) {
                current = new FavouriteButton();
                screenHooks.cloth$addButtonWidget(
                    current
                );
            }
        });
    }

    public void toggleActive() {
        setActive(!toggleActive);
    }

    public boolean isActive() {
        return toggleActive;
    }

    public void setActive(boolean toggleActive) {
        this.toggleActive = toggleActive;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        HandledScreen<?> screen = (HandledScreen<?>) MinecraftClient.getInstance().currentScreen;
        ButtonDisplayType type = ChestTracker.CONFIG.visualOptions.buttonDisplayType;
        if (screen != null) {
            if (type.isVertical()) {
                this.setPos(type.getX(screen), type.getY(screen) + 12);
            } else {
                this.setPos(type.getX(screen) - 12, type.getY(screen));
            }

            if (this.isHovered()) {
                screen.renderTooltip(matrices, new TranslatableText("chesttracker.gui.favourite"), mouseX, mouseY);
            }
            MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);

            RenderSystem.enableDepthTest();
            drawTexture(matrices, this.x, this.y, (float) (toggleActive ? 9 : 0), (float) (this.isHovered() ? 9 : 0), this.width, this.height, 18, 18);
        }
    }
}
