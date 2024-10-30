package moe.ruruke.skyblock.utils.pojo

class ProfileMembers {
    private val members: HashMap<String, MemberData>? = null

    fun getMembers(): HashMap<String, MemberData> {
        return members!!
    }

    class MemberData {
        private val slayer: Slayers? = null
        private val stats: Stats? = null

        fun getSlayer(): Slayers {
            return slayer!!
        }

        fun getStats(): Stats {
            return stats!!
        }
    }

    class Slayers {
        private val zombie: SlayerData? = null
        private val spider: SlayerData? = null
        private val wolf: SlayerData? = null
        private val enderman: SlayerData? = null

        fun getZombie(): SlayerData? {
            return zombie
        }
        fun getSpider(): SlayerData? {
            return spider
        }

        fun getWolf(): SlayerData? {
            return wolf
        }
        fun getEnderman(): SlayerData? {
            return enderman
        }
    }

    class SlayerData {
        private val kills_tier: HashMap<Int, Int>? = null
        fun getKills_tier(): HashMap<Int, Int> {
            return kills_tier!!
        }
    }

    class Stats {
        private val pet_milestones: PetMilestones? = null
        fun getPetMilestones(): PetMilestones {
            return pet_milestones!!
        }
    }

    class PetMilestones {
        private val ore_mined = 0
        private val sea_creatures_killed = 0
        fun getOreMined(): Int {
            return ore_mined
        }
        fun getSea_creatures_killed(): Int {
            return sea_creatures_killed
        }
    }
}
