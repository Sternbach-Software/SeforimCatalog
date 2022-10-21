import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger

fun main() = runBlocking {
    class Foo(var a: Int, var b: Int)
    val foo1 = Foo(1,2)
    val foo2 = Foo(3,4)
    val list1 = mutableListOf<Foo>()
    val list2 = mutableListOf<Foo>()
    list1.add(foo1)
    list1.add(foo2)
    for(item in list1) list2.add(item)
    println(list2[0].a)
    list2[0].a = 0
    println(list1[0].a == 0)
    println(list2[0] === foo1)
    println(list2[1] === foo2)
    println(list2[0] === foo2)
    println(list2[1] === foo1)
    val flow = flow {
        val int = AtomicInteger()
//        while(true) {
            delay(500)
            emit(int.incrementAndGet())
//        }
    }
    launch {
        flow.collect {
            println(it)
        }
    }
    println("Got here")
}