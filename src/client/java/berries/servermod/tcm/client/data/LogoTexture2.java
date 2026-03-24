package berries.servermod.tcm.client.data;

import berries.servermod.tcm.TCM;
import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.texture.SimpleTexture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.util.Objects;

public class LogoTexture2 extends SimpleTexture {
    public LogoTexture2(ResourceLocation a) {
        super(a);
    }

    @Override
    protected SimpleTexture.@NotNull TextureImage getTextureImage(ResourceManager resourceManager) {
        try {
            InputStream inputStream = TCM.class.getClassLoader().getResourceAsStream("assets/tcm/" + this.location.getPath());

            SimpleTexture.TextureImage var5;
            try {
                var5 = new SimpleTexture.TextureImage(new TextureMetadataSection(true, true), NativeImage.read(inputStream));
            } catch (Throwable var8) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                    }
                }

                throw var8;
            }

            if (inputStream != null) {
                inputStream.close();
            }

            return var5;
        } catch (IOException var9) {
            return new SimpleTexture.TextureImage(var9);
        }
    }
}