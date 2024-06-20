package net.smileycorp.followme.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyMappings {
    
    static Lazy<KeyMapping> FOLLOW_KEY = key("key.followme.follow.desc", InputConstants.KEY_H);
    static Lazy<KeyMapping> STOP_KEY = key("key.followme.stop.desc", InputConstants.KEY_J);
    
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FOLLOW_KEY.get());
        event.register(STOP_KEY.get());
    }
    
    private static Lazy<KeyMapping> key(String name, int key) {
        return Lazy.of(() -> new KeyMapping(name, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, key, "key.followme.category"));
    }
}
