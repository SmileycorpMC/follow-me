package net.smileycorp.followme.common.data;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.smileycorp.atlas.api.data.LogicalOperation;

import java.util.List;

public class LogicalDataCondition implements DataCondition {

	public final List<DataCondition> subConditions;
	protected final LogicalOperation operation;

	public LogicalDataCondition(LogicalOperation operation, List<DataCondition> conditions) {
		this.operation=operation;
		subConditions=conditions;
	}

	@Override
	public boolean matches(Mob entity, LivingEntity player) {
		boolean result = false;
		for (DataCondition condition : subConditions) {
			result = operation.apply(result, condition.matches(entity, player));
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < subConditions.size(); i++) {
			builder.append(subConditions.get(i).toString());
			if (i < subConditions.size()-1) builder.append(" " + operation.getSymbol() + " ");
		}
		return super.toString() + "[" + builder.toString() + "]";
	}

}
