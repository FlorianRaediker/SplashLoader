package io.github.florianraediker.customsplashes.client.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.florianraediker.customsplashes.client.util.splash.ReducedCalendar;
import io.github.florianraediker.customsplashes.client.util.splash.SplashList;
import io.github.florianraediker.customsplashes.client.util.splash.SplashStorage;
import net.minecraft.client.util.Splashes;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class CustomSplashes extends Splashes {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String SPLASHES_TXT_PATH = "texts/splashes.txt";
    private static final String SPLASHES_JSON_PATH = "texts/splashes.json";
    private static final Random RANDOM = new Random();

    private SplashStorage splashStorage = new SplashStorage();
    private final Session gameSession;

    public CustomSplashes(Session gameSessionIn) {
        super(gameSessionIn);
        gameSession = gameSessionIn;
    }

    /**
     * Performs any reloading that can be done off-thread, such as file IO
     */
    public List<String> prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
        this.splashStorage = new SplashStorage();
        SplashList mcHardCodedSplashes = new SplashList("minecraft");
        ReducedCalendar christmasCalendar = new ReducedCalendar();
        christmasCalendar.set(Calendar.MONTH, 12 - 1);
        christmasCalendar.set(Calendar.DATE, 24);
        mcHardCodedSplashes.addSubList(new SplashList("minecraft", false, christmasCalendar, true, Collections.singletonList("Merry X-mas!")));

        ReducedCalendar newYearCalendar = new ReducedCalendar();
        newYearCalendar.set(Calendar.MONTH, 0);
        newYearCalendar.set(Calendar.DATE, 1);
        mcHardCodedSplashes.addSubList(new SplashList("minecraft", false, newYearCalendar, true, Collections.singletonList("Happy new year!")));

        ReducedCalendar halloweenCalendar = new ReducedCalendar();
        halloweenCalendar.set(Calendar.MONTH, 10 - 1);
        halloweenCalendar.set(Calendar.DATE, 31);
        mcHardCodedSplashes.addSubList(new SplashList("minecraft", false, halloweenCalendar, true, Collections.singletonList("OOoooOOOoooo! Spooky!")));

        HashMap<String, SplashList> modId2splashList = new HashMap<>();
        for (final ModInfo mod : ModList.get().getMods()) {
            LOGGER.debug("Looking for splashes from domain " + mod.getModId());
            SplashList jsonSplashList = null;
            SplashList txtSplashList = null;
            boolean jsonOverridesTxt = false;
            try {
                ResourceLocation splashesJsonLocation = new ResourceLocation(mod.getModId(), SPLASHES_JSON_PATH);
                JsonObject jsonData = null;
                if (resourceManagerIn.hasResource(splashesJsonLocation)) {
                    try (
                            IResource resource = resourceManagerIn.getResource(new ResourceLocation(mod.getModId(), SPLASHES_JSON_PATH));
                            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                    ) {
                        jsonData = JSONUtils.fromJson(reader);
                        jsonSplashList = SplashList.fromJSON(mod.getModId(), jsonData);
                    } catch (IOException e) {
                        LOGGER.error("Could not parse " + mod.getModId() + ":texts/splashes.json", e);
                    }
                } else {
                    LOGGER.debug("Did not find {}:texts/splashes.json", mod.getModId());
                }

                if (jsonData != null) {
                    if (jsonData.has("remove")) {
                        JsonObject splashesToRemove = JSONUtils.getJsonObject(jsonData, "remove");
                        for (Map.Entry<String, JsonElement> modIdAndSplashes : splashesToRemove.entrySet()) {
                            String modId = modIdAndSplashes.getKey();
                            if (!modId2splashList.containsKey(modId)) {
                                LOGGER.warn("{}:texts/splashes.json: Unknown modid to remove splashes from: '{}'", mod.getModId(), modId);
                                continue;
                            }
                            SplashList splashList = modId2splashList.get(modId);
                            if (modIdAndSplashes.getValue().isJsonArray()) {
                                for (JsonElement splashToRemove : modIdAndSplashes.getValue().getAsJsonArray()) {
                                    if (!splashList.removeSplash(splashToRemove.getAsString())) {
                                        LOGGER.warn("{}:texts/splashes.json: Did not find splash to remove from '{}': '{}'", mod.getModId(), modId, splashToRemove);
                                    }
                                }
                            } else {
                                String value = JSONUtils.getString(modIdAndSplashes.getValue(), "splashes to be removed from modid '" + modId + "'");
                                if (value.equals("*")) {
                                    this.splashStorage.removeSubList(splashList);
                                    continue;
                                } else if (modId.equals("minecraft")) {
                                    if (value.equals("hard-coded")) {
                                        this.splashStorage.removeSubList(mcHardCodedSplashes);
                                        continue;
                                    } else if (value.equals("**")) {
                                        this.splashStorage.removeSubList(mcHardCodedSplashes);
                                        this.splashStorage.removeSubList(splashList);
                                        continue;
                                    }
                                }
                                throw new JsonSyntaxException("Expected to find \"*\" or an array as value of \"remove\"");
                            }
                        }
                    }
                    jsonOverridesTxt = JSONUtils.getBoolean(jsonData, "override_txt", false);
                }
            } catch (JsonSyntaxException e) {
                LOGGER.error("Could not parse " + mod.getModId() + ":texts/splashes.json", e);
            }

            if (!jsonOverridesTxt) {
                ResourceLocation splashesTxtLocation = new ResourceLocation(mod.getModId(), SPLASHES_TXT_PATH);
                if (resourceManagerIn.hasResource(splashesTxtLocation)) {
                    try (
                            IResource resource = resourceManagerIn.getResource(splashesTxtLocation);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                    ) {
                        txtSplashList = SplashList.fromReader(mod.getModId(), reader);
                    } catch (IOException e) {
                        LOGGER.error("Could not parse " + mod.getModId() + ":texts/splashes.txt", e);
                    }
                } else {
                    LOGGER.debug("Did not find {}:texts/splashes.txt", mod.getModId());
                }
            }

            SplashList splashList;
            if (jsonSplashList != null) {
                splashList = jsonSplashList;
                if (txtSplashList != null)
                    splashList.addSplashes(txtSplashList.getSplashes());
            } else {
                splashList = txtSplashList;
            }

            if (splashList != null) {
                LOGGER.debug("Successfully loaded splashes from domain " + mod.getModId());
                modId2splashList.put(mod.getModId(), splashList);
                this.splashStorage.addSubList(splashList);
            } else {
                LOGGER.debug("Did not find any splashes from domain " + mod.getModId());
            }
        }
        return Collections.emptyList();
    }

    protected void apply(List<String> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
    }

    @Nullable
    public String getSplashText() {
        String splash = this.splashStorage.getRandomSplash(RANDOM);
        if (splash == null) {
            return null;
        } else {
            return this.gameSession != null && RANDOM.nextInt(this.splashStorage.getAvailableSplashesCount()) == 42 ? this.gameSession.getUsername().toUpperCase(Locale.ROOT) + " IS YOU" : splash;
        }
    }
}
