package fault_localization.reports

import org.json.JSONObject
import java.io.File

class Nodes {
    val nodes = LinkedHashMap<Int, NodeInfo>()

    constructor(projPath: String) {
        File("$projPath/target/qsfl/nodes.txt").forEachLine {
            line ->
            var jsonObj = JSONObject(line)
            var id = jsonObj.getInt("id")
            var line = jsonObj.getInt("line")
            var name = jsonObj.getString("name")
            var parentId = jsonObj.getInt("parentId")
            var type = jsonObj.getString("type")
            this[id] = NodeInfo(line, name, parentId, type)
        }
    }

    constructor(nodesFile: File){
        nodesFile.forEachLine {
            line ->
            var jsonObj = JSONObject(line)
            var id = jsonObj.getInt("id")
            var line = jsonObj.getInt("line")
            var name = jsonObj.getString("name")
            var parentId = jsonObj.getInt("parentId")
            var type = jsonObj.getString("type")
            this[id] = NodeInfo(line, name, parentId, type)
        }
    }

    operator fun set(id: Int, nodeInfo: NodeInfo) {
        nodes.put(id, nodeInfo)
    }

    operator fun get(faultyNodeId: Int): NodeInfo? {
        return nodes[faultyNodeId]
    }
}
