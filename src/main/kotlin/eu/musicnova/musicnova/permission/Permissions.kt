package eu.musicnova.musicnova.permission

import de.phyrone.brig.wrapper.StringArgument
import de.phyrone.brig.wrapper.getArgument
import de.phyrone.brig.wrapper.literal
import de.phyrone.brig.wrapper.runs
import eu.musicnova.musicnova.database.dao.*
import eu.musicnova.musicnova.utils.TerminalCommandDispatcher
import org.springframework.stereotype.Component
import java.io.Serializable
import javax.annotation.PostConstruct
import javax.persistence.Entity

@Component
class PermissionManager(
    val permissionEntityDatabase: PermissionEntityDatabase,
    val permissionClientEntityDatabase: PermissionClientEntityDatabase,
    val permissionGroupEntityDatabase: PermissionGroupEntityDatabase,
    val terminalCommandDispatcher: TerminalCommandDispatcher
) {

    @PostConstruct
    fun initTerminalPermCommands() {
        terminalCommandDispatcher.literal("permissions") {
            alias("perms")
            alias("perm")
            literal("group") {
                literal("list") {
                    runs {
                        val groups = permissionGroupEntityDatabase.findAll()

                        println("Groups:")
                        groups.forEach { group ->
                            println("  - ${group.name}(uuid=${group.id})")
                        }
                    }
                }
                literal("create") {
                    argument("name", StringArgument) {
                        runs { context ->
                            val groupName = context.getArgument<String>("name")
                            if (permissionGroupEntityDatabase.existsByName(groupName)) {
                                println("group already exist")
                            } else {
                                val group = PersistentPermissionGroupEntityData(groupName)
                                permissionGroupEntityDatabase.save(group)
                                println("group created")
                            }
                        }
                    }
                }
                argument("group", StringArgument) {
                    literal("add") {
                        literal("parent") {
                            argument("parent group", StringArgument) {
                                runs { context ->
                                    val currentGroupName = context.getArgument<String>("group")
                                    val parentGroupName = context.getArgument<String>("parent group")
                                    val group = permissionGroupEntityDatabase.getByName(currentGroupName)
                                    val parentGroup = permissionGroupEntityDatabase.getByName(parentGroupName)
                                    if (group != null) {
                                        if (parentGroup != null) {
                                            if (group.parents.contains(parentGroup)) {
                                                group.parents += parentGroup
                                                permissionGroupEntityDatabase.save(group)
                                                println("done")
                                            } else {
                                                println("is already parent group")
                                            }
                                        } else {
                                            println("parent group not found")
                                        }
                                    } else {
                                        println("group not found")
                                    }
                                }
                            }
                        }
                        literal("permission") {
                            argument("permission", StringArgument) {
                                runs { context ->
                                    val groupName = context.getArgument<String>("group")
                                    val permission = context.getArgument<String>("permission")
                                    val group = permissionGroupEntityDatabase.getByName(groupName)
                                    if (group != null) {
                                        group.permissions += PersistentPermissionEntryData(permission)
                                        permissionGroupEntityDatabase.save(group)
                                        println("done")
                                    } else {
                                        println("group not found")
                                    }
                                }
                            }
                        }
                    }
                    literal("delete") {
                        runs { context ->
                            val groupName = context.getArgument<String>("group")
                            when (val result = permissionGroupEntityDatabase.deleteByName(groupName)) {
                                1 -> println("deleted successfully")
                                0 -> println("not found")
                                else -> println("deleted $result groups")
                            }
                        }
                    }
                }
            }
        }
    }

    private inner class PermissionEntityImpl(override val identifier: String, override val platform: String) :
        PermissionEntity {

        fun getPermissions(permission: String) {

        }

        override fun hasPermission(permission: String, minLevel: Int): Boolean? {

            TODO()
        }

        override fun getPermissionLevel(permission: String): Int? {
            TODO("Not yet implemented")
        }

        override fun setPermission(
            permission: String,
            level: Int,
            identificationStrategy: PermissionIdentificationStrategy
        ) {
            TODO("Not yet implemented")
        }
    }

}

interface PermissionEntity {
    val identifier: String
    val platform: String

    fun hasPermission(permission: String, minLevel: Int): Boolean?
    fun getPermissionLevel(permission: String): Int?


    fun setPermission(permission: String, level: Int, identificationStrategy: PermissionIdentificationStrategy)
}

fun PermissionEntity.hasPermission(permission: String) = hasPermission(permission, 0)
fun PermissionEntity.setPermission(permission: String, level: Int) =
    setPermission(permission, level, PermissionIdentificationStrategy.EQUALS)

fun PermissionEntity.setPermission(permission: String) = setPermission(permission, 0)

enum class PermissionIdentificationStrategy {
    EQUALS, REGEX, STARTS_WITH
}


data class PersistentPermissionEntryData
@JvmOverloads constructor(
    val permission: String,
    val level: Int = 0,
    val identificationStrategy: PermissionIdentificationStrategy = PermissionIdentificationStrategy.EQUALS,
) : Serializable