package com.georlegacy.general.theatrical.tile;

import com.georlegacy.general.theatrical.tiles.fixtures.TileEntityFresnel;
import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo")
public class TileIlluminator extends TileEntity implements ILightProvider {

    private BlockPos controller;


    public NBTTagCompound getNBT(@Nullable NBTTagCompound nbtTagCompound) {
        if (nbtTagCompound == null) {
            nbtTagCompound = new NBTTagCompound();
        }
        if (controller != null) {
            nbtTagCompound.setInteger("controllerX", controller.getX());
            nbtTagCompound.setInteger("controllerY", controller.getY());
            nbtTagCompound.setInteger("controllerZ", controller.getZ());
        }
        return nbtTagCompound;
    }

    public void readNBT(NBTTagCompound nbtTagCompound) {
        int x = nbtTagCompound.getInteger("controllerX");
        int y = nbtTagCompound.getInteger("controllerY");
        int z = nbtTagCompound.getInteger("controllerZ");
        controller = new BlockPos(x, y, z);
    }


    @Override
    public void readFromNBT(NBTTagCompound compound) {
        readNBT(compound);
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = getNBT(compound);
        return super.writeToNBT(compound);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 1, getNBT(null));
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbtTagCompound = super.getUpdateTag();
        nbtTagCompound = getNBT(nbtTagCompound);
        return nbtTagCompound;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        readNBT(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound tag = pkt.getNbtCompound();
        readNBT(tag);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState,
        IBlockState newState) {
        return (oldState.getBlock() != newState.getBlock());
    }

    public BlockPos getController() {
        return controller;
    }

    public void setController(BlockPos controller) {
        this.controller = controller;
        this.markDirty();
        if (!world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 11);
        }
    }

    @Optional.Method(modid = "albedo")
    @Override
    public Light provideLight() {
        TileEntityFresnel tileEntityFresnel = (TileEntityFresnel) world.getTileEntity(controller);
        if (tileEntityFresnel == null) {
            return null;
        }
        int value = world.getBlockState(pos).getLightValue(world, pos);
        int color = tileEntityFresnel.getGelType().getHex();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return Light.builder().pos(pos)
            .color(r, g, b, ((tileEntityFresnel.getPower() * 0.009F) / 255))
            .radius(Math.max(value / 2, 1)).build();
    }
}