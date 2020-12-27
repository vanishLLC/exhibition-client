/**
 * Time: 9:48:52 PM
 * Date: Jan 4, 2017
 * Creator: cool1
 */
package exhibition.module.impl.other;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.management.friend.Friend;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.NetUtil;
import exhibition.util.misc.ChatUtil;
import exhibition.util.misc.PathFind;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S02PacketChat;

/**
 * @author cool1
 *
 */
public class CMDBot extends Module {

	private boolean following;
	private String followName;

	/**
	 * @param data
	 */
	public CMDBot(ModuleData data) {
		super(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see EventListener#onEvent(Event)
	 */
	@Override
	@RegisterEvent(events = { EventPacket.class, EventMotionUpdate.class })
	public void onEvent(Event event) {
		if (event instanceof EventMotionUpdate) {
			EventMotionUpdate e = (EventMotionUpdate) event;
			if (e.isPre()) {
				if (mc.thePlayer.isDead) {
					mc.thePlayer.respawnPlayer();
				}
				if (this.following) {
					try {
						final PathFind pf = new PathFind(this.followName);
						e.setPitch(PathFind.fakePitch - 30.0f);
						e.setYaw(PathFind.fakeYaw);
					} catch (Exception ex) {
					}
				}
			}
		}
		if (event instanceof EventPacket) {
			EventPacket ep = (EventPacket) event;
			if (ep.isIncoming() && ep.getPacket() instanceof S02PacketChat) {
				final S02PacketChat message = (S02PacketChat) ep.getPacket();
				if (message.getChatComponent().getFormattedText().contains("-follow")) {
					for (final Friend friend : FriendManager.friendsList) {
						if (message.getChatComponent().getFormattedText().contains(friend.name)) {
	                        String s = message.getChatComponent().getFormattedText();
	                        s = s.substring(s.indexOf("-follow ") + 8);
	                        s = s.substring(0, s.indexOf("\247"));
							this.following = true;
							this.followName = s;
							break;
						}
					}
				}
				if (message.getChatComponent().getFormattedText().contains("-stopfollow")) {
					for (final Friend friend : FriendManager.friendsList) {
						if (message.getChatComponent().getFormattedText().contains(friend.name)) {
							String s = message.getChatComponent().getFormattedText();
	                        s = s.substring(s.indexOf("-stopfollow ") + 12);
	                        s = s.substring(0, s.indexOf("\247"));
							this.following = false;
							this.followName = "";
							break;
						}
					}
				}
				if (message.getChatComponent().getFormattedText().contains("-amandatodd")) {
					for (final Friend friend : FriendManager.friendsList) {
						if (message.getChatComponent().getFormattedText().contains(friend.name)) {
							for (int index = 0; index < 81; index++) {
								NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(
										mc.thePlayer.posX, mc.thePlayer.posY + 20D, mc.thePlayer.posZ, false));
								NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(
										mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
							}
							break;
						}
					}
				}
				if (message.getChatComponent().getFormattedText().contains("-tpahere")) {
					for (final Friend friend : FriendManager.friendsList) {
						if (message.getChatComponent().getFormattedText().contains(friend.name)) {
							ChatUtil.sendChat("/tpa " + friend.name);
							break;
						}
					}
				}
				if (message.getChatComponent().getFormattedText().contains("-friend ")) {
					for (Object o : mc.theWorld.getLoadedEntityList()) {
						if (o instanceof EntityPlayer) {
							EntityPlayer mod = (EntityPlayer) o;
							if (message.getChatComponent().getFormattedText().contains("-friend " + mod.getName())) {
								for (final Friend friend2 : FriendManager.friendsList) {
									if (message.getChatComponent().getFormattedText().contains(friend2.name)) {
										if (FriendManager.isFriend(mod.getName())) {
											ChatUtil.sendChat(mod.getName() + " is already a friend.");
											break;
										}
										if (!FriendManager.isFriend(mod.getName())) {
											ChatUtil.sendChat(mod.getName() + " has been friended.");
											FriendManager.addFriend(mod.getName(), mod.getName());
											break;
										}
										break;
									}
								}
							}
						}

					}
				}
				if (message.getChatComponent().getFormattedText().contains("-friendremove ")) {
					for (Object o : mc.theWorld.getLoadedEntityList()) {
						if (o instanceof EntityPlayer) {
							EntityPlayer mod = (EntityPlayer) o;
							if (message.getChatComponent().getFormattedText().contains("-friendremove " + mod.getName())) {
								for (final Friend friend2 : FriendManager.friendsList) {
									if (message.getChatComponent().getFormattedText().contains(friend2.name)) {
										if (FriendManager.isFriend(mod.getName())) {
											FriendManager.removeFriend(mod.getName());
											ChatUtil.sendChat(mod.getName() + " has been removed from friends.");
											break;
										}
										if (!FriendManager.isFriend(mod.getName())) {
											ChatUtil.sendChat(mod.getName() + " is not friended.");
											break;
										}
										break;
									}
								}
							}
						}
					}
				}
				if (message.getChatComponent().getFormattedText().contains("-toggle ")) {
					for (final Module mod2 : Client.getModuleManager().getArray()) {
						if (message.getChatComponent().getFormattedText().contains("-toggle " + mod2.getName())) {
							for (final Friend friend2 : FriendManager.friendsList) {
								if (message.getChatComponent().getFormattedText().contains(friend2.name)) {
									mod2.toggle();
									final boolean state = mod2.isEnabled();
									final String s2 = state ? "On" : "Off";
									//ChatUtil.sendChat(mod2.getName() + " is now " + s2);
									break;
								}
							}
						}
					}
				}
			}
		}
	}
}
