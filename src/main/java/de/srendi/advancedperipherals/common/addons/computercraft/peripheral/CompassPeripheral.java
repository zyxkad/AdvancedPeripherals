package de.srendi.advancedperipherals.common.addons.computercraft.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.core.apis.TableHelper;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import de.srendi.advancedperipherals.common.addons.computercraft.operations.SingleOperationContext;
import de.srendi.advancedperipherals.common.addons.computercraft.owner.TurtlePeripheralOwner;
import de.srendi.advancedperipherals.common.configuration.APConfig;
import de.srendi.advancedperipherals.common.util.StringUtil;
import de.srendi.advancedperipherals.lib.peripherals.BasePeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import java.util.Map;
import javax.annotation.Nullable;

import static de.srendi.advancedperipherals.common.addons.computercraft.operations.SingleOperation.ACCURE_PLACE;

public class CompassPeripheral extends BasePeripheral<TurtlePeripheralOwner> {

    public static final String PERIPHERAL_TYPE = "compass";

    protected CompassPeripheral(TurtlePeripheralOwner owner) {
        super(PERIPHERAL_TYPE, owner);
        owner.attachOperation(ACCURE_PLACE);
    }

    public CompassPeripheral(ITurtleAccess turtle, TurtleSide side) {
        this(new TurtlePeripheralOwner(turtle, side).attachFuel(1));
    }

    @Override
    public boolean isEnabled() {
        return APConfig.PERIPHERALS_CONFIG.enableCompassTurtle.get();
    }

    @LuaFunction(mainThread = true)
    public String getFacing() {
        return owner.getFacing().toString();
    }

    @LuaFunction(mainThread = true)
    /**
     * 
     * @param options A table contains how to place the block:
     *   x: the x offset relative to the turtle. Default 0
     *   y: the y offset relative to the turtle. Default 0
     *   z: the z offset relative to the turtle. Default 0
     *   forward: the direction the block is going to facing. Default is the facing direction of the turtle
     *   top: the direction the block's top is going to facing. Default is TOP
     *   text: the text going to write on the sign. Default is null
     */
    public MethodResult place(Map<?, ?> options) throws LuaException {
        int x = TableHelper.optIntField(options, "x", 0);
        int y = TableHelper.optIntField(options, "y", 0);
        int z = TableHelper.optIntField(options, "z", 0);
        final int maxDist = APConfig.PERIPHERALS_CONFIG.compassTurtleRadius.get();
        final int freeDist = APConfig.PERIPHERALS_CONFIG.compassTurtleFreeRadius.get();
        if (Math.abs(x) > maxDist || Math.abs(y) > maxDist || Math.abs(z) > maxDist) {
            return MethodResult.of(false, "OUT_OF_RANGE");
        }
        String forward = TableHelper.optStringField(options, "forward", null);
        String top = TableHelper.optStringField(options, "top", null);
        Direction forwardDir = null, topDir = null;
        if (forward != null && (forwardDir = Direction.byName(forward.toLowerCase())) == null) {
            throw new LuaException(forward + "is not a valid direction");
        }
        if (top != null && (topDir = Direction.byName(top.toLowerCase())) == null) {
            throw new LuaException(top + "is not a valid direction");
        }

        // variable must be final to be used in lambda
        final Direction forwardDirF = forwardDir, topDirF = topDir;
        int distance =
            Math.max(0, Math.abs(x) - freeDist) +
            Math.max(0, Math.abs(y) - freeDist) +
            Math.max(0, Math.abs(z) - freeDist);
        return withOperation(ACCURE_PLACE, new SingleOperationContext(1, distance), null, context -> {
            ITurtleAccess turtle = owner.getTurtle();
            ItemStack stack = turtle.getInventory().getItem(turtle.getSelectedSlot());
            if (stack.isEmpty()) {
                return MethodResult.of(false, "EMPTY_SLOT");
            }
            BlockPos position = turtle.getPosition().offset(x, y, z);
            String err = deployOn(stack, position, forwardDirF, topDirF, options);
            if (err != null) {
                return MethodResult.of(false, err);
            }
            return MethodResult.of(true);
        }, null, null);
    }

    /**
     * @return A nullable string of the error. <code>null</code> means the operation is successful
     */
    @Nullable
    private String deployOn(ItemStack stack, BlockPos position, Direction forward, Direction top, Map<?, ?> options) throws LuaException {
        ITurtleAccess turtle = owner.getTurtle();
        Level world = turtle.getLevel();
        if (forward == null) {
            forward = turtle.getDirection();
        }
        if (top == null) {
            top = Direction.UP;
        }
        TurtlePlayer turtlePlayer = TurtlePlayer.getWithPosition(turtle, position, forward.getOpposite());
        BlockHitResult hit = BlockHitResult.miss(Vec3.atCenterOf(position), top, position);
        DirectionalPlaceContext context = new DirectionalPlaceContext(world, position, forward, stack, top);
        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(turtlePlayer, InteractionHand.MAIN_HAND, position, hit);
        if (event.isCanceled()) {
            return "EVENT_CANCELED";
        }
        Item item = stack.getItem();
        if (!(item instanceof BlockItem)) {
            return "NOT_BLOCK";
        }
        BlockItem block = (BlockItem) item;
        InteractionResult res = block.place(context);
        if (!res.consumesAction()) {
            return "CANNOT_PLACE";
        }
        if (block instanceof SignItem) {
            BlockEntity blockEntity = world.getBlockEntity(position);
            if (blockEntity instanceof SignBlockEntity sign) {
                String text = StringUtil.convertAndToSectionMark(TableHelper.optStringField(options, "text", null));
                setSignText(world, sign, text);
            }
        }
        return null;
    }

    private static void setSignText(Level world, SignBlockEntity sign, String text) {
        if (text == null) {
            for (int i = 0; i < SignBlockEntity.LINES; i++) {
                sign.setMessage(i, Component.literal(""));
            }
        } else {
            String[] lines = text.split("\n");
            for (int i = 0; i < SignBlockEntity.LINES; i++) {
                sign.setMessage(i, Component.literal(i < lines.length ? lines[i] : ""));
            }
        }
        sign.setChanged();
        world.sendBlockUpdated(sign.getBlockPos(), sign.getBlockState(), sign.getBlockState(), Block.UPDATE_ALL);
    }
}
