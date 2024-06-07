package de.srendi.advancedperipherals.common.container.base;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class SlotInputHandler extends Slot {

    SlotCondition condition;

    public SlotInputHandler(Container container, int index, int xPosition, int yPosition, SlotCondition condition) {
        super(container, index, xPosition, yPosition);
        this.condition = condition;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return condition.isValid(stack);
    }
}
