package moe.ruruke.skyblock.events

import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.player.PlayerEvent

/**
 * This event is fired when a player dies in Skyblock.
 * It includes the `EntityPlayer` of the player who died, their username and the cause of death.
 * The [PlayerEvent.entityPlayer] may be `null` if a player dies outside of the client's render distance.
 *
 * @see codes.biscuit.skyblockaddons.listeners.PlayerListener.onChatReceive
 */
class SkyblockPlayerDeathEvent
/**
 * Creates a new instance of `SkyblockPlayerDeathEvent` with the given `EntityPlayer`,
 * and cause of death. `player` can be `null` if the entity isn't loaded.
 *
 * @param player the player who died
 * @param cause the player's cause of death
 */(
    player: EntityPlayer?,
    /** The username of the player who died  */
    val username: String,
    /** The player's cause of death  */
    val cause: String
) : PlayerEvent(player)
