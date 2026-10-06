package dev.slithergoh.pixelmonradar.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "pixelmonradar", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientKeys {
    public static final String CATEGORY = "key.categories.pixelmonradar";
    public static final KeyMapping OPEN_RADAR = key("key.pixelmonradar.open_radar", GLFW.GLFW_KEY_R);
    public static final KeyMapping TOGGLE_HUD = key("key.pixelmonradar.toggle_hud", GLFW.GLFW_KEY_H);
    public static final KeyMapping OPEN_ORES = key("key.pixelmonradar.open_ores", GLFW.GLFW_KEY_O);
    public static final KeyMapping TOGGLE_ORES = key("key.pixelmonradar.toggle_ores", GLFW.GLFW_KEY_UNKNOWN);

    private static KeyMapping key(String id, int key) {
        return new KeyMapping(id, InputConstants.Type.KEYSYM, key, CATEGORY);
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_RADAR);
        event.register(TOGGLE_HUD);
        event.register(OPEN_ORES);
        event.register(TOGGLE_ORES);
    }
}
