package net.uku3lig.ukulib.config.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.uku3lig.ukulib.config.ConfigManager;
import net.uku3lig.ukulib.config.impl.BrokenConfigScreen;
import net.uku3lig.ukulib.utils.Ukutils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Simple abstract class which provides utilities for config screens
 *
 * @param <T> The type of the config class
 */
public abstract class BaseConfigScreen<T extends Serializable> extends CloseableScreen {
    /**
     * The config manager
     */
    protected final ConfigManager<T> manager;

    /*
     * Logger
     */
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private ButtonWidget doneButton;

    /**
     * Constructs the screen
     *
     * @param key     The translation key of the title
     * @param parent  The parent screen
     * @param manager The config manager
     */
    protected BaseConfigScreen(String key, Screen parent, ConfigManager<T> manager) {
        super(key, parent);
        this.manager = manager;
    }

    /**
     * Tries to apply the current config to a function (eg. {@code getWidgets}).
     * If an exception is thrown, the config is first reset and applied again,
     * and if it fails a second time, a notice is shown to the end user.
     * This is mainly useful to prevent {@link NullPointerException}s.
     * <p>
     * Example: {@code buttonList.addAll(applyConfigChecked(this::getWidgets, new WidgetCreator[0]))}
     *
     * @param mapper       The function that needs a config
     * @param defaultValue The default value, in case the config is broken. Only here to avoid returning null.
     * @param <R>          The type of the return value of the applied function
     * @return The result of the function, if the config is working
     */
    protected <R> R applyConfigChecked(Function<T, R> mapper, R defaultValue) {
        try {
            return mapper.apply(manager.getConfig());
        } catch (Exception e) {
            log.error("Error while getting options, replacing config with the default one", e);
            manager.resetConfig();
            try {
                return mapper.apply(manager.getConfig());
            } catch (Exception e2) {
                log.error("Error while getting options with the default config, this is a bug", e2);
                MinecraftClient.getInstance().setScreen(new BrokenConfigScreen(parent));
            }
        }

        return defaultValue;
    }

    @Override
    protected void init() {
        doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> MinecraftClient.getInstance().setScreen(parent))
                .dimensions(width / 2 - 155, height - 27, 150, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("ukulib.option.reset"), button -> {
                    MinecraftClient.getInstance().setScreen(parent);
                    manager.resetConfig();
                    manager.saveConfig();
                    Ukutils.sendToast(Text.of("Sucessfully reset config!"), null);
                })
                .dimensions(width / 2 + 5, height - 27, 150, 20)
                .build());
    }

    @Override
    public void tick() {
        super.tick();

        Collection<ClickableWidget> invalid = this.getInvalidOptions();
        doneButton.active = invalid.isEmpty();

        if (doneButton.active) {
            doneButton.setTooltip(Tooltip.of(Text.empty()));
        } else {
            String invalidNames = invalid.stream()
                    .map(ClickableWidget::getMessage)
                    .map(Text::getString)
                    .collect(Collectors.joining(", "));

            doneButton.setTooltip(Tooltip.of(Text.translatable("ukulib.option.invalid", invalidNames)));
        }
    }

    /**
     * Finds invalid options in the config screen. Called once every tick.
     *
     * @return The collection of invalid options.
     */
    protected Collection<ClickableWidget> getInvalidOptions() {
        return Collections.emptyList();
    }

    @Override
    public void removed() {
        if (getInvalidOptions().isEmpty()) {
            manager.saveConfig();
        }
    }
}
