/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity.corporea;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.corporea.*;
import vazkii.botania.common.BotaniaStats;
import vazkii.botania.common.advancements.CorporeaRequestTrigger;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.helper.MathHelper;
import vazkii.botania.network.serverbound.IndexStringRequestPacket;
import vazkii.botania.xplat.ClientXplatAbstractions;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CorporeaIndexBlockEntity extends BaseCorporeaBlockEntity implements CorporeaRequestor {
	public static final double RADIUS = 2.5;
	public static final int MAX_REQUEST = 1 << 16;

	private static final Set<CorporeaIndexBlockEntity> serverIndexes = Collections.newSetFromMap(new WeakHashMap<>());
	private static final Set<CorporeaIndexBlockEntity> clientIndexes = Collections.newSetFromMap(new WeakHashMap<>());

	private static final Map<Pattern, IRegexStacker> patterns = new LinkedHashMap<>();

	/**
	 * (name) = Item name, or "this" for the name of the item in your hand
	 * (n), (n1), (n2), etc = Numbers
	 * [text] = Optional
	 * <a/b> = Either a or b
	 */
	static {
		// (name) = 1
		addPattern("(.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 1;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// [a][n] (name) = 1
		addPattern("a??n?? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 1;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		//(n)[x][ of] (name) = n
		addPattern("(\\d+)x?(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return i(m, 1);
			}

			@Override
			public String getName(Matcher m) {
				return m.group(2);
			}
		});

		// [a ]stack[ of] (name) = 64
		addPattern("(?:a )?stack(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 64;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// (n)[x] stack[s][ of] (name) = n * 64
		addPattern("(\\d+)x?? stacks?(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 64 * i(m, 1);
			}

			@Override
			public String getName(Matcher m) {
				return m.group(2);
			}
		});

		// [a ]stack <and/+> (n)[x][ of] (name) = 64 + n
		addPattern("(?:a )?stack (?:(?:and)|(?:\\+)) (\\d+)(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 64 + i(m, 1);
			}

			@Override
			public String getName(Matcher m) {
				return m.group(2);
			}
		});

		// (n1)[x] stack[s] <and/+> (n2)[x][ of] (name) = n1 * 64 + n2
		addPattern("(\\d+)x?? stacks? (?:(?:and)|(?:\\+)) (\\d+)x?(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 64 * i(m, 1) + i(m, 2);
			}

			@Override
			public String getName(Matcher m) {
				return m.group(3);
			}
		});

		// [a ]half [of ][a ]stack[ of] (name) = 32
		addPattern("(?:a )?half (?:of )?(?:a )?stack(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 32;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// [a ]quarter [of ][a ]stack[ of] (name) = 16
		addPattern("(?:a )?quarter (?:of )?(?:a )?stack(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 16;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// [a ]dozen[ of] (name) = 12
		addPattern("(?:a )?dozen(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 12;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// (n)[x] dozen[s][ of] (name) = n * 12
		addPattern("(\\d+)x?? dozens?(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 12 * i(m, 1);
			}

			@Override
			public String getName(Matcher m) {
				return m.group(2);
			}
		});

		// <all/every> [<of/the> ](name) = 2147483647
		addPattern("(?:all|every) (?:(?:of|the) )?(.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return MAX_REQUEST;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// [the ]answer to life[,] the universe and everything [of ](name) = 42
		addPattern("(?:the )?answer to life,? the universe and everything (?:of )?(.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 42;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// [a ]nice [of ](name) = 69 
		addPattern("(?:a )?nice (?:of )?(.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 69;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// (n)[x] nice[s][ of] (name) = n * 69
		addPattern("(\\d+)x?? nices?(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 69 * i(m, 1);
			}

			@Override
			public String getName(Matcher m) {
				return m.group(2);
			}
		});

		// <count/show/display/tell> (name) = 0 (display only)
		addPattern("(?:count|show|display|tell) (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 0;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});
	}

	public int ticksWithCloseby = 0;
	public float closeby = 0F;
	public boolean hasCloseby;

	public CorporeaIndexBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.CORPOREA_INDEX, pos, state);
	}

	public static void commonTick(Level level, BlockPos worldPosition, BlockState state, CorporeaIndexBlockEntity self) {
		double x = worldPosition.getX() + 0.5;
		double y = worldPosition.getY() + 0.5;
		double z = worldPosition.getZ() + 0.5;

		List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(x - RADIUS, y - RADIUS, z - RADIUS, x + RADIUS, y + RADIUS, z + RADIUS));
		self.hasCloseby = false;
		if (self.getSpark() != null) {
			for (Player player : players) {
				if (self.isInRange(player)) {
					self.hasCloseby = true;
					break;
				}
			}
		}

		float step = 0.2F;
		if (self.hasCloseby) {
			self.ticksWithCloseby++;
			if (self.closeby < 1F) {
				self.closeby += step;
			}
		} else if (self.closeby > 0F) {
			self.closeby -= step;
		}

		if (!self.isRemoved()) {
			addIndex(self);
		}
	}

	public static List<CorporeaIndexBlockEntity> getNearbyValidIndexes(Player player) {
		return (player.level.isClientSide ? clientIndexes : serverIndexes)
				.stream().filter(i -> i.getSpark() != null && i.isInRange(player))
				.collect(Collectors.toList());
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		removeIndex(this);
	}

	@Override
	public void doCorporeaRequest(CorporeaRequestMatcher request, int count, CorporeaSpark spark, @Nullable LivingEntity entity) {
		doRequest(request, count, spark, entity);
	}

	private CorporeaResult doRequest(CorporeaRequestMatcher matcher, int count, CorporeaSpark spark, @Nullable LivingEntity entity) {
		CorporeaResult result = CorporeaHelper.instance().requestItem(matcher, count, spark, entity, true);
		List<ItemStack> stacks = result.stacks();
		spark.onItemsRequested(stacks);
		for (ItemStack stack : stacks) {
			if (!stack.isEmpty()) {
				ItemEntity item = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.5, worldPosition.getZ() + 0.5, stack);
				level.addFreshEntity(item);
			}
		}
		return result;
	}

	private boolean isInRange(Player player) {
		return player.level.dimension() == level.dimension()
				&& MathHelper.pointDistancePlane(getBlockPos().getX() + 0.5, getBlockPos().getZ() + 0.5, player.getX(), player.getZ()) < RADIUS
				&& Math.abs(getBlockPos().getY() + 0.5 - player.getY()) < 5;
	}

	public static void addPattern(String pattern, IRegexStacker stacker) {
		patterns.put(Pattern.compile(pattern), stacker);
	}

	public static int i(Matcher m, int g) {
		try {
			int i = Math.abs(Integer.parseInt(m.group(g)));
			return i;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static void addIndex(CorporeaIndexBlockEntity index) {
		Set<CorporeaIndexBlockEntity> set = index.level.isClientSide ? clientIndexes : serverIndexes;
		set.add(index);
	}

	private static void removeIndex(CorporeaIndexBlockEntity index) {
		Set<CorporeaIndexBlockEntity> set = index.level.isClientSide ? clientIndexes : serverIndexes;
		set.remove(index);
	}

	public static void clearIndexCache() {
		clientIndexes.clear();
		serverIndexes.clear();
	}

	public void performPlayerRequest(ServerPlayer player, CorporeaRequestMatcher request, int count) {
		if (!XplatAbstractions.INSTANCE.fireCorporeaIndexRequestEvent(player, request, count, this.getSpark())) {
			CorporeaResult res = this.doRequest(request, count, this.getSpark(), player);

			player.sendSystemMessage(Component.translatable("botaniamisc.requestMsg", count, request.getRequestName(), res.matchedCount(), res.extractedCount()).withStyle(ChatFormatting.LIGHT_PURPLE));
			player.awardStat(BotaniaStats.CORPOREA_ITEMS_REQUESTED, res.extractedCount());
			CorporeaRequestTrigger.INSTANCE.trigger(player, player.getLevel(), this.getBlockPos(), res.extractedCount());
		}
	}

	public static class ClientHandler {
		public static boolean onChat(Player player, String message) {
			if (!getNearbyValidIndexes(player).isEmpty()) {
				ClientXplatAbstractions.INSTANCE.sendToServer(new IndexStringRequestPacket(message));
				return true;
			}
			return false;
		}
	}

	public static void onChatMessage(ServerPlayer player, String message) {
		if (player.isSpectator()) {
			return;
		}

		List<CorporeaIndexBlockEntity> nearbyIndexes = getNearbyValidIndexes(player);
		if (!nearbyIndexes.isEmpty()) {
			String msg = message.toLowerCase(Locale.ROOT).trim();
			for (CorporeaIndexBlockEntity index : nearbyIndexes) {
				String name = "";
				int count = 0;
				for (Pattern pattern : patterns.keySet()) {
					Matcher matcher = pattern.matcher(msg);
					if (matcher.matches()) {
						IRegexStacker stacker = patterns.get(pattern);
						count = Math.min(MAX_REQUEST, stacker.getCount(matcher));
						name = stacker.getName(matcher).toLowerCase(Locale.ROOT).trim();
					}
				}

				if (name.equals("this")) {
					ItemStack stack = player.getMainHandItem();
					if (!stack.isEmpty()) {
						name = stack.getHoverName().getString().toLowerCase(Locale.ROOT).trim();
					}
				}

				index.performPlayerRequest(player, CorporeaHelper.instance().createMatcher(name), count);
			}
		}
	}

	public interface IRegexStacker {
		int getCount(Matcher m);

		String getName(Matcher m);

	}

}
