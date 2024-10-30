package moe.ruruke.skyblock.features

import moe.ruruke.skyblock.core.EntityAggregate
import moe.ruruke.skyblock.utils.ItemUtils
import moe.ruruke.skyblock.utils.TextUtils
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.AxisAlignedBB
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

class JerryPresent(
    present: UUID?,
    fromLine: UUID?,
    toLine: UUID?,
    private val presentColor: PresentColor?,
    private val _isFromPlayer: Boolean,
    private val _isForPlayer: Boolean,
) :
    EntityAggregate(present, fromLine, toLine) {
    fun getPresentColor(): PresentColor? {
        return presentColor
    }

    fun isFromPlayer(): Boolean {
        return _isFromPlayer
    }
    fun isForPlayer(): Boolean {
        return _isForPlayer
    }

    val thePresent: UUID
        /**
         * Armor stand with the present-colored skull
         */
        get() = this.getEntities().get(0)

    val lowerDisplay: UUID
        /**
         * Armor stand with "From: [RANK] Username"
         */
        get() = this.getEntities().get(1)

    val upperDisplay: UUID
        /**
         * Armor stand with "CLICK TO OPEN" or "To: [RANK] Username"
         */
        get() = this.getEntities().get(2)

    fun shouldHide(): Boolean {
        return !_isForPlayer && !_isFromPlayer
    }

    enum class PresentColor(private val skullID: String) {
        WHITE("7732c5e4-1800-3b90-a70f-727d2969254b"),
        GREEN("d5eb6a2a-3f10-3d6b-ba6a-4d46bb58a5cb"),
        RED("bc74cb05-2758-3395-93ec-70452a983604");

        companion object {
            fun fromSkullID(skullID: String): PresentColor? {
                for (presentColor in entries) {
                    if (presentColor.skullID == skullID) {
                        return presentColor
                    }
                }

                return null
            }
        }
    }

    companion object {
        private val FROM_TO_PATTERN: Pattern = Pattern.compile("(?:From:|To:) (?:\\[.*?] )?(?<name>\\w{1,16})")

        private val jerryPresents: HashMap<UUID, JerryPresent> = HashMap()
        fun getJerryPresents(): HashMap<UUID, JerryPresent> {
            return jerryPresents
        }

        /**
         * Returns an instance of JerryPresent if this entity is in fact part of a jerry
         * present, or null if not.
         */
        fun getJerryPresent(targetEntity: Entity): JerryPresent? {
            if (targetEntity !is EntityArmorStand || !targetEntity.isInvisible) {
                return null
            }

            // Check if this present already exists...
            for (present in jerryPresents.values) {
                if (present.getEntities().contains(targetEntity.uniqueID)) {
                    return present
                }
            }

            // Check a small range around...
            val stands: List<EntityArmorStand> =
                Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB<EntityArmorStand>(
                    EntityArmorStand::class.java,
                    AxisAlignedBB(
                        targetEntity.posX - 0.1, targetEntity.posY - 2, targetEntity.posZ - 0.1,
                        targetEntity.posX + 0.1, targetEntity.posY + 2, targetEntity.posZ + 0.1
                    )
                )

            var present: EntityArmorStand? = null
            var fromLine: EntityArmorStand? = null
            var toLine: EntityArmorStand? = null
            var presentColor: PresentColor? = null
            for (stand in stands) {
                if (!stand.isInvisible()) {
                    continue
                }

                if (stand.hasCustomName()) {
                    val name = TextUtils.stripColor(stand.getCustomNameTag())

                    // From line (middle)
                    if (name.startsWith("From: ")) {
                        fromLine = stand

                        // To line (top)
                    } else if (name == "CLICK TO OPEN" || name.startsWith("To: ")) {
                        toLine = stand
                    }
                } else {
                    val skullID: String = ItemUtils.getSkullOwnerID(stand.getEquipmentInSlot(4)) ?: continue

                    val standColor = PresentColor.fromSkullID(skullID) ?: continue

                    // Present stand (bottom)
                    present = stand
                    presentColor = standColor
                }
            }
            // Verify that we've found all parts, and that the positions make sense
            if (present == null || fromLine == null || toLine == null || present.posY > fromLine.posY || fromLine.posY > toLine.posY) {
                return null
            }

            val matcher = FROM_TO_PATTERN.matcher(TextUtils.stripColor(fromLine.getCustomNameTag()))
            if (!matcher.matches()) {
                return null
            }
            val name = matcher.group("name")

            val fromYou = name == Minecraft.getMinecraft().thePlayer.getName()
            val forYou = TextUtils.stripColor(toLine.getCustomNameTag()).equals("CLICK TO OPEN")

            return JerryPresent(
                present.getUniqueID(),
                fromLine.getUniqueID(),
                toLine.getUniqueID(),
                presentColor,
                fromYou,
                forYou
            )
        }
    }
}
