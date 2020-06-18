package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import repair.mutators.utils.getParent

class AccessorModifierChange: MutatorRepair<Modifier>() {
    override val rank: Int
        get() = 16

    override fun checkedRepair(program: BuggyProgram, mod: Modifier): List<Modifier> {
        val parent = getParent(mod) ?: return emptyList()
        return if((parent is FieldDeclaration || parent is MethodDeclaration)
                && mod == Modifier.publicModifier()){
            listOf(Modifier.privateModifier(), Modifier.protectedModifier())
        } else emptyList()
    }

}
