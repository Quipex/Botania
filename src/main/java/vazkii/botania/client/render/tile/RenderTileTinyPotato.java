/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jul 18, 2014, 10:48:46 PM (GMT)]
 */
package vazkii.botania.client.render.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import vazkii.botania.api.item.TinyPotatoRenderEvent;
import vazkii.botania.client.core.handler.ContributorFancinessHandler;
import vazkii.botania.client.core.helper.ShaderHelper;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.client.model.ModelTinyPotato;
import vazkii.botania.common.block.tile.TileTinyPotato;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.bauble.ItemFlightTiara;
import vazkii.botania.common.item.material.ItemManaResource;
import vazkii.botania.common.item.relic.ItemInfiniteFruit;

public class RenderTileTinyPotato extends TileEntitySpecialRenderer {

	private static final ResourceLocation texture = new ResourceLocation(LibResources.MODEL_TINY_POTATO);
	private static final ResourceLocation textureGrayscale = new ResourceLocation(LibResources.MODEL_TINY_POTATO_GS);
	private static final ResourceLocation textureHalloween = new ResourceLocation(LibResources.MODEL_TINY_POTATO_HALLOWEEN);
	private static final ModelTinyPotato model = new ModelTinyPotato();

	@Override
	public void renderTileEntityAt(TileEntity var1, double d0, double d1, double d2, float var8, int digProgress) {
		TileTinyPotato potato = (TileTinyPotato) var1;
		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.translate(d0, d1, d2);

		Minecraft mc = Minecraft.getMinecraft();
		mc.renderEngine.bindTexture(ClientProxy.dootDoot ? textureHalloween : texture);
		String name = potato.name.toLowerCase();

		boolean usedShader = false;
		if(name.startsWith("gaia ")) {
			ShaderHelper.useShader(ShaderHelper.doppleganger);
			name = name.substring(5);
			usedShader = true;
		} else if(name.startsWith("hot ")) {
			ShaderHelper.useShader(ShaderHelper.halo);
			name = name.substring(4);
			usedShader = true;
		} else if(name.startsWith("magic ")) {
			ShaderHelper.useShader(ShaderHelper.enchanterRune);
			name = name.substring(6);
			usedShader = true;
		} else if(name.startsWith("gold ")) {
			ShaderHelper.useShader(ShaderHelper.gold);
			name = name.substring(5);
			usedShader = true;
		} else if(name.startsWith("snoop ")) {
			ShaderHelper.useShader(ShaderHelper.terraPlateRune);
			name = name.substring(6);
			usedShader = true;
		}

		GlStateManager.translate(0.5F, 1.5F, 0.5F);
		GlStateManager.scale(1F, -1F, -1F);
		int meta = potato.getWorld() == null ? 3 : potato.getBlockMetadata();
		float rotY = meta * 90F - 180F;
		GlStateManager.rotate(rotY, 0F, 1F, 0F);

		float jump = potato.jumpTicks;
		if(jump > 0)
			jump -= var8;

		float up = (float) -Math.abs(Math.sin(jump / 10 * Math.PI)) * 0.2F;
		float rotZ = (float) Math.sin(jump / 10 * Math.PI) * 2;

		GlStateManager.translate(0F, up, 0F);
		GlStateManager.rotate(rotZ, 0F, 0F, 1F);

		GlStateManager.pushMatrix();
		if(name.equals("pahimar")) {
			GlStateManager.scale(1F, 0.3F, 1F);
			GlStateManager.translate(0F, 3.5F, 0F);
		} else if(name.equals("kyle hyde"))
			mc.renderEngine.bindTexture(textureGrayscale);
		else if(name.equals("dinnerbone") || name.equals("grumm")) {
			GlStateManager.rotate(180F, 0F, 0F, 1F);
			GlStateManager.translate(0F, -2.625F, 0F);
		} else if(name.equals("aureylian"))
			GlStateManager.color(1F, 0.5F, 1F);


		boolean render = !(name.equals("mami") || name.equals("soaryn") || name.equals("eloraam") && jump != 0);
		if(render)
			model.render();
		if(name.equals("kingdaddydmac")) {
			GlStateManager.translate(0.5F, 0F, 0F);
			model.render();
		}

		if(usedShader)
			ShaderHelper.releaseShader();

		GlStateManager.popMatrix();

		if(!name.isEmpty()) {
			GlStateManager.pushMatrix();
			mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

			ContributorFancinessHandler.firstStart();

			float scale = 1F / 4F;
			GlStateManager.translate(0F, 1F, 0F);
			GlStateManager.scale(scale, scale, scale);
			if(name.equals("phi") || name.equals("vazkii")) {
				GlStateManager.translate(0.45F, 0F, 0.4F);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				GlStateManager.rotate(20F, 1F, 0F, 1F);
				renderIcon(((ItemManaResource) ModItems.manaResource).phiFlowerIcon);

				if(name.equals("vazkii")) {
					GlStateManager.rotate(-20F, 1F, 0F, 1F);
					GlStateManager.scale(1.25F, 1.25F, 1.25F);
					GlStateManager.rotate(180F, 0F, 0F, 1F);
					GlStateManager.translate(-1.5F, -1.3F, -0.75F);
					renderIcon(((ItemManaResource) ModItems.manaResource).nerfBatIcon);
				}
			} else if(name.equals("skull kid")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-0.5F, -1.2F, -0.4F);
				renderIcon(ModItems.cosmetic.getIconFromDamage(23));
			} else if(name.equals("kamina")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-0.5F, -1.1F, -0.4F);
				renderIcon(ModItems.cosmetic.getIconFromDamage(26));
			} else if(name.equals("haighyorkie")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				GlStateManager.translate(-0.5F, -1.2F, -0.4F);
				renderIcon(((ItemManaResource) ModItems.manaResource).goldfishIcon);
			} else if(name.equals("chitoge")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-0.5F, -0.7F, 0.1F);
				renderIcon(ModItems.cosmetic.getIconFromDamage(7));
			} else if(name.equals("direwolf20")) {
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-0.5F, -2.2F, -0.5F);
				renderIcon(ModItems.cosmetic.getIconFromDamage(0));
			} else if(name.equals("doctor")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-0.5F, -1.15F, -0.4F);
				renderIcon(ModItems.cosmetic.getIconFromDamage(25));
			} else if(name.equals("snoo")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-0.5F, -0.7F, 0.1F);
				GlStateManager.rotate(20F, 0F, 0F, 1F);
				renderIcon(ModItems.cosmetic.getIconFromDamage(24));
			} else if(name.equals("charlotte")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-0.5F, -1.2F, -0.4F);
				renderIcon(ModItems.cosmetic.getIconFromDamage(12));
			} else if(name.equals("greg") || name.equals("gregorioust")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				GlStateManager.translate(-0.5F, -1.5F, -0.4F);
				renderIcon(Items.book.getIconFromDamage(0));

				mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				GlStateManager.translate(0.5F, 0.5F, 0F);
				GlStateManager.scale(0.3F, 0.3F, 0.3F);

				RenderBlocks.getInstance().renderBlockAsItem(Blocks.iron_ore, 0, 1F);
			} else if(name.equals("profmobius")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				GlStateManager.translate(-0.5F, -1.2F, -0.4F);
				renderIcon(Items.bread.getIconFromDamage(0));
			} else if(name.equals("martysgames") || name.equals("marty")) {
				GlStateManager.scale(0.7F, 0.7F, 0.7F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-0.75F, -2.4F, -0.7F);
				GlStateManager.rotate(10F, 0F, 0F, 1F);
				renderIcon(ItemInfiniteFruit.dasBootIcon);
			} else if(name.equals("tromped")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				GlStateManager.translate(-0.5F, -1.2F, -0.4F);
				renderIcon(ModItems.cacophonium.getIconFromDamage(0));
			} else if(name.equals("kain vinosec")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				GlStateManager.translate(-0.3F, -1.5F, -0.4F);
				renderIcon(ModItems.recordGaia1.getIconFromDamage(0));
				GlStateManager.translate(0F, 0F, 0.85F);
				renderIcon(ModItems.recordGaia2.getIconFromDamage(0));
			} else if(name.equals("mankrik")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-0.5F, -0.2F, -0.1F);
				renderIcon(ModItems.cosmetic.getIconFromDamage(31));
			} else if(name.equals("kurumi")) {
				GlStateManager.scale(0.4F, 0.4F, 0.4F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-0.9F, -2.5F, -1.3F);
				renderIcon(ModItems.cosmetic.getIconFromDamage(17));
			} else if(name.equals("ichun")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-0.5F, -1.2F, -0.4F);
				renderIcon(ModItems.cosmetic.getIconFromDamage(15));
			} else if(name.equals("wiiv") || name.equals("dylan4ever") || name.equals("dylankaiser")) {
				GlStateManager.scale(1.5F, 1.5F, 1.5F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				GlStateManager.translate(-0.5F, -1.1F, -0.325F);
				renderIcon(Items.painting.getIconFromDamage(0));
			} else if(name.equals("jibril")) {
				GlStateManager.scale(1.5F, 1.5F, 1.5F);
				GlStateManager.translate(0F, 0.7F, 0F);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				ItemFlightTiara.renderHalo(null, var8);
			} else if(name.equals("nebris")) {
				mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				GlStateManager.rotate(180F, 1F, 0F, 0F);
				RenderBlocks.getInstance().renderBlockAsItem(Blocks.glowstone, 0, 1F);
			} else if(name.equals("ible")) {
				mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				GlStateManager.scale(1.2F, 1.2F, 1.2F);
				GlStateManager.translate(0F, 0.7F, 0F);
				GlStateManager.rotate(180F, 1F, 0F, 0F);
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				RenderBlocks.getInstance().renderBlockAsItem(Blocks.portal, 0, 1F);
			} else if(name.equals("razz") || name.equals("razzleberryfox")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				GlStateManager.translate(-0.5F, -1F, 0.45F);
				renderIcon(ModItems.cosmetic.getIconFromDamage(8));
			} else if(name.equals("etho") || name.equals("ethoslab")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				GlStateManager.translate(-0.5F, -1.2F, -0.4F);
				renderIcon(Items.cookie.getIconFromDamage(0));
			} else if(name.equals("sethbling")) {
				mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				GlStateManager.scale(1.2F, 1.2F, 1.2F);
				GlStateManager.translate(0F, 0.9F, 0F);
				GlStateManager.rotate(180F, 1F, 0F, 0F);
				RenderBlocks.getInstance().renderBlockAsItem(Blocks.command_block, 0, 1F);
			} else if(name.equals("bdoubleo100") || name.equals("bdoubleo")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-1F, -1.1F, -0.1F);
				renderIcon(Items.stick.getIconFromDamage(0));
			} else if(name.equals("kingdaddydmac")) {
				GlStateManager.scale(0.5F, 0.5F, 0.5F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				GlStateManager.translate(-0.3F, -2.5F, 1.075F);
				renderIcon(ModItems.manaRing.getIconFromDamage(0));
				GlStateManager.translate(0F, 0F, -4F);
				renderIcon(ModItems.manaRing.getIconFromDamage(0));

				mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				GlStateManager.scale(1.5F, 1.5F, 1.5F);
				GlStateManager.translate(1.5F, -0.5F, 0.7F);
				RenderBlocks.getInstance().renderBlockAsItem(Blocks.cake, 0, 1F);
			} else if(name.equals("sjin")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-0.5F, -1.27F, -0.4F);
				renderIcon(ModItems.cosmetic.getIconFromDamage(27));
			} else if(name.equals("martyn") || name.equals("inthelittlewood")) {
				GlStateManager.scale(1.25F, 1.25F, 1.25F);
				GlStateManager.rotate(180F, 0F, 0F, 1F);
				GlStateManager.translate(-0.5F, -0.45F, -0.1F);
				mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				renderIcon(Blocks.sapling.getIcon(0, 0));
			}else if(ContributorFancinessHandler.flowerMap != null && ContributorFancinessHandler.flowerMap.containsKey(name)) {
				IIcon icon = ContributorFancinessHandler.flowerMap.get(name);
				if(icon != null) {
					mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
					GlStateManager.rotate(180F, 1F, 0F, 0F);
					GlStateManager.translate(-0.5F, -0.5F, 0F);
					ShaderHelper.useShader(ShaderHelper.gold);
					renderIcon(icon);
					ShaderHelper.releaseShader();
				}
			}

			GlStateManager.popMatrix();
		}

		MinecraftForge.EVENT_BUS.post(new TinyPotatoRenderEvent(potato, potato.name, d0, d1, d2, var8));

		GlStateManager.rotate(-rotZ, 0F, 0F, 1F);
		GlStateManager.rotate(-rotY, 0F, 1F, 0F);
		GlStateManager.color(1F, 1F, 1F);
		GlStateManager.scale(1F, -1F, -1F);

		MovingObjectPosition pos = mc.objectMouseOver;
		if(!name.isEmpty() && pos != null && pos.blockX == potato.xCoord && pos.blockY == potato.yCoord && pos.blockZ == potato.zCoord) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, -0.6F, 0F);
			GlStateManager.rotate(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
			float f = 1.6F;
			float f1 = 0.016666668F * f;
			GlStateManager.scale(-f1, -f1, f1);
			GlStateManager.disableLighting();
			GlStateManager.translate(0.0F, 0F / f1, 0.0F);
			GlStateManager.depthMask(false);
			GlStateManager.enableBlend();
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			Tessellator tessellator = Tessellator.getInstance();
			GlStateManager.disableTexture2D();
			tessellator.getWorldRenderer().startDrawingQuads();
			int i = mc.fontRendererObj.getStringWidth(potato.name) / 2;
			tessellator.getWorldRenderer().setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
			tessellator.getWorldRenderer().addVertex(-i - 1, -1.0D, 0.0D);
			tessellator.getWorldRenderer().addVertex(-i - 1, 8.0D, 0.0D);
			tessellator.getWorldRenderer().addVertex(i + 1, 8.0D, 0.0D);
			tessellator.getWorldRenderer().addVertex(i + 1, -1.0D, 0.0D);
			tessellator.draw();
			GlStateManager.enableTexture2D();
			GlStateManager.depthMask(true);
			mc.fontRendererObj.drawString(potato.name, -mc.fontRendererObj.getStringWidth(potato.name) / 2, 0, 0xFFFFFF);
			if(name.equals("pahimar") || name.equals("soaryn")) {
				GlStateManager.translate(0F, 14F, 0F);
				String s = name.equals("pahimar") ? "[WIP]" : "(soon)";
				GlStateManager.depthMask(false);
				GlStateManager.enableBlend();
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				GlStateManager.disableTexture2D();
				tessellator.getWorldRenderer().startDrawingQuads();
				i = mc.fontRendererObj.getStringWidth(s) / 2;
				tessellator.getWorldRenderer().setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
				tessellator.getWorldRenderer().addVertex(-i - 1, -1.0D, 0.0D);
				tessellator.getWorldRenderer().addVertex(-i - 1, 8.0D, 0.0D);
				tessellator.getWorldRenderer().addVertex(i + 1, 8.0D, 0.0D);
				tessellator.getWorldRenderer().addVertex(i + 1, -1.0D, 0.0D);
				tessellator.draw();
				GlStateManager.enableTexture2D();
				GlStateManager.depthMask(true);
				mc.fontRendererObj.drawString(s, -mc.fontRendererObj.getStringWidth(s) / 2, 0, 0xFFFFFF);
			}

			GlStateManager.enableLighting();
			GlStateManager.disableBlend();
			GlStateManager.color(1F, 1F, 1F, 1F);
			GlStateManager.scale(1F / -f1, 1F / -f1, 1F / f1);
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}

	public void renderIcon(TextureAtlasSprite icon) {
		float f = icon.getMinU();
		float f1 = icon.getMaxU();
		float f2 = icon.getMinV();
		float f3 = icon.getMaxV();
		ItemRenderer.renderItemIn2D(Tessellator.getInstance(), f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 1F / 16F);
	}
}