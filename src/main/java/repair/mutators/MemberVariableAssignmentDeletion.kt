package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.VariableDeclarator

class MemberVariableAssignmentDeletion: MutatorRepair<FieldDeclaration>() {
    override val rank: Int
        get() = 13

    override fun checkedRepair(program: BuggyProgram, fieldDecl: FieldDeclaration): List<FieldDeclaration> {
        val variable = fieldDecl.findFirst(VariableDeclarator::class.java).orElse(null) ?: return emptyList()
        return if(variable.initializer.isPresent){
            listOf(FieldDeclaration(NodeList(fieldDecl.modifiers.map { it.clone() }),
                    VariableDeclarator(variable.type.clone(), variable.nameAsString)))
        } else emptyList()
    }
}
