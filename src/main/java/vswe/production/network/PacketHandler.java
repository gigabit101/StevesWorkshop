package vswe.production.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vswe.production.gui.container.ContainerTable;
import vswe.production.tileentity.TileEntityTable;


public class PacketHandler
{

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event)
    {
        onPacket(event, FMLClientHandler.instance().getClient().thePlayer, false);
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event)
    {
        onPacket(event, ((NetHandlerPlayServer) event.getHandler()).playerEntity, true);
    }

    private void onPacket(FMLNetworkEvent.CustomPacketEvent event, EntityPlayer player, boolean onServer)
    {
        DataReader dr = new DataReader(event.getPacket().payload());
        PacketId id = dr.readEnum(PacketId.class);
        TileEntityTable table = null;

        if (id.isInInterface())
        {
            if (player.openContainer instanceof ContainerTable)
            {
                table = ((ContainerTable) player.openContainer).getTable();
            }
        } else
        {
            int x = dr.readSignedInteger();
            int y = dr.readSignedInteger();
            int z = dr.readSignedInteger();
            World world = player.worldObj;
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityTable)
            {
                table = (TileEntityTable) te;
            }
        }

        if (table != null)
        {
            if (onServer)
            {
                table.receiveServerPacket(dr, id, player);
            } else
            {
                table.receiveClientPacket(dr, id);
            }
        }
    }

    public static DataWriter getWriter(TileEntityTable table, PacketId id)
    {
        DataWriter dw = new DataWriter();
        dw.writeEnum(id);
        if (!id.isInInterface())
        {
            dw.writeInteger(table.getPos().getX());
            dw.writeInteger(table.getPos().getY());
            dw.writeInteger(table.getPos().getZ());
        }
        return dw;
    }

    public static void sendToPlayer(DataWriter dw, EntityPlayer player)
    {
        dw.sendToPlayer((EntityPlayerMP) player);
    }

    public static void sendToServer(DataWriter dw)
    {
        dw.sendToServer();
    }

}
