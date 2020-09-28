@file:JvmName("AgentAppender")

package eu.musicnova.lazyloadagend

import com.sun.tools.attach.VirtualMachine

fun main(args: Array<String>) {
    val vm = VirtualMachine.attach(args.component1())
    vm.loadAgent(args.component2())
    vm.detach()
}