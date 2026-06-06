package com.vyrriox.rspolymorph.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A self-contained recipe-selection popup drawn over an RS grid screen, replacing Polymorph's
 * {@code SelectionWidget}. It renders a vertical grid of candidate recipe outputs as item icons;
 * clicking one fires the selection callback with that recipe's id.
 *
 * Pure vanilla GUI — no Polymorph, no loader API. Owned and driven by {@link RsGridRecipeWidget};
 * rendered and click-routed by {@code MixinAbstractGridScreenRender}.
 *
 * Author: vyrriox
 */
public final class RecipeSelectorPopup {

    /** One selectable recipe: its id and the output stack to show as the icon. */
    public record Entry(Identifier id, ItemStack output) {}

    private static final int SLOT = 18;          // icon cell size
    private static final int COLS = 5;           // icons per row
    private static final int PADDING = 4;
    private static final int BG = 0xF0100010;    // vanilla tooltip-like background
    private static final int BORDER = 0xFF202040;
    private static final int HOVER = 0x80FFFFFF;

    private final List<Entry> entries = new ArrayList<>();
    private final Consumer<Identifier> onSelect;

    private boolean open = false;
    private int x, y;          // top-left of the popup, in screen space
    private int hovered = -1;

    public RecipeSelectorPopup(Consumer<Identifier> onSelect) {
        this.onSelect = onSelect;
    }

    public boolean isOpen() {
        return open;
    }

    /** Opens the popup anchored with its bottom-right near (anchorX, anchorY) — e.g. the result slot. */
    public void open(List<Entry> newEntries, int anchorX, int anchorY) {
        entries.clear();
        entries.addAll(newEntries);
        int rows = Math.max(1, (entries.size() + COLS - 1) / COLS);
        int w = COLS * SLOT + PADDING * 2;
        int h = rows * SLOT + PADDING * 2;
        // Place the popup to the left of and above the anchor so it doesn't cover the result.
        this.x = anchorX - w - 4;
        this.y = anchorY - h + SLOT;
        this.open = true;
        this.hovered = -1;
    }

    public void close() {
        open = false;
        entries.clear();
        hovered = -1;
    }

    /** Renders the popup. Call from the screen's render RETURN so it draws on top. */
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!open || entries.isEmpty()) return;

        int rows = Math.max(1, (entries.size() + COLS - 1) / COLS);
        int w = COLS * SLOT + PADDING * 2;
        int h = rows * SLOT + PADDING * 2;

        // We draw at the RETURN of the grid screen's extractContents (before slots/items in the
        // 26.x pipeline), so advance to a new stratum to composite the popup ABOVE the slots.
        graphics.nextStratum();

        // Background + border.
        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, BORDER);
        graphics.fill(x, y, x + w, y + h, BG);

        hovered = -1;
        for (int i = 0; i < entries.size(); i++) {
            int col = i % COLS;
            int row = i / COLS;
            int cx = x + PADDING + col * SLOT;
            int cy = y + PADDING + row * SLOT;

            boolean over = mouseX >= cx && mouseX < cx + SLOT && mouseY >= cy && mouseY < cy + SLOT;
            if (over) {
                hovered = i;
                graphics.fill(cx, cy, cx + SLOT, cy + SLOT, HOVER);
            }
            graphics.item(entries.get(i).output(), cx + 1, cy + 1);
        }

        // Name of the hovered entry, drawn as a FIXED label above the popup (not a cursor-following
        // tooltip): the grid screen still renders its own slot tooltip at the cursor, so a tooltip
        // here would overlap it into unreadable text. A fixed label disambiguates the recipes
        // cleanly without colliding.
        if (hovered >= 0) {
            Font font = Minecraft.getInstance().font;
            Component name = entries.get(hovered).output().getHoverName();
            int lw = font.width(name);
            int lx = x;
            int ly = y - font.lineHeight - 4;
            graphics.fill(lx - 2, ly - 2, lx + lw + 2, ly + font.lineHeight + 1, BG);
            graphics.fill(lx - 3, ly - 2, lx - 2, ly + font.lineHeight + 1, BORDER);
            graphics.text(font, name, lx, ly, 0xFFFFFFFF);
        }
    }

    /**
     * Handles a click. Returns true if the click was consumed by the popup (either selecting an
     * entry or clicking inside the popup background) so the screen does not pass it to slots.
     */
    public boolean mouseClicked(double mouseX, double mouseY) {
        if (!open || entries.isEmpty()) return false;

        int rows = Math.max(1, (entries.size() + COLS - 1) / COLS);
        int w = COLS * SLOT + PADDING * 2;
        int h = rows * SLOT + PADDING * 2;

        boolean inside = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        if (!inside) {
            // Click outside closes the popup but is not consumed (let the screen handle it).
            close();
            return false;
        }

        for (int i = 0; i < entries.size(); i++) {
            int col = i % COLS;
            int row = i / COLS;
            int cx = x + PADDING + col * SLOT;
            int cy = y + PADDING + row * SLOT;
            if (mouseX >= cx && mouseX < cx + SLOT && mouseY >= cy && mouseY < cy + SLOT) {
                Identifier id = entries.get(i).id();
                close();
                onSelect.accept(id);
                return true;
            }
        }
        // Clicked the popup background (not an icon) — consume so slots don't react.
        return true;
    }

    /** Hint title for accessibility / future use. */
    public static Component title() {
        return Component.translatable("rspolymorph.gui.recipe_selection");
    }

    @SuppressWarnings("unused")
    private static int fontHeight() {
        Font f = Minecraft.getInstance().font;
        return f.lineHeight;
    }
}