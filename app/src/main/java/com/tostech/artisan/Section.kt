package com.tostech.artisan


internal class Section @JvmOverloads constructor(
    var subheaderPosition: Int,
    var itemCount: Int = 0,
    var isExpanded: Boolean = true
) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val section = o as Section
        if (subheaderPosition != section.subheaderPosition) return false
        return if (itemCount != section.itemCount) false else isExpanded == section.isExpanded
    }

    override fun hashCode(): Int {
        var result = subheaderPosition
        result = 31 * result + itemCount
        result = 31 * result + if (isExpanded) 1 else 0
        return result
    }

    override fun toString(): String {
        return "Section{" +
                "subheaderPosition=" + subheaderPosition +
                ", itemCount=" + itemCount +
                ", isExpanded=" + isExpanded +
                '}'
    }

    companion object {
        @JvmOverloads
        fun create(subheaderPosition: Int, itemCount: Int, isExpanded: Boolean = true): Section {
            return Section(subheaderPosition, itemCount, isExpanded)
        }
    }
}
