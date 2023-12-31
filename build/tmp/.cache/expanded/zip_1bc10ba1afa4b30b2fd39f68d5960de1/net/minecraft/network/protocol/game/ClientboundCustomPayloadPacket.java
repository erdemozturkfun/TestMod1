package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCustomPayloadPacket implements Packet<ClientGamePacketListener>, net.minecraftforge.network.ICustomPacket<ClientboundCustomPayloadPacket> {
   private static final int MAX_PAYLOAD_SIZE = 1048576;
   public static final ResourceLocation BRAND = new ResourceLocation("brand");
   public static final ResourceLocation DEBUG_PATHFINDING_PACKET = new ResourceLocation("debug/path");
   public static final ResourceLocation DEBUG_NEIGHBORSUPDATE_PACKET = new ResourceLocation("debug/neighbors_update");
   public static final ResourceLocation DEBUG_STRUCTURES_PACKET = new ResourceLocation("debug/structures");
   public static final ResourceLocation DEBUG_WORLDGENATTEMPT_PACKET = new ResourceLocation("debug/worldgen_attempt");
   public static final ResourceLocation DEBUG_POI_TICKET_COUNT_PACKET = new ResourceLocation("debug/poi_ticket_count");
   public static final ResourceLocation DEBUG_POI_ADDED_PACKET = new ResourceLocation("debug/poi_added");
   public static final ResourceLocation DEBUG_POI_REMOVED_PACKET = new ResourceLocation("debug/poi_removed");
   public static final ResourceLocation DEBUG_VILLAGE_SECTIONS = new ResourceLocation("debug/village_sections");
   public static final ResourceLocation DEBUG_GOAL_SELECTOR = new ResourceLocation("debug/goal_selector");
   public static final ResourceLocation DEBUG_BRAIN = new ResourceLocation("debug/brain");
   public static final ResourceLocation DEBUG_BEE = new ResourceLocation("debug/bee");
   public static final ResourceLocation DEBUG_HIVE = new ResourceLocation("debug/hive");
   public static final ResourceLocation DEBUG_GAME_TEST_ADD_MARKER = new ResourceLocation("debug/game_test_add_marker");
   public static final ResourceLocation DEBUG_GAME_TEST_CLEAR = new ResourceLocation("debug/game_test_clear");
   public static final ResourceLocation DEBUG_RAIDS = new ResourceLocation("debug/raids");
   public static final ResourceLocation DEBUG_GAME_EVENT = new ResourceLocation("debug/game_event");
   public static final ResourceLocation DEBUG_GAME_EVENT_LISTENER = new ResourceLocation("debug/game_event_listeners");
   private final ResourceLocation identifier;
   private final FriendlyByteBuf data;
   private final boolean shouldRelease;

   public ClientboundCustomPayloadPacket(ResourceLocation pIdentifier, FriendlyByteBuf pData) {
      this.identifier = pIdentifier;
      this.data = pData;
      if (pData.writerIndex() > 1048576) {
         throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
      }
      this.shouldRelease = false; //We are not the owner of the buffer, don't release it, it might be reused.
   }

   public ClientboundCustomPayloadPacket(FriendlyByteBuf pBuffer) {
      this.identifier = pBuffer.readResourceLocation();
      int i = pBuffer.readableBytes();
      if (i >= 0 && i <= 1048576) {
         this.data = new FriendlyByteBuf(pBuffer.readBytes(i));
      } else {
         throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
      }
      this.shouldRelease = true; //We are the owner of the buffer, release it when we are done.
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeResourceLocation(this.identifier);
      pBuffer.writeBytes(this.data.slice()); // Use Slice instead of copy, to not update the read index, allowing packet to be sent multiple times.
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleCustomPayload(this);
      if (this.shouldRelease) this.data.release(); // FORGE: Fix impl buffer leaks (MC-121884), can only be fixed partially, because else you get problems in LAN-Worlds
   }

   public ResourceLocation getIdentifier() {
      return this.identifier;
   }

   public FriendlyByteBuf getData() {
      return new FriendlyByteBuf(this.data.copy());
   }

   @Override public int getIndex() { return Integer.MAX_VALUE; }
   @Override public ResourceLocation getName() { return getIdentifier(); }
   @Override public FriendlyByteBuf getInternalData() { return getData(); }
}
