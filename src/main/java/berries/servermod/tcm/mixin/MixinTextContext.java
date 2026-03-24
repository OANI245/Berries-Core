package berries.servermod.tcm.mixin;

import com.google.gson.JsonParseException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pers.solid.mishang.uc.MishangUtils;
import pers.solid.mishang.uc.text.OutlineColorType;
import pers.solid.mishang.uc.text.SpecialDrawable;
import pers.solid.mishang.uc.text.TextContext;
import pers.solid.mishang.uc.util.HorizontalAlign;
import pers.solid.mishang.uc.util.TextBridge;
import pers.solid.mishang.uc.util.VerticalAlign;

@Mixin(value = TextContext.class, remap = false)
public abstract class MixinTextContext {
    @Shadow public HorizontalAlign horizontalAlign;
    @Shadow @Nullable public MutableComponent text;
    @Shadow public VerticalAlign verticalAlign;
    @Shadow public int color;
    @Shadow @NotNull public OutlineColorType outlineColorType;
    @Shadow public int outlineColor;
    @Shadow public boolean shadow;
    @Shadow public boolean seeThrough;
    @Shadow public float size;
    @Shadow public float offsetX;
    @Shadow public float offsetY;
    @Shadow public float offsetZ;
    @Shadow public float rotationX;
    @Shadow public float rotationY;
    @Shadow public float rotationZ;
    @Shadow public float scaleX;
    @Shadow public float scaleY;

    @Shadow
    private static void putBooleanParam(CompoundTag nbt, String name, boolean value) {
    }

    @Shadow public boolean bold;
    @Shadow public boolean italic;
    @Shadow public boolean underline;
    @Shadow public boolean strikethrough;
    @Shadow public boolean obfuscated;
    @Shadow public boolean absolute;
    @Shadow @Nullable public SpecialDrawable extra;
    @Unique
    public boolean isTCMConverted = false;

    @Inject(
            method = "writeNbt",
            at = @At("HEAD"),
            cancellable = true
    )
    public void write(CompoundTag nbt, CallbackInfo ci) {
        inj:
        {
            if (!isTCMConverted) {
                if (!this.italic || !this.bold) {
                    this.isTCMConverted = true;
                    break inj;
                }

                if (this.text != null) {
                    nbt.putString("textJson", String.format("{\"text\": \"%s\", \"font\": \"tcm:jtbz_a\"}", this.text.getString()));
                } else {
                    nbt.remove("text");
                }

                if (this.horizontalAlign != HorizontalAlign.CENTER) {
                    nbt.putString("horizontalAlign", this.horizontalAlign.getSerializedName());
                } else {
                    nbt.remove("horizontalAlign");
                }

                if (this.verticalAlign != VerticalAlign.MIDDLE) {
                    nbt.putString("verticalAlign", this.verticalAlign.getSerializedName());
                } else {
                    nbt.remove("verticalAlign");
                }

                nbt.putInt("color", this.color);
                nbt.putString("outlineColorType", this.outlineColorType.getSerializedName());
                nbt.putInt("outlineColor", this.outlineColor);
                putBooleanParam(nbt, "shadow", this.shadow);
                putBooleanParam(nbt, "seeThrough", this.seeThrough);
                nbt.putFloat("size", this.size + 0.5F);
                if (this.offsetX != 0.0F) {
                    nbt.putFloat("offsetX", this.offsetX);
                } else {
                    nbt.remove("offsetX");
                }

                if (this.offsetY != 0.0F) {
                    nbt.putFloat("offsetY", this.offsetY);
                } else {
                    nbt.remove("offsetY");
                }

                if (this.offsetZ != 0.0F) {
                    nbt.putFloat("offsetZ", this.offsetZ);
                } else {
                    nbt.remove("offsetZ");
                }

                if (this.rotationX != 0.0F) {
                    nbt.putFloat("rotationX", this.rotationX);
                } else {
                    nbt.remove("rotationX");
                }

                if (this.rotationY != 0.0F) {
                    nbt.putFloat("rotationY", this.rotationY);
                } else {
                    nbt.remove("rotationY");
                }

                if (this.rotationZ != 0.0F) {
                    nbt.putFloat("rotationZ", this.rotationZ);
                } else {
                    nbt.remove("rotationZ");
                }

                if (this.scaleX != 1.0F) {
                    nbt.putFloat("scaleX", this.scaleX);
                } else {
                    nbt.remove("scaleX");
                }

                if (this.scaleY != 1.0F) {
                    nbt.putFloat("scaleY", this.scaleY);
                } else {
                    nbt.remove("scaleY");
                }

                putBooleanParam(nbt, "bold", false);
                putBooleanParam(nbt, "italic", false);
                putBooleanParam(nbt, "underline", this.underline);
                putBooleanParam(nbt, "strikethrough", this.strikethrough);
                putBooleanParam(nbt, "obfuscated", this.obfuscated);
                putBooleanParam(nbt, "absolute", this.absolute);
                if (this.extra == null) {
                    nbt.remove("extra");
                } else {
                    nbt.put("extra", this.extra.createNbt());
                }

                nbt.putBoolean("isTCMConverted", isTCMConverted);
                ci.cancel();
            }

            nbt.putBoolean("isTCMConverted", isTCMConverted);
        }
    }

