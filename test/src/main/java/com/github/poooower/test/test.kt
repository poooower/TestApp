package com.github.poooower.test

import org.koin.core.parameter.parametersOf
import org.koin.dsl.module.module
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.getKoin
import org.koin.standalone.inject

data class HelloMessageData(val message: String = "Hello Koin!")

interface HelloService {
    fun hello(): String
}

class HelloServiceImpl(private val helloMessageData: HelloMessageData, private val name: String) : HelloService {
    override fun hello() = "Hey $name, ${helloMessageData.message}"
}

val helloModule = module {
    single { HelloMessageData() }
    module("A") {
        single<HelloService> { (name: String) -> HelloServiceImpl(get(), "A_${name}") }
    }

    module("B") {
        single<HelloService> { (name: String) -> HelloServiceImpl(get(), "(name: String) -> HelloServiceImpl(get(), \"B_${name}") }
    }

    module("C") {
        scope<HelloService>("scope_id") {
            (name: String) -> HelloServiceImpl(get(), "(name: String) -> HelloServiceImpl(get(), \"C_${name}")
        }
    }
}

class HelloApplication : KoinComponent {
    private val helloService: HelloService by inject(name = "C.HelloService") {
        parametersOf("haha")
    }
    fun sayHello() = println(helloService.hello())
}

fun main(vararg args: String) {
    startKoin(listOf(helloModule))

    val app = HelloApplication()
    val session = app.getKoin().getOrCreateScope("scope_id")
    app.sayHello()
    session.close()
}