package mcjty.rftoolsutility.modules.screen.modules;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CapabilityTools;
import mcjty.lib.varia.WorldTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.items.IItemHandler;

public class ItemStackScreenModule implements IScreenModule<ItemStackScreenModule.ModuleDataStacks> {
    private int slot1 = -1;
    private int slot2 = -1;
    private int slot3 = -1;
    private int slot4 = -1;
    protected DimensionType dim = DimensionType.OVERWORLD;
    protected BlockPos coordinate = BlockPosTools.INVALID;


    public static class ModuleDataStacks implements IModuleData {

        public static final String ID = RFToolsUtility.MODID + ":itemStacks";

        private final ItemStack[] stacks = new ItemStack[4];

        @Override
        public String getId() {
            return ID;
        }

        public ModuleDataStacks(ItemStack stack1, ItemStack stack2, ItemStack stack3, ItemStack stack4) {
            this.stacks[0] = stack1;
            this.stacks[1] = stack2;
            this.stacks[2] = stack3;
            this.stacks[3] = stack4;
        }

        public ModuleDataStacks(ByteBuf buf) {
            for (int i = 0 ; i < 4 ; i++) {
                if (buf.readBoolean()) {
                    stacks[i] = NetworkTools.readItemStack(buf);
                } else {
                    stacks[i] = ItemStack.EMPTY;
                }
            }
        }

        public ItemStack getStack(int idx) {
            return stacks[idx];
        }

        @Override
        public void writeToBuf(ByteBuf buf) {
            writeStack(buf, stacks[0]);
            writeStack(buf, stacks[1]);
            writeStack(buf, stacks[2]);
            writeStack(buf, stacks[3]);
        }

        private void writeStack(ByteBuf buf, ItemStack stack) {
            if (!stack.isEmpty()) {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, stack);
            } else {
                buf.writeBoolean(false);
            }
        }
    }

    @Override
    public ModuleDataStacks getData(IScreenDataHelper helper, World worldObj, long millis) {
        World world = WorldTools.getWorld(dim);
        if (world == null) {
            return null;
        }

        if (!WorldTools.chunkLoaded(world, coordinate)) {
            return null;
        }

        TileEntity te = world.getTileEntity(coordinate);
        if (te == null) {
            return null;
        }

        return CapabilityTools.getItemCapabilitySafe(te).map(h -> {
            ItemStack stack1 = getItemStack(h, slot1);
            ItemStack stack2 = getItemStack(h, slot2);
            ItemStack stack3 = getItemStack(h, slot3);
            ItemStack stack4 = getItemStack(h, slot4);
            return new ModuleDataStacks(stack1, stack2, stack3, stack4);
        }).orElse(null);
    }

    private ItemStack getItemStack(IInventory inventory, int slot) {
        if (slot == -1) {
            return ItemStack.EMPTY;
        }
        if (slot < inventory.getSizeInventory()) {
//            if (RFTools.instance.mfr && MFRCompatibility.isExtendedStorage(inventory)) {
//                return MFRCompatibility.getContents(inventory);
//            } else if (RFTools.instance.jabba && MFRCompatibility.isExtendedStorage(inventory)) {
//                return MFRCompatibility.getContents(inventory);
//            }
            return inventory.getStackInSlot(slot);
        } else {
            return ItemStack.EMPTY;
        }
    }

    private ItemStack getItemStack(IItemHandler itemHandler, int slot) {
        if (slot == -1) {
            return ItemStack.EMPTY;
        }
        if (slot < itemHandler.getSlots()) {
//            if (RFTools.instance.mfr && MFRCompatibility.isExtendedStorage(inventory)) {
//                return MFRCompatibility.getContents(inventory);
//            } else if (RFTools.instance.jabba && MFRCompatibility.isExtendedStorage(inventory)) {
//                return MFRCompatibility.getContents(inventory);
//            }
            return itemHandler.getStackInSlot(slot);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, DimensionType dim, BlockPos pos) {
        if (tagCompound != null) {
            setupCoordinateFromNBT(tagCompound, dim, pos);
            if (tagCompound.contains("slot1")) {
                slot1 = tagCompound.getInt("slot1");
            }
            if (tagCompound.contains("slot2")) {
                slot2 = tagCompound.getInt("slot2");
            }
            if (tagCompound.contains("slot3")) {
                slot3 = tagCompound.getInt("slot3");
            }
            if (tagCompound.contains("slot4")) {
                slot4 = tagCompound.getInt("slot4");
            }
        }
    }

    protected void setupCoordinateFromNBT(CompoundNBT tagCompound, DimensionType dim, BlockPos pos) {
        coordinate = BlockPosTools.INVALID;
        if (tagCompound.contains("monitorx")) {
            if (tagCompound.contains("monitordim")) {
                this.dim = DimensionType.byName(new ResourceLocation(tagCompound.getString("monitordim")));
            } else {
                // Compatibility reasons
                this.dim = DimensionType.byName(new ResourceLocation(tagCompound.getString("dim")));
            }
            if (dim == this.dim) {
                BlockPos c = new BlockPos(tagCompound.getInt("monitorx"), tagCompound.getInt("monitory"), tagCompound.getInt("monitorz"));
                int dx = Math.abs(c.getX() - pos.getX());
                int dy = Math.abs(c.getY() - pos.getY());
                int dz = Math.abs(c.getZ() - pos.getZ());
                if (dx <= 64 && dy <= 64 && dz <= 64) {
                    coordinate = c;
                }
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.ITEMSTACK_RFPERTICK.get();
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, PlayerEntity player) {

    }
}