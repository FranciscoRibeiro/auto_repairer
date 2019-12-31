package visitors

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter

class MutatorVisitor : VoidVisitorAdapter<Triple<BinaryExpr, CompilationUnit, MutableList<CompilationUnit>>>() {
    enum class CMP_OP {
        LESS, LESS_EQUALS, GREATER, GREATER_EQUALS, EQUALS
    }

    enum class LOGIC_OP {
        OR, AND
    }

    override fun visit(n: BinaryExpr?, arg: Triple<BinaryExpr, CompilationUnit, MutableList<CompilationUnit>>?) {
        super.visit(n, arg)
        if (n != null && arg != null) {
            if(n.equals(arg.first)){
                val originalOp = n.operator.name
                if(originalOp == "OR" || originalOp == "AND") {
                    for (op in LOGIC_OP.values()) {
                        if (op.name != originalOp) {
                            when (op) {
                                LOGIC_OP.AND -> n.setOperator(BinaryExpr.Operator.AND)
                                LOGIC_OP.OR -> n.setOperator(BinaryExpr.Operator.OR)
                            }
                            arg.third.add(arg.second.clone())
                        }
                    }
                }
                else{
                    for(op in CMP_OP.values()){
                        if(op.name != originalOp){
                            when (op) {
                                CMP_OP.LESS -> n.setOperator(BinaryExpr.Operator.LESS)
                                CMP_OP.LESS_EQUALS -> n.setOperator(BinaryExpr.Operator.LESS_EQUALS)
                                CMP_OP.GREATER -> n.setOperator(BinaryExpr.Operator.GREATER)
                                CMP_OP.GREATER_EQUALS -> n.setOperator(BinaryExpr.Operator.GREATER_EQUALS)
                                CMP_OP.EQUALS -> n.setOperator(BinaryExpr.Operator.EQUALS)
                            }
                            arg.third.add(arg.second.clone())
                        }
                    }
                }
            }
        }
    }
}
