import com.github.javaparser.JavaToken
import com.github.javaparser.ast.CompilationUnit

class MaskedProgram(val maskedToken: JavaToken, val originalProgram: CompilationUnit): AlternativeProgram() {
    override fun toString(): String {
        val tokens = originalProgram.tokenRange.orElse(null) ?: return ""
        var str = ""
        for(origToken in tokens){
            str = if(origToken === maskedToken) "$str<mask>"
            else "$str${origToken.text}"
        }
        return str
    }
}