package io.github.florianraediker.customsplashes;

import io.github.florianraediker.customsplashes.client.util.CustomSplashes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

@Mod("splashloader")
public class SplashLoader {
    private static final Logger LOGGER = LogManager.getLogger();

    public SplashLoader() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(final FMLClientSetupEvent event) {
        CustomSplashes customSplashes = new CustomSplashes(Minecraft.getInstance().getSession());
        try {
            Field splashesField = ObfuscationReflectionHelper.findField(Minecraft.class, "field_213271_aF");  // "field_213271_aF" is Minecraft#splashes
            splashesField.set(Minecraft.getInstance(), customSplashes);
        } catch (IllegalAccessException | ObfuscationReflectionHelper.UnableToFindFieldException e) {
            LOGGER.fatal("Could not change Minecraft::splashes field. SplashLoader Mod won't work.", e);
            return;
        }
        customSplashes.prepare(Minecraft.getInstance().getResourceManager(), Minecraft.getInstance().getProfiler());
        ((IReloadableResourceManager)Minecraft.getInstance().getResourceManager()).addReloadListener(customSplashes);
    }
}
