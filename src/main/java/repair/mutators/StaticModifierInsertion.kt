package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.FieldDeclaration

class StaticModifierInsertion: MutatorRepair<FieldDeclaration>() {
    override val rank: Int
        get() = 25

    override fun checkedRepair(program: BuggyProgram, fieldDecl: FieldDeclaration): List<FieldDeclaration> {
        val mods = fieldDecl.modifiers
        return if(mods.contains(Modifier.staticModifier())) emptyList()
        else {
            val newMods = mods.map { it.clone() }.toMutableList()
            newMods.add(Modifier.staticModifier())
            listOf(FieldDeclaration(NodeList(newMods), fieldDecl.annotations, fieldDecl.variables))
        }
    }
}
