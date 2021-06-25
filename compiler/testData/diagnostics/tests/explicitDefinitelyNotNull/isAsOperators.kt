// SKIP_TXT
// !LANGUAGE: -DefinitelyNotNullTypeParameters

fun Any.bar() {}
fun Boolean.baz() {}

var x: Int = 0

inline fun <reified T> foo(v: Any?): T {
    if (x == 0) return v as T<!UNNECESSARY_NOT_NULL_ASSERTION, UNREACHABLE_CODE!>!!<!>
    if (x == 0) return <!TYPE_MISMATCH!>v is T<!><!UNNECESSARY_NOT_NULL_ASSERTION, UNREACHABLE_CODE!>!!<!>
    if (x == 0) return <!TYPE_MISMATCH!>v !is T<!><!UNNECESSARY_NOT_NULL_ASSERTION, UNREACHABLE_CODE!>!!<!>
<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
