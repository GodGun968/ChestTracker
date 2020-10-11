package red.jackf.chesttracker.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public class NameEditButton extends TexturedButtonWidget {
    private static final Identifier TEXTURE = id("textures/text_button.png");
    private final HandledScreen<?> screen;

    public NameEditButton(HandledScreen<?> screen) {
        super(0, 0, 9, 9, 0, 0, 9, TEXTURE, 9, 18, (button) -> {

        });
        this.screen = screen;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.resize();
        super.render(matrices, mouseX, mouseY, delta);

        // renderTooltip doesn't get called
        if (this.isMouseOver(mouseX, mouseY)) {
            this.screen.renderTooltip(matrices, new TranslatableText("chesttracker.gui.edit_name"), mouseX, mouseY);
        }
    }

    private void resize() {
        this.setPos(ButtonPositions.getX(screen, 1), ButtonPositions.getY(screen, 1));
    }
}