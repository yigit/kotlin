// !LANGUAGE: +RepeatableAnnotations
// !USE_EXPERIMENTAL: kotlin.ExperimentalStdlibApi
// TARGET_BACKEND: JVM_IR
// JVM_TARGET: 1.8
// FULL_JDK
// WITH_REFLECT

// Android doesn't have @Repeatable.
// IGNORE_BACKEND: ANDROID

package test

import kotlin.test.assertEquals
import kotlin.reflect.full.*

@java.lang.annotation.Repeatable(As::class)
annotation class A(val value: String)

annotation class As(val value: Array<A>)

@A("1")
@As([A("2"), A("3")])
class Z

@As([A("1"), A("2")])
@A("3")
class ZZ

// Explicit container is not unwrapped.
fun box(): String {
    assertEquals("[@test.A(value=1), @test.As(value=[@test.A(value=2), @test.A(value=3)])]", Z::class.annotations.toString())
    assertEquals("[@test.A(value=1)]", Z::class.findAnnotations<A>().toString())
    assertEquals("@test.A(value=1)", Z::class.findAnnotation<A>().toString())

    assertEquals("[@test.As(value=[@test.A(value=1), @test.A(value=2)]), @test.A(value=3)]", ZZ::class.annotations.toString())
    assertEquals("[@test.A(value=3)]", ZZ::class.findAnnotations<A>().toString())
    assertEquals("@test.A(value=3)", ZZ::class.findAnnotation<A>().toString())

    return "OK"
}
