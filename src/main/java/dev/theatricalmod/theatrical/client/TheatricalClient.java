package dev.theatricalmod.theatrical.client;

import dev.theatricalmod.theatrical.TheatricalCommon;
import dev.theatricalmod.theatrical.api.capabilities.dmx.provider.DMXProvider;
import dev.theatricalmod.theatrical.api.capabilities.dmx.receiver.DMXReceiver;
import dev.theatricalmod.theatrical.api.capabilities.dmx.receiver.IDMXReceiver;
import dev.theatricalmod.theatrical.api.fixtures.Fixture;
import dev.theatricalmod.theatrical.block.TheatricalBlocks;
import dev.theatricalmod.theatrical.client.gui.container.TheatricalContainers;
import dev.theatricalmod.theatrical.client.gui.screen.*;
import dev.theatricalmod.theatrical.client.tile.TileEntityRendererBasicLightingDesk;
import dev.theatricalmod.theatrical.tiles.TheatricalTiles;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeBuffers;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static dev.theatricalmod.theatrical.client.tile.TheatricalRenderType.MAIN_BEAM;

public class TheatricalClient extends TheatricalCommon {

    public static IBakedModel testModel;

    @Override
    public void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::textureStitch);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::modelload);
        MinecraftForge.EVENT_BUS.addListener(this::worldRenderLastEvent);
    }

    public void setup(FMLClientSetupEvent event){
        RenderType cutout = RenderType.getCutout();
        RenderTypeLookup.setRenderLayer(TheatricalBlocks.TRUSS.get(), cutout);
        RenderTypeLookup.setRenderLayer(TheatricalBlocks.MOVING_LIGHT.get(), cutout);
        RenderTypeLookup.setRenderLayer(TheatricalBlocks.GENERIC_LIGHT.get(), cutout);
        RenderTypeLookup.setRenderLayer(TheatricalBlocks.BASIC_LIGHTING_DESK.get(), cutout);
        ClientRegistry.bindTileEntityRenderer(TheatricalTiles.MOVING_LIGHT.get(), MovingLightRenderer::new);
        ClientRegistry.bindTileEntityRenderer(TheatricalTiles.GENERIC_LIGHT.get(), MovingLightRenderer::new);
        ClientRegistry.bindTileEntityRenderer(TheatricalTiles.BASIC_LIGHTING_DESK.get(), TileEntityRendererBasicLightingDesk::new);
        ScreenManager.registerFactory(TheatricalContainers.INTELLIGENT_FIXTURE.get(), ScreenIntelligentFixture::new);
        ScreenManager.registerFactory(TheatricalContainers.ARTNET_INTERFACE.get(), ScreenArtNetInterface::new);
        ScreenManager.registerFactory(TheatricalContainers.GENERIC_FIXTURE.get(), ScreenGenericFixture::new);
        ScreenManager.registerFactory(TheatricalContainers.DIMMER_RACK.get(), ScreenDimmerRack::new);
        ScreenManager.registerFactory(TheatricalContainers.BASIC_LIGHTING_CONSOLE.get(), ScreenBasicLightingConsole::new);
        ScreenManager.registerFactory(TheatricalContainers.DMX_REDSTONE_INTERFACE.get(), ScreenDMXRedstoneInterface::new);
//        ModelLoaderRegistry.registerLoader(new ResourceLocation(TheatricalMod.MOD_ID, "cable"), new CableModelLoader());
    }

    @SubscribeEvent
    public void worldRenderLastEvent(RenderWorldLastEvent event){
//        Minecraft.getInstance().getBufferBuilders().getBlockBufferBuilders().get(MAIN_BEAM).finishDrawing();
//        Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish(MAIN_BEAM);
//       Minecraft.getInstance().getBufferBuilders().getBlockBufferBuilders().get()
    }

    public void modelload(ModelRegistryEvent event){
        for(Fixture fixture : Fixture.getRegistry()){
            ModelLoader.addSpecialModel(fixture.getHookedModelLocation());
            ModelLoader.addSpecialModel(fixture.getPanModelLocation());
            ModelLoader.addSpecialModel(fixture.getStaticModelLocation());
            ModelLoader.addSpecialModel(fixture.getTiltModelLocation());
        }
    }

    public static BlockRayTraceResult getLookingAt(PlayerEntity player, double range)
    {
        return getLookingAt(player, range, RayTraceContext.FluidMode.NONE);
    }

    public static BlockRayTraceResult getLookingAt(PlayerEntity player, double range, RayTraceContext.FluidMode fluidMode)
    {
        World world = player.world;

        Vector3d look = player.getLookVec();
        Vector3d startPos = getVec3d(player).add(0, player.getEyeHeight(), 0);
        Vector3d endPos = startPos.add(look.mul(range, range, range));
        RayTraceContext context = new RayTraceContext(startPos, endPos, RayTraceContext.BlockMode.OUTLINE, fluidMode, player);
        return world.rayTraceBlocks(context);
    }


    @Override
    public World getWorld() {
        return Minecraft.getInstance().world;
    }

    public static Vector3d getVec3d(Entity entity)
    {
        return entity.getPositionVec();
    }

    public void textureStitch(TextureStitchEvent.Pre event){
        for(Fixture fixture : Fixture.getRegistry()){
            Arrays.stream(fixture.getTextures()).forEach(event::addSprite);
        }
    }

    public void handleDMXUpdate(BlockPos pos, byte[] data){
        TileEntity tileFresnel = getWorld().getTileEntity(pos);
        if(tileFresnel != null) {
            IDMXReceiver dmxReceiver = tileFresnel.getCapability(DMXReceiver.CAP, Direction.NORTH).orElse(null);
            if (dmxReceiver != null) {
                if (data != null) {
                    for (int i = 0; i < data.length; i++) {
                        dmxReceiver
                            .updateChannel(i, data[i]);
                    }
                }
            }
        }
    }

    public void handleProviderDMXUpdate(BlockPos pos, byte[] data){
        TileEntity tile = getWorld().getTileEntity(pos);
        if(tile != null){
            tile.getCapability(DMXProvider.CAP, Direction.NORTH).ifPresent(idmxProvider -> {
                idmxProvider.getUniverse(getWorld()).setDmxChannels(data);
                idmxProvider.refreshDevices();
                idmxProvider.updateDevices(getWorld(), pos);
            });
        }
    }
}
