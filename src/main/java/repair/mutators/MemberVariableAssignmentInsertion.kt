package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
import repair.mutators.utils.getParent
import java.util.*

class MemberVariableAssignmentInsertion: MutatorRepair<VariableDeclarator>() {
    override val rank: Int
        get() = 13

    override fun checkedRepair(program: BuggyProgram, varDecl: VariableDeclarator): List<VariableDeclarator> {
        val fieldDecl = getParent(varDecl)
        if(fieldDecl == null || fieldDecl !is FieldDeclaration) return emptyList()

        return if(varDecl.initializer.isPresent) { emptyList() }
        else {
            val newVarDecl = varDecl.clone()
            newVarDecl.setInitializer(ObjectCreationExpr(null, varDecl.type.clone() as ClassOrInterfaceType, NodeList()))
            listOf(newVarDecl)
        }

        /*val variable = fieldDecl.findFirst(VariableDeclarator::class.java).orElse(null) ?: return emptyList()
        return if(variable.initializer.isPresent){
            listOf(FieldDeclaration(NodeList(fieldDecl.modifiers.map { it.clone() }),
                    VariableDeclarator(variable.type.clone(), variable.nameAsString)))
        } else emptyList()*/
    }
}
