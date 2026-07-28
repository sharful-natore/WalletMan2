import java.lang.reflect.Field
fun main() {
    val clazz = Class.forName("com.k2fsa.sherpa.onnx.OnlineRecognizerConfig")
    for (f in clazz.declaredFields) {
        println(f.name)
    }
}
