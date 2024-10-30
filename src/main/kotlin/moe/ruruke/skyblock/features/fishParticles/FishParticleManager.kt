package moe.ruruke.skyblock.features.fishParticles

import net.minecraft.client.Minecraft
import net.minecraft.client.particle.EntityFX
import net.minecraft.client.particle.EntityFishWakeFX
import net.minecraft.util.MathHelper
import kotlin.math.abs
import kotlin.math.sqrt

/**
 *
 * This class allows us to approximate the trail of fish particles made by a fish converging to the player's bobber.
 * To do this, we use a variant on the Bellman Ford algorithm, as well as the exponentiation-by-squaring algorithm
 *
 * In general, we wish to identify those splash particles that belong to a fish converging on the player's bobber.
 * Notice several identifying features of these converging particles:
 * 1) Particles spawn at a radial distance from the cast hook.
 * - That distance decreases by .1 blocks for sequential particles (with a small amount of variation)
 * - During the rain, or in the case of dropped packets, the distance can be .2 blocks.
 * 2) Particles spawn at an angle (think polar coordinates) to the cast hook.
 * - That angle differs based on a gaussian distribution for sequential particles
 * - The gaussian distribution has mean 0 and standard deviation 4 degrees
 * - Based on the distribution, very few sequential particles (<.27%) will differ in angle by more than
 * 3 standard distributions (12 degrees)
 * 3) Sequential particles approaching the hook generally come in every tick, but we can allow for 2 or 3 ticks
 * 4) Particles will not spawn farther than 8 blocks away from the cast hook
 *
 * (These are mostly discoverable in the EntityFishHook.java file. Hopefully, my making this algorithm won't cause servers to go ballistic...)
 *
 * Consideration of time complexity is a huge part of the algorithm.
 * It is an O(n^2) algorithm, which for a large number of particles is unsustainable.
 * Each tick only gives us 50 milliseconds to compute stuff, so we're using bitwise operations.
 * Unfortunately, the longest primitive is 64 bits, so we're limited to tracking 64 particles at the moment.
 * This means when 10+ fishers are in the same spot, we may not be able to link the particles before we overwrite with new ones.
 *
 * Processing the per-particle step takes <.025 milliseconds and the per-tick step generally takes <.050 milliseconds
 * So we are well below any critical thresholds for computation time.
 *
 * @author Phoube
 */
object FishParticleManager {
    /**
     * Fish approach particles converge to the hook at a rate of .1 blocks
     */
    private const val DIST_EXPECTED = .1

    /**
     * Fish approach particles converge to the hook w/ a small distance variation
     */
    private const val DIST_VARIATION = .005

    /**
     * Consecutive particle-angle difference is a gaussian dist. with standard dev. = 4 degrees
     * Taking 3 standard dev. (12 degrees) gives us an extremely good margin of error
     */
    private const val ANGLE_EXPECTED = 12.0

    /**
     * Allow for 4 ticks between particles in a trail
     */
    private const val TIME_VARIATION = 4

    /* Store several metrics about recently spawned particles */
    /**
     * The angle of each particle relative to the player's cast hook
     */
    private val particleAngl = DoubleArray(64)

    /**
     * The distance of each particle relative to the player's cast hook
     */
    private val particleDist = DoubleArray(64)

    /**
     * For each particle combination (i,j), store whether particle i could have followed particle j in a converging trail
     */
    private val particleMatrixRows = LongArray(64)

    /**
     * The position of each particle
     */
    private val particleList: Array<ArrayList<EntityFX>?> = arrayOfNulls(64)//arrayOfNulls<ArrayList<*>>(64)

    /**
     * A set of the positions (designed to filter out the double particles that spawn at the same position)
     */
    private val particleHash = LinkedHashMap<Double, MutableList<EntityFX>>(64)

    /**
     * The time each particle spawned
     */
    private val particleTime = LongArray(64)

    /**
     * Current index
     */
    private var idx = 0

    /**
     * True if particles have spawned/been processed and are currently stored in memory
     */
    private var cacheEmpty = true

    /**
     * The object reference to the overlay renderer
     */
    private val overlay = FishParticleOverlay()

