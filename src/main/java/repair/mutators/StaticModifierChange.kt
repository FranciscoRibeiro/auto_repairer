package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.FieldDeclaration

class StaticModifierChange: MutatorRepair<FieldDeclaration>() {
    override val rank: Int
        get() = 25

    override fun checkedRepair(program: BuggyProgram, fieldDecl: FieldDeclaration): List<FieldDeclaration> {
        val newMods = addOrDeleteStatic(fieldDecl.modifiers)
        return listOf(FieldDeclaration(newMods, fieldDecl.annotations, fieldDecl.variables))
    }

    private fun addOrDeleteStatic(mods: NodeList<Modifier>): NodeList<Modifier> {
        val newMods = mods.map { it.clone() }.toMutableList()
        if(newMods.contains(Modifier.staticModifier())) newMods.remove(Modifier.staticModifier())
        else newMods.add(Modifier.staticModifier())
        return NodeList(newMods)
    }
}
