package net.kly.armorhudredesign.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.kly.armorhudredesign.ArmorPointsTally;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    @Unique private static final Identifier ARMOR = new Identifier("armorhudredesign", "textures/gui/armor.png");

    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;
    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract PlayerEntity getCameraPlayer();

    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getArmor()I"))
    private int getArmorRedirect(PlayerEntity playerEntity) {
        return 0;
    }

    @Inject(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V"))
    private void renderStatusBarsInject(MatrixStack matrices, CallbackInfo info) {
        renderArmorBar(matrices, this.getCameraPlayer());
    }

    private void renderArmorBar(MatrixStack matrices, PlayerEntity playerEntity) {
        this.client.getProfiler().swap("armor");
        int width = this.scaledWidth / 2 - 91;
        int height = this.scaledHeight - 39;

        int maxHealth = MathHelper.ceil(playerEntity.getMaxHealth());
        int absorption = MathHelper.ceil(playerEntity.getAbsorptionAmount());

        int rows = MathHelper.ceil((maxHealth + absorption) / 2.0F / 10.0F);
        int lines = Math.max(10 - (rows - 2), 3);
        int y = height - (rows - 1) * lines - 10;

        ArmorPointsTally turtle = new ArmorPointsTally(0, 0);
        ArmorPointsTally netherite = new ArmorPointsTally(0, 0);
        ArmorPointsTally diamond = new ArmorPointsTally(0, 0);
        ArmorPointsTally iron = new ArmorPointsTally(0, 0);
        ArmorPointsTally mail = new ArmorPointsTally(0, 0);
        ArmorPointsTally gold = new ArmorPointsTally(0, 0);
        ArmorPointsTally leather = new ArmorPointsTally(0, 0);

        for (ItemStack stack : playerEntity.getArmorItems()) {
            if (stack.getItem() instanceof ArmorItem armorItem) {
                if (armorItem.getMaterial() == ArmorMaterials.TURTLE) {
                    turtle.points += armorItem.getProtection();
                    if ((float)stack.getDamage() / (float)stack.getMaxDamage() > 0.9F) turtle.pointsOnLowDurability += armorItem.getProtection();
                } else if (armorItem.getMaterial() == ArmorMaterials.NETHERITE) {
                    netherite.points += armorItem.getProtection();
                    if ((float)stack.getDamage() / (float)stack.getMaxDamage() > 0.9F) netherite.pointsOnLowDurability += armorItem.getProtection();
                } else if (armorItem.getMaterial() == ArmorMaterials.DIAMOND) {
                    diamond.points += armorItem.getProtection();
                    if ((float)stack.getDamage() / (float)stack.getMaxDamage() > 0.9F) diamond.pointsOnLowDurability += armorItem.getProtection();
                } else if (armorItem.getMaterial() == ArmorMaterials.IRON) {
                    iron.points += armorItem.getProtection();
                    if ((float)stack.getDamage() / (float)stack.getMaxDamage() > 0.9F) iron.pointsOnLowDurability += armorItem.getProtection();
                } else if (armorItem.getMaterial() == ArmorMaterials.CHAIN) {
                    mail.points += armorItem.getProtection();
                    if ((float)stack.getDamage() / (float)stack.getMaxDamage() > 0.9F) mail.pointsOnLowDurability += armorItem.getProtection();
                } else if (armorItem.getMaterial() == ArmorMaterials.GOLD) {
                    gold.points += armorItem.getProtection();
                    if ((float)stack.getDamage() / (float)stack.getMaxDamage() > 0.9F) gold.pointsOnLowDurability += armorItem.getProtection();
                } else {
                    leather.points += armorItem.getProtection();
                    if ((float)stack.getDamage() / (float)stack.getMaxDamage() > 0.9F) leather.pointsOnLowDurability += armorItem.getProtection();
                }
            }
        }

        long timeMs = Util.getMeasuringTimeMs();
        float alpha = MathHelper.sin(timeMs / 2000F * MathHelper.PI);

        RenderSystem.setShaderTexture(0, ARMOR);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        for (int count = 9; count >= 0 && leather.points + gold.points + mail.points + iron.points + diamond.points + netherite.points + turtle.points > 0; count--) {
            int x = width + count * 8;
            this.drawTexture(matrices, x, y, 0, 9, 9, 9);
        }

        for (int count = 9; count >= 0 && leather.points + gold.points + mail.points + iron.points + diamond.points + netherite.points + turtle.points > 0; count--) {
            int x = width + count * 8;
            boolean half;
            if (count * 2 < leather.points + gold.points + mail.points + iron.points + diamond.points + netherite.points + turtle.points) {
                half = count * 2 + 1 == leather.points + gold.points + mail.points + iron.points + diamond.points + netherite.points + turtle.points;
                this.drawTexture(matrices, x, y, half ? 9 : 0, 0, 9, 9);

                if (leather.pointsOnLowDurability > 0 && count * 2 >= leather.points + gold.points + mail.points + iron.points + diamond.points + netherite.points + turtle.points - leather.pointsOnLowDurability) {
                    RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                    this.drawTexture(matrices, x, y, 0, half ? 27 : 18, 9, 9);
                    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                } else if (leather.pointsOnLowDurability > 0 && count * 2 + 1 == leather.points + gold.points + mail.points + iron.points + diamond.points + netherite.points + turtle.points - leather.pointsOnLowDurability) {
                    RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                    this.drawTexture(matrices, x, y, 0, 36, 9, 9);
                    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                }

                if (count * 2 < gold.points + mail.points + iron.points + diamond.points + netherite.points + turtle.points) {
                    half = count * 2 + 1 ==  gold.points + mail.points + iron.points + diamond.points + netherite.points + turtle.points;
                    this.drawTexture(matrices, x, y, half ? 27 : 18, 0, 9, 9);

                    if (gold.pointsOnLowDurability > 0 && count * 2 >= gold.points + mail.points + iron.points + diamond.points + netherite.points + turtle.points - gold.pointsOnLowDurability) {
                        RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                        this.drawTexture(matrices, x, y, 0, half ? 27 : 18, 9, 9);
                        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                    } else if (gold.pointsOnLowDurability > 0 && count * 2 + 1 == gold.points + mail.points + iron.points + diamond.points + netherite.points + turtle.points - gold.pointsOnLowDurability) {
                        RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                        this.drawTexture(matrices, x, y, 0, 36, 9, 9);
                        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                    }

                    if (count * 2 < mail.points + iron.points + diamond.points + netherite.points + turtle.points) {
                        half = count * 2 + 1 ==  mail.points + iron.points + diamond.points + netherite.points + turtle.points;
                        this.drawTexture(matrices, x, y, half ? 45 : 36, 0, 9, 9);

                        if (mail.pointsOnLowDurability > 0 && count * 2 >= mail.points + iron.points + diamond.points + netherite.points + turtle.points - mail.pointsOnLowDurability) {
                            RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                            this.drawTexture(matrices, x, y, 0, half ? 27 : 18, 9, 9);
                            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                        } else if (mail.pointsOnLowDurability > 0 && count * 2 + 1 == mail.points + iron.points + diamond.points + netherite.points + turtle.points - mail.pointsOnLowDurability) {
                            RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                            this.drawTexture(matrices, x, y, 0, 36, 9, 9);
                            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                        }

                        if (count * 2 < iron.points + diamond.points + netherite.points + turtle.points) {
                            half = count * 2 + 1 ==  iron.points + diamond.points + netherite.points + turtle.points;
                            this.drawTexture(matrices, x, y, half ? 63 : 54, 0, 9, 9);

                            if (iron.pointsOnLowDurability > 0 && count * 2 >= iron.points + diamond.points + netherite.points + turtle.points - iron.pointsOnLowDurability) {
                                RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                                this.drawTexture(matrices, x, y, 0, half ? 27 : 18, 9, 9);
                                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                            } else if (iron.pointsOnLowDurability > 0 && count * 2 + 1 == iron.points + diamond.points + netherite.points + turtle.points - iron.pointsOnLowDurability) {
                                RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                                this.drawTexture(matrices, x, y, 0, 36, 9, 9);
                                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                            }

                            if (count * 2 < diamond.points + netherite.points + turtle.points) {
                                half = count * 2 + 1 == diamond.points + netherite.points + turtle.points;
                                this.drawTexture(matrices, x, y, half ? 81 : 72, 0, 9, 9);

                                if (diamond.pointsOnLowDurability > 0 && count * 2 >= diamond.points + netherite.points + turtle.points - diamond.pointsOnLowDurability) {
                                    RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                                    this.drawTexture(matrices, x, y, 0, half ? 27 : 18, 9, 9);
                                    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                                } else if (diamond.pointsOnLowDurability > 0 && count * 2 + 1 == diamond.points + netherite.points + turtle.points - diamond.pointsOnLowDurability) {
                                    RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                                    this.drawTexture(matrices, x, y, 0, 36, 9, 9);
                                    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                                }

                                if (count * 2 < netherite.points + turtle.points) {
                                    half = count * 2 + 1 == netherite.points + turtle.points;
                                    this.drawTexture(matrices, x, y, half ? 99 : 90, 0, 9, 9);

                                    if (netherite.pointsOnLowDurability > 0 && count * 2 >= netherite.points + turtle.points - netherite.pointsOnLowDurability) {
                                        RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                                        this.drawTexture(matrices, x, y, 0, half ? 27 : 18, 9, 9);
                                        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                                    } else if (netherite.pointsOnLowDurability > 0 && count * 2 + 1 == netherite.points + turtle.points - netherite.pointsOnLowDurability) {
                                        RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                                        this.drawTexture(matrices, x, y, 0, 36, 9, 9);
                                        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                                    }

                                    if (count * 2 < turtle.points) {
                                        half = count * 2 + 1 == turtle.points;
                                        this.drawTexture(matrices, x, y, half ? 117 : 108, 0, 9, 9);

                                        if (turtle.pointsOnLowDurability > 0 && count * 2 >= turtle.points - turtle.pointsOnLowDurability) {
                                            RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                                            this.drawTexture(matrices, x, y, 0, half ? 27 : 18, 9, 9);
                                            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                                        } else if (turtle.pointsOnLowDurability > 0 && count * 2 + 1 == turtle.points - turtle.pointsOnLowDurability) {
                                            RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
                                            this.drawTexture(matrices, x, y, 0, 36, 9, 9);
                                            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }
}
