package net.astrorbits.doNotDoIt.criteria

import com.google.gson.JsonObject
import net.astrorbits.doNotDoIt.team.TeamData

abstract class CriteriaListener {
    abstract val type: CriteriaType
    abstract val placeholder: String
    lateinit var parm: String
    lateinit var displayName: String



    data class SubCriteria(val id: Int, val params: List<String>, val displayName: String)

    open fun trigger(team: TeamData, type: CriteriaType) {
        
    }

    /**
     * 将名称格式化为`MiniMessage`的形式
     */
    open fun formatName(rawName: String, index: Int): String {
        return rawName
    }

    /**
     * 为了防止[formatName]调用尚未初始化的字段，应将`super.readFromJson(json)`放在方法末尾调用
     */
    open fun readFromJson(json: JsonObject) {
        displayName = formatName(json.get("name").asString,0)
    }
}
