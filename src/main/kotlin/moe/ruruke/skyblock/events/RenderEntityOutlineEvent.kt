package moe.ruruke.skyblock.events

import moe.ruruke.skyblock.events.RenderEntityOutlineEvent.Type
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItemFrame
import net.minecraftforge.fml.common.eventhandler.Event
import java.util.function.Consumer
import java.util.function.Function

/**
 * Event that is fired by [moe.ruruke.skyblock.features.EntityOutlines.EntityOutlineRenderer] to determine which entities will be outlined.
 * The event is fired twice each tick, first for the [Type.XRAY] phase, and second for the [Type.NO_XRAY] phase.
 * Event handlers can add entities/colors to be outlined for either phase using the [.queueEntitiesToOutline] event function
 * The resulting list of entities/associated colors is outlined after both events have been handled
 */
class RenderEntityOutlineEvent(
    /**
     * The phase of the event (see [Type]
     */
    private val type: Type,
    /**
     * The entities we can outline. Note that this set and [.entitiesToOutline] are disjoint at all times.
     */
    private var entitiesToChooseFrom: HashSet<Entity>?
) :
    Event() {

    /**
     * The entities to outline. This is progressively cumulated from [.entitiesToChooseFrom]
     */
    fun getType(): Type {
        return type
    }
    fun getEntitiesToChooseFrom(): HashSet<Entity>? {
        return entitiesToChooseFrom
    }
    private var entitiesToOutline: HashMap<Entity, Int>? = null
    fun getEntitiesToOutline(): HashMap<Entity, Int>? {
        return entitiesToOutline
    }

    /**
     * Constructs the event, given the type and optional entities to outline.
     *
     *
     * This will modify {@param potentialEntities} internally, so make a copy before passing it if necessary.
     *
     * @param theType           of the event (see [Type]
     */
    init {
        if (entitiesToChooseFrom != null) {
            entitiesToOutline = HashMap(entitiesToChooseFrom!!.size)
        }
    }

    /**
     * Conditionally queue entities around which to render entities
     * Selects from the pool of [.entitiesToChooseFrom] to speed up the predicate testing on subsequent calls.
     * Is more efficient (theoretically) than calling [.queueEntityToOutline] for each entity because lists are handled internally.
     *
     *
     * This function loops through all entities and so is not very efficient.
     * It's advisable to encapsulate calls to this function with global checks (those not dependent on an individual entity) for efficiency purposes.
     *
     * @param outlineColor a function to test
     */
    fun queueEntitiesToOutline(outlineColor: Function<Entity, Int?>) {
        if (outlineColor == null) {
            return
        }
        if (entitiesToChooseFrom == null) {
            computeAndCacheEntitiesToChooseFrom()
        }
        val itr = entitiesToChooseFrom!!.iterator()
        while (itr.hasNext()) {
            val e = itr.next()
            val i = outlineColor.apply(e)
            if (i != null) {
                entitiesToOutline!![e] = i
                itr.remove()
            }
        }
    }

    /**
     * Adds a single entity to the list of the entities to outline
     *
     * @param entity       the entity to add
     * @param outlineColor the color with which to outline
     */
    fun queueEntityToOutline(entity: Entity?, outlineColor: Int) {
        if (entity == null) {
            return
        }
        if (entitiesToChooseFrom == null) {
            computeAndCacheEntitiesToChooseFrom()
        }
        if (!entitiesToChooseFrom!!.contains(entity)) {
            return
        }
        entitiesToOutline!![entity] = outlineColor
        entitiesToChooseFrom!!.remove(entity)
    }

    /**
     * Used for on-the-fly generation of entities. Driven by event handlers in a decentralized fashion
     */
    private fun computeAndCacheEntitiesToChooseFrom() {
        val entities: List<Entity> = Minecraft.getMinecraft().theWorld.getLoadedEntityList()
        // Only render outlines around non-null entities within the camera frustum
        entitiesToChooseFrom = HashSet(entities.size)
        // Only consider entities that aren't invisible armorstands to increase FPS significantly
        entities.forEach(Consumer<Entity> { e: Entity? ->
            if (e != null && !(e is EntityArmorStand && e.isInvisible) && e !is EntityItemFrame) {
                entitiesToChooseFrom!!.add(e)
            }
        })
        entitiesToOutline = HashMap(entitiesToChooseFrom!!.size)
    }

    /**
     * The phase of the event.
     * [.XRAY] means that this directly precedes entities whose outlines are rendered through walls (Vanilla 1.9+)
     * [.NO_XRAY] means that this directly precedes entities whose outlines are rendered only when visible to the client
     */
    enum class Type {
        XRAY,
        NO_XRAY
    }


    class EntityAndOutlineColor(private val entity: Entity, private val color: Int) {
        fun getEntity(): Entity {
            return entity
        }

        fun getColor(): Int {
            return color;
        }

        override fun hashCode(): Int {
            return entity.hashCode()
        }
    }
}
