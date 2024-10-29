package moe.ruruke.skyblock.events

import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * This event is fired when a player is revived in Skyblock Dungeons.
 * It includes the usernames of the revived player and their reviver.
 * `EntityPlayer` instances for both of them are included as well if the players are within the client's render
 * distance. Otherwise, they will be `null`.
 *
 * @see codes.biscuit.skyblockaddons.listeners.PlayerListener.onChatReceive
 */
class DungeonPlayerReviveEvent
/**
 * Creates a new instance of `DungeonPlayerReviveEvent` with the given usernames of the revived player and
 * their reviver as well as the `EntityPlayer` instances of both.
 *
 * @param revivedPlayer The `EntityPlayer` instance of the revived player
 * @param reviverPlayer The `EntityPlayer` instance of the reviver
 */(
    /** The player that was revived  */
    val revivedPlayer: EntityPlayer,
    /** The player who did the reviving  */
    val reviverPlayer: EntityPlayer
) : Event()