    @Inject(
            method = "readNbt",
            at = @At("HEAD"),
            cancellable = true
    )
    public void read(CompoundTag nbt, CallbackInfo ci) {
        this.isTCMConverted = nbt.getBoolean("isTCMConverted");

        inj: {
            if (!isTCMConverted) {
                this.bold = nbt.getBoolean("bold");
                this.italic = nbt.getBoolean("italic");

                if (!this.italic || !this.bold) {
                    this.isTCMConverted = true;
                    break inj;
                }

                this.bold = false;
                this.italic = false;

                Tag nbtText = nbt.get("text");
                String textJson = nbt.getString("textJson");
                if (nbtText instanceof StringTag && !nbtText.toString().isEmpty()) {
                    try {
                        this.text = Component.Serializer.fromJsonLenient(!textJson.isEmpty() ? textJson : String.format("{\"text\": \"%s\", \"font\": \"tcm:jtbz_b\"}", nbtText.toString()));
                    } catch (JsonParseException e) {
                        this.text = TextBridge.translatable("message.mishanguc.invalid_json", new Object[]{e.getMessage()});
                    }
                }

                this.horizontalAlign = HorizontalAlign.byName(nbt.getString("horizontalAlign"));
                if (this.horizontalAlign == null) {
                    this.horizontalAlign = HorizontalAlign.CENTER;
                }

                this.verticalAlign = VerticalAlign.byName(nbt.getString("verticalAlign"));
                if (this.verticalAlign == null) {
                    this.verticalAlign = VerticalAlign.MIDDLE;
                }

                if (nbt.contains("color")) {
                    this.color = MishangUtils.readColorFromNbtElement(nbt.get("color"));
                }

                OutlineColorType outlineColorType;
                if (nbt.contains("outlineColorType")) {
                    outlineColorType = (OutlineColorType) OutlineColorType.CODEC.byName(nbt.getString("outlineColorType"));
                } else {
                    outlineColorType = null;
                }

                if (nbt.contains("outlineColor")) {
                    this.outlineColor = MishangUtils.readColorFromNbtElement(nbt.get("outlineColor"));
                }

                if (outlineColorType == null) {
                    this.outlineColorType = OutlineColorType.fromCompatibilityValue(this.outlineColor);
                } else {
                    this.outlineColorType = outlineColorType;
                }

                this.shadow = nbt.getBoolean("shadow");
                this.seeThrough = nbt.getBoolean("seeThrough");
                this.offsetX = nbt.getFloat("offsetX");
                this.offsetY = nbt.getFloat("offsetY");
                this.offsetZ = nbt.getFloat("offsetZ");
                this.rotationX = nbt.getFloat("rotationX");
                this.rotationY = nbt.getFloat("rotationY");
                this.rotationZ = nbt.getFloat("rotationZ");
                this.scaleX = nbt.getFloat("scaleX");
                if (this.scaleX == 0.0F) {
                    this.scaleX = 1.0F;
                }

                this.scaleY = nbt.getFloat("scaleY");
                if (this.scaleY == 0.0F) {
                    this.scaleY = 1.0F;
                }

                this.size = nbt.contains("size", 99) ? nbt.getFloat("size") + 0.5F : this.size;
                this.underline = nbt.getBoolean("underline");
                this.strikethrough = nbt.getBoolean("strikethrough");
                this.obfuscated = nbt.getBoolean("obfuscated");
                this.absolute = nbt.getBoolean("absolute");
                this.extra = nbt.contains("extra", 10) ? SpecialDrawable.fromNbt((TextContext) ((Object) this), nbt.getCompound("extra")) : null;
            }
        }
    }
}
