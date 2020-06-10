package fault_localization.reports.qsfl

import org.json.JSONObject
import java.io.File

class Nodes {
    val nodes = LinkedHashMap<Int, NodeInfo>()

    constructor(projPath: String) {
        File("$projPath/target/qsfl/nodes.txt").forEachLine { createNodeInfo(it) }
    }

    constructor(nodesFile: File){
        if(nodesFile.exists()) nodesFile.forEachLine { createNodeInfo(it) } else return
    }

    private fun createNodeInfo(jsonString: String) {
        var jsonObj = JSONObject(jsonString)
        var id = jsonObj.getInt("id")
        var type = jsonObj.getString("type")
        this[id] = when(type){
            "CLASS" -> Class(id, getName(jsonObj))
            "METHOD" -> Method(id, getName(jsonObj), getParentId(jsonObj))
            "PARAMETER" -> Parameter(id, getName(jsonObj), getParentId(jsonObj))
            "LANDMARK" -> Landmark(id, getName(jsonObj), getParentId(jsonObj))
            "LINE" -> Line(id, getLine(jsonObj), getParentId(jsonObj))
            else -> Undefined(id)
        }
    }

    operator fun set(id: Int, nodeInfo: NodeInfo) {
        nodes.put(id, nodeInfo)
    }

    operator fun get(faultyNodeId: Int): NodeInfo? {
        return nodes[faultyNodeId]
    }

    private fun getName(jsonObj: JSONObject): String {
        return jsonObj.getString("name")
    }

    private fun getParentId(jsonObj: JSONObject): Int {
        return jsonObj.getInt("parentId")
    }

    private fun getLine(jsonObj: JSONObject): Int {
        return jsonObj.getInt("line")
    }
}
