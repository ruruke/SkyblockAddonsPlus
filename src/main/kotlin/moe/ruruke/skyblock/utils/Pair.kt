package moe.ruruke.skyblock.utils

class Pair<K, V>(@JvmField var key: K, @JvmField var value: V) {
    
    override fun toString(): String {
        return "Pair{" +
                "key=" + key +
                ", value=" + value +
                '}'
    }
}