    /**
     * When a new particle spawns, check if it matches these conditions for each recently-spawned particle.
     * For any that do match the criteria, "link" the current particle to the previously-spawned particle.
     * We "link" the particles using a matrix. Set a '1' to matrix entry (i,j) for the particle i that links to j.
     * Do not set a '1' to the entry (j,i). We want to be able to 'link' things ONLY backwards in time.
     *
     * @param fishWakeParticle a newly spawned fish particle
     */
    fun onFishWakeSpawn(fishWakeParticle: EntityFishWakeFX) {
        val hook = Minecraft.getMinecraft().thePlayer.fishEntity
        val xCoord = fishWakeParticle.posX
        val zCoord = fishWakeParticle.posZ

        // It's extremely unlikely that two unrelated particles hash to the same place
        // However, normal fish particles come in pairs of two--the extra one is extraneous and we wish to ignore it
        val hash = 31 * (31 * 23 + xCoord) + zCoord
        if (hook != null && !particleHash.containsKey(hash)) {
            val distToHook =
                sqrt((xCoord - hook.posX) * (xCoord - hook.posX) + (zCoord - hook.posZ) * (zCoord - hook.posZ))
            // Particle trails start 2-8 blocks away. Ignore any that are far away
            if (distToHook > 8) {
                return
            }
            // Save several particle metrics
            particleDist[idx] = distToHook
            particleAngl[idx] = MathHelper.atan2(xCoord - hook.posX, zCoord - hook.posZ) * 180 / Math.PI
            particleTime[idx] = Minecraft.getMinecraft().theWorld.totalWorldTime
            // We want O(1) index lookup and position-hash lookup. Use hashmap and array
            val tmp = ArrayList<EntityFX>(listOf(fishWakeParticle))
            particleList[idx] = tmp
            particleHash[hash] = tmp
            // Use linked hash map's FIFO order to keep the hash at 64 elements
            if (particleHash.size > 64) {
                val itr: MutableIterator<Map.Entry<Double, List<EntityFX>>> = particleHash.entries.iterator()
                while (particleHash.size > 64) {
                    itr.next()
                    itr.remove()
                }
            }
            cacheEmpty = false

            // Begin with a clean row
            var particleRowTmp1: Long = 0
            var particleRowTmp2: Long = 0
            // Mask to zero out the idx th element from a row
            val idxMask = (1L shl (63 - idx)).inv()
            // Update the row for each particle
            for (i in 0..63) {
                // The newest spawned particle should be within a few degrees of the previous particle in the trail

                val anglDiff = abs(particleAngl[i] - particleAngl[idx]) % 360
                val anglMatch = (if (anglDiff > 180) 360 - anglDiff else anglDiff) < ANGLE_EXPECTED
                // The newest spawned particle should have a distance of -.1/-.2 to the previous particle in the trail
                val distDiff1 = abs(particleDist[i] - particleDist[idx] - DIST_EXPECTED)
                val distDiff2 = abs(particleDist[i] - particleDist[idx] - 2 * DIST_EXPECTED)
                // The newest spawned particle should be within a few ticks of the previous particle in the trail
                val timeMatch =
                    (particleTime[idx] - particleTime[i]) <= TIME_VARIATION // Negative (okay) if uninitialized
                // Matrix multiplication assumes we are little endian (most significant bit is column 0)
                // Default: if it's not raining and particles aren't being dropped, we expect .1 block distance
                particleRowTmp1 =
                    particleRowTmp1 or ((if (distDiff1 < DIST_VARIATION && anglMatch && timeMatch) 1L else 0L) shl (63 - i))
                // Special: if it's raining or particles are being dropped, we expect .2 block distance sometimes
                particleRowTmp2 =
                    particleRowTmp2 or ((if (distDiff2 < DIST_VARIATION && anglMatch && timeMatch) 1L else 0L) shl (63 - i))
                // De-link all previous particles from the current one (We want to trace particles back in time, only)
                // Create a bitmask that zeros out the idx position of the ith particle (keep in mind we are in little endian)
                // This step effectively zeros-put the idx column of the matrix
                particleMatrixRows[i] = particleMatrixRows[i] and idxMask
            }
            // If we find no .1 distance particles, go to .2 distance particles...it's not perfect by any means lol
            particleMatrixRows[idx] = if (particleRowTmp1 != 0L) particleRowTmp1 else particleRowTmp2
            // Recalculate the fish trails with the new particle
            calculateTrails()
            // Wrap from 63 to 0
            idx = if (idx < 63) idx + 1 else 0
        } else if (hook != null) {
            particleHash[hash]!!.add(fishWakeParticle)
        } else {
            clearParticleCache()
        }
    }


