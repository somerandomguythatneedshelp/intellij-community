// "Replace with 'declaringJavaClass'" "true"
// ACTION: Add method contract to 'getDeclaringClass()'
// ACTION: Introduce local variable
// ACTION: Replace with 'declaringJavaClass'
// API_VERSION: 1.7
// WITH_STDLIB

enum class CustomEnum { A; }

fun foo() {
    CustomEnum.A.<caret>declaringClass
}