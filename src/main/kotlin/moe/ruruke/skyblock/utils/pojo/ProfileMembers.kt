package moe.ruruke.skyblock.utils.pojo

class ProfileMembers {
    private val members: HashMap<String, MemberData>? = null

    class MemberData {
        private val slayer: Slayers? = null
        private val stats: Stats? = null
    }

    class Slayers {
        private val zombie: SlayerData? = null
        private val spider: SlayerData? = null
        private val wolf: SlayerData? = null
        private val enderman: SlayerData? = null
    }

    class SlayerData {
        private val kills_tier: HashMap<Int, Int>? = null
    }

    class Stats {
        private val pet_milestones: PetMilestones? = null
    }

    class PetMilestones {
        private val ore_mined = 0
        private val sea_creatures_killed = 0
    }
}
