package moe.ruruke.skyblock.utils

import com.google.gson.reflect.TypeToken
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.features.slayertracker.SlayerBoss
import moe.ruruke.skyblock.features.slayertracker.SlayerTracker
import moe.ruruke.skyblock.utils.pojo.Profile
import moe.ruruke.skyblock.utils.pojo.ProfileMembers
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.Logger
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class APIManager {
    private var firstSwitch = true

    fun onProfileSwitch(profileName: String?) {
        if (profileName != null) {
            val uuid: String = Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", "") // No dashes

            SkyblockAddonsPlus.runAsync {
                if (firstSwitch) {
                    pullPlayer(uuid)
                    firstSwitch = false
                }
                pullProfiles(uuid, profileName)
            }
        }
    }

    fun pullPlayer(uuid: String) {
        logger.info("Grabbing player API data for UUID $uuid...")
        try {
            val url = URL(String.format(PLAYER, uuid))
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", Utils.USER_AGENT)

            logger.info("Got response code " + connection.responseCode)

            val playerData: PlayerData = SkyblockAddonsPlus.getGson().fromJson(
                InputStreamReader(connection.inputStream),
                PlayerData::class.java
            )
            connection.disconnect()

            if (playerData?.getLanguage() != null) {
                main.persistentValuesManager!!.getPersistentValues().setHypixelLanguage(playerData.getLanguage()!!)
            }
        } catch (ex: Exception) {
            logger.warn("Failed to grab player's profiles API data!")
            logger.catching(ex)
        }
    }

    fun pullProfiles(uuid: String, profileName: String) {
        logger.info("Grabbing player's profiles API data for UUID $uuid & profile name $profileName...")
        try {
            val url = URL(String.format(SKYBLOCK_PROFILES, uuid))
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", Utils.USER_AGENT)

            logger.info("Got response code " + connection.responseCode)

            val profiles: Map<String, Profile> = SkyblockAddonsPlus.getGson().fromJson(
                InputStreamReader(connection.inputStream),
                object : TypeToken<HashMap<String?, Profile?>?>() {}.type
            )
            connection.disconnect()

            for ((profileID, profile) in profiles) {
                if (profileName == profile.getCute_name()) {
                    logger.info("Found profile matching $profileName with ID $profileID! Pulling profile data...")
                    pullProfileData(uuid, profileID)
                    return
                }
            }

            logger.info("Did not find profile matching $profileName!")
        } catch (ex: Exception) {
            logger.warn("Failed to grab player's profiles API data!")
            logger.catching(ex)
        }
    }

    fun pullProfileData(uuid: String, profileID: String) {
        logger.info("Grabbing profile API data for UUID $uuid & profile ID $profileID...")
        try {
            val url = URL(String.format(SKYBLOCK_PROFILE, uuid, profileID))
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", Utils.USER_AGENT)

            logger.info("Got response code " + connection.responseCode)

            val profileMembers: ProfileMembers = SkyblockAddonsPlus.getGson().fromJson(
                InputStreamReader(connection.inputStream),
                ProfileMembers::class.java
            )
            connection.disconnect()

            if (profileMembers.getMembers().containsKey(uuid)) {
                val memberData: ProfileMembers.MemberData = profileMembers.getMembers().get(uuid)!!

                val slayers: ProfileMembers.Slayers = memberData.getSlayer()
                if (slayers != null) {
                    val zombie: ProfileMembers.SlayerData? = memberData.getSlayer().getZombie()
                    val spider: ProfileMembers.SlayerData? = memberData.getSlayer().getSpider()
                    val wolf: ProfileMembers.SlayerData? = memberData.getSlayer().getWolf()
                    val enderman: ProfileMembers.SlayerData? = memberData.getSlayer().getEnderman()

                    if (zombie != null && zombie.getKills_tier() != null) {
                        var total = 0
                        for (kills in zombie.getKills_tier().values) {
                            total += kills
                        }
                        SlayerTracker.getInstance().setKillCount(SlayerBoss.REVENANT, total)
                    }

                    if (spider != null && spider.getKills_tier() != null) {
                        var total = 0
                        for (kills in spider.getKills_tier().values) {
                            total += kills
                        }
                        SlayerTracker.getInstance().setKillCount(SlayerBoss.TARANTULA, total)
                    }

                    if (wolf != null && wolf.getKills_tier() != null) {
                        var total = 0
                        for (kills in wolf.getKills_tier().values) {
                            total += kills
                        }
                        SlayerTracker.getInstance().setKillCount(SlayerBoss.SVEN, total)
                    }
                    if (enderman != null && enderman.getKills_tier() != null) {
                        var total = 0
                        for (kills in enderman.getKills_tier().values) {
                            total += kills
                        }
                        SlayerTracker.getInstance().setKillCount(SlayerBoss.VOIDGLOOM, total)
                    }
                }

                val stats: ProfileMembers.Stats = memberData.getStats()
                if (stats != null) {
                    val petMilestones: ProfileMembers.PetMilestones = stats.getPetMilestones()
                    if (petMilestones != null) {
                        main.persistentValuesManager!!.getPersistentValues()
                            .setOresMined(petMilestones.getOreMined())
                        main.persistentValuesManager!!.getPersistentValues()
                            .setSeaCreaturesKilled(petMilestones.getSea_creatures_killed())
                    }
                }

                main.persistentValuesManager!!.saveValues()
            }
        } catch (ex: Exception) {
            logger.warn("Failed to grab profile API data!")
            logger.catching(ex)
        }
    }

    companion object {
        val instance: APIManager = APIManager()

        private const val BASE_URL = "https://api.slothpixel.me/api/"
        private const val PLAYER = BASE_URL + "players/%s" // UUID
        private const val SKYBLOCK_PROFILES = BASE_URL + "skyblock/profiles/%s" // UUID
        private const val SKYBLOCK_PROFILE = BASE_URL + "skyblock/profile/%s/%s" // UUID, Profile

        private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        private val logger: Logger = SkyblockAddonsPlus.getLogger()
    }
}
