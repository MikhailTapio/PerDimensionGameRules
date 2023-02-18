package snownee.perworldgamerules;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;
import snownee.kiwi.config.KiwiConfigManager;

public class PWGameRuleCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		final LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("pwgamerule").requires(commandSourceStack -> commandSourceStack.hasPermission(2));
		GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {

			@Override
			public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
				if (PWGameRulesMod.isSupported(key)) {
					literalArgumentBuilder.then((Commands.literal(key.getId()).executes(commandContext -> PWGameRuleCommand.queryRule(commandContext.getSource(), key))).then(type.createArgument("value").executes(commandContext -> PWGameRuleCommand.setRule(commandContext, key))));
				}
			}

		});
		commandDispatcher.register(literalArgumentBuilder);
	}

	static <T extends GameRules.Value<T>> int setRule(CommandContext<CommandSourceStack> commandContext, GameRules.Key<T> key) {
		CommandSourceStack commandSourceStack = commandContext.getSource();
		T value = commandSourceStack.getLevel().getGameRules().getRule(key).type.createRule();
		value.setFromArgument(commandContext, "value");
		String dimension = commandSourceStack.getLevel().dimension().location().toString();
		Map<String, Object> map = PWGameRulesConfig.rules.computeIfAbsent(dimension, k -> Maps.newHashMap());
		map.put(key.getId(), value.serialize());
		KiwiConfigManager.getHandler(PWGameRulesConfig.class).save();
		PWGameRulesMod.generation++;
		commandSourceStack.sendSuccess(Component.translatable("commands.gamerule.set", key.getId(), value.toString()), true);
		return value.getCommandResult();
	}

	static <T extends GameRules.Value<T>> int queryRule(CommandSourceStack commandSourceStack, GameRules.Key<T> key) {
		T value = commandSourceStack.getLevel().getGameRules().getRule(key);
		commandSourceStack.sendSuccess(Component.translatable("commands.gamerule.query", key.getId(), value.toString()), false);
		return value.getCommandResult();
	}

}
