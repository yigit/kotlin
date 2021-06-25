// SKIP_TXT
// !LANGUAGE: -DefinitelyNotNullTypeParameters

fun Any.bar() {}
fun Boolean.baz() {}

var x: Int = 0

inline fun <reified T> foo(v: Any?): T {
    if (x == 0) return v as T<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
    if (x == 0) return <!RETURN_TYPE_MISMATCH!>v is T<!><!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
    if (x == 0) return <!RETURN_TYPE_MISMATCH!>v !is T<!><!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
}
