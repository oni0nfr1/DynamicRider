package io.github.oni0nfr1.dynamicrider.client.command.debug

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CommandVar(val name: String)
