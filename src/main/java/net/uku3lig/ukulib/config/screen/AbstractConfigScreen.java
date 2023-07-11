package net.uku3lig.ukulib.config.screen;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.uku3lig.ukulib.config.ConfigManager;
import net.uku3lig.ukulib.config.impl.BrokenConfigScreen;
import net.uku3lig.ukulib.config.option.CheckedOption;
import net.uku3lig.ukulib.config.option.WidgetCreator;
import net.uku3lig.ukulib.config.option.widget.WidgetCreatorList;

import java.io.Serializable;

/**
 * A screen used to edit a config.
 * Instances of this class should NOT be reused.
 *
 * @param <T> The type of the config
 */
@Slf4j
public abstract class AbstractConfigScreen<T extends Serializable> extends BaseConfigScreen<T> {
    /**
     * The widget used to display the options.
     *
     * @see AbstractConfigScreen#getWidgets(Serializable)
     */
    protected WidgetCreatorList buttonList;

    /**
     * Creates a config screen.
     *
     * @param key     The translation key of the title
     * @param parent  The parent screen
     * @param manager The config manager
     */
    protected AbstractConfigScreen(String key, Screen parent, ConfigManager<T> manager) {
        super(key, parent, manager);
    }

    /**
     * The list of widgets that will be shown to the user when this screen is displayed.
     *
     * @param config The config
     * @return An array of {@link WidgetCreator}
     */
    protected abstract WidgetCreator[] getWidgets(T config);

    @Override
    protected void init() {
        super.init();
        buttonList = new WidgetCreatorList(this.client, this.width, this.height, 32, this.height - 32, 25);

        try {
            buttonList.addAll(getWidgets(manager.getConfig()));
        } catch (Exception e) {
            log.error("Error while getting options, replacing config with the default one", e);
            manager.resetConfig();
            try {
                buttonList.addAll(getWidgets(manager.getConfig()));
            } catch (Exception e2) {
                log.error("Error while getting options with the default config, this is a bug", e2);
                MinecraftClient.getInstance().setScreen(new BrokenConfigScreen(parent));
            }
        }

        this.addSelectableChild(buttonList);
    }

    @Override
    protected boolean isEverythingValid() {
        for (WidgetCreatorList.ButtonEntry entry : buttonList.children()) {
            for (Element element : entry.children()) {
                if (element instanceof CheckedOption option && !option.isValid()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);
        buttonList.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(drawContext, mouseX, mouseY, delta);
    }
}