    /**
     * Find a few (i.e. a "trail" of) particles that each meet the criteria for the subsequent particle in the trail
     * E.g. we find a trail 1 -> 5 -> 6 -> 10 (particle 1 meets the criteria for 5, 5 meets the criteria for 6, and so on)
     * Finding the trail of length n is found by computing M^n, where M is the pairwise matchings for each particle combination
     *
     * Given the particle trail, spawn a distinct particle (lava drip) at the most recently spawned particle in the particle trail.
     */
    private fun calculateTrails() {
        val pow2 = LongArray(64)
        val pow4 = LongArray(64)

        // Main computation:
        // Binary matrix multiplication (wherein we OR the bitwise AND of each row/column to get a matrix entry)
        // This is like a markoff chain, except node connections are binary (either there is one or there isn't)
        // Squaring the adjacency matrix gives the connections "links" between nodes with two degrees of separation.
        // Here we link four particles together, which involves an particleMatrixRows^4 computation
        // After matrix multiplication, any '1's indicate there exists a path with <= matrix power
        bitwiseMatrixSquare(pow2, particleMatrixRows) // Square the matrix
        bitwiseMatrixSquare(pow4, pow2) // Quart the matrix

        // Get the particles at the "head" of any trails of >= 4 particles, beginning at the most recently spawned particle
        // Track which particles have "linked" to previously-iterated particles
        var trailHeadTracker: Long = 0
        var first = true
        val currTick = Minecraft.getMinecraft().theWorld.totalWorldTime
        // Particle idx was the most recently spawned. Start there and go back through the particles.
        var i = idx
        while (first || i != idx) {
            // If this particle is at the head of >= 4 particles
            if (pow4[i] != 0L) {
                // If this particle is at the head of the trail, then it won't link to any other previously iterated particles
                val mask = 1L shl (63 - i)
                if ((trailHeadTracker and mask) == 0L && currTick - particleTime[i] < 5) {
                    for (entityFX in particleList[i]!!) {
                        overlay.addParticle(entityFX)
                    }
                }
                // Keep track of all particles that linked to this particle.
                // Don't add these particles later, as they're not the head of the trail
                trailHeadTracker = trailHeadTracker or particleMatrixRows[i]
            }
            first = false
            i = if (i == 0) 63 else i - 1
        }
    }


    /**
     * Clears matrices, saved elements, etc.
     */
    fun clearParticleCache() {
        if (cacheEmpty) return
        for (i in 0..63) {
            particleMatrixRows[i] = 0
            particleDist[i] = Double.MAX_VALUE
            particleAngl[i] = Double.MAX_VALUE
            particleList[i] = null
            particleTime[i] = Long.MAX_VALUE
        }
        particleHash.clear()
        idx = 0
        overlay.clearParticles()
        cacheEmpty = true
    }


    /**
     * Performs a bitwise square of a matrix
     *
     * Saves the resulting matrix to result in row form
     * @param result saved result of the matrix squaring
     * @param rows input 64 x 64 bit matrix
     */
    private fun bitwiseMatrixSquare(result: LongArray, rows: LongArray) {
        // First, get the column representation of the matrix by copying the matrix
        val cols = LongArray(64)
        // Iterate through columns
        for (j in 0..63) {
            // Zero out column
            cols[j] = 0
            // Create a mask to get jth column from the row
            val mask = 1L shl (63 - j)
            // Iterate through rows
            for (i in 0..63) {
                // Leftshift the i,j entry to the ith position (row) of the jth column
                cols[j] = cols[j] or ((if ((rows[i] and mask) != 0L) 1L else 0L) shl i)
            }
        }
        // Perform the multiplication
        for (i in 0..63) {
            result[i] = 0
            for (j in 0..63) {
                // Leftshift i,j entry to the ith position (row) of the jth column
                result[i] = result[i] or ((if ((rows[i] and cols[j]) != 0L) 1L else 0L) shl (63 - j))
            }
        }
    }
}
