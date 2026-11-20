package com.cuac_xd.zencore.command

import com.cuac_xd.zencore.util.AdventureUtil
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.Locale

/**
 * Contexto de ejecución de un comando de ZenCore.
 */
data class ZenCommandContext(
    val sender: CommandSender,
    val player: Player?,
    val args: List<String>,
    val label: String
) {
    val isPlayer: Boolean get() = player != null

    fun reply(message: String) {
        sender.sendMessage(AdventureUtil.parse(message))
    }

    fun getArg(index: Int): String? = args.getOrNull(index)

    fun getInt(index: Int, default: Int = 0): Int = getArg(index)?.toIntOrNull() ?: default

    fun getDouble(index: Int, default: Double = 0.0): Double = getArg(index)?.toDoubleOrNull() ?: default

    fun getTargetPlayer(index: Int): Player? = getArg(index)?.let { Bukkit.getPlayerExact(it) }
}

/**
 * Representa un subcomando dentro de un [ZenCommand].
 */
open class ZenSubcommand(
    val name: String,
    val permission: String? = null,
    val playerOnly: Boolean = false,
    val description: String = "",
    val usage: String = "",
    val aliases: List<String> = emptyList()
) {
    private var executor: ((ZenCommandContext) -> Unit)? = null
    private var tabCompleter: ((ZenCommandContext) -> List<String>)? = null

    fun onExecute(handler: (ZenCommandContext) -> Unit): ZenSubcommand {
        this.executor = handler
        return this
    }

    fun onTabComplete(handler: (ZenCommandContext) -> List<String>): ZenSubcommand {
        this.tabCompleter = handler
        return this
    }

    open fun execute(context: ZenCommandContext) {
        executor?.invoke(context)
    }

    open fun tabComplete(context: ZenCommandContext): List<String> {
        return tabCompleter?.invoke(context) ?: emptyList()
    }
}

/**
 * Motor fluido de comandos y subcomandos con registro dinámico en el CommandMap de Bukkit.
 * Elimina la necesidad de registrar comandos en plugin.yml y gestiona permisos, validación de jugador
 * y autocompletado inteligente con prefijo automático.
 */
class ZenCommand(
    commandName: String,
    commandPermission: String? = null,
    val playerOnly: Boolean = false,
    commandDescription: String = "",
    commandUsage: String = "/$commandName",
    commandAliases: List<String> = emptyList()
) : Command(commandName, commandDescription, commandUsage, commandAliases) {

    init {
        permission = commandPermission
    }

    private val subcommands = mutableMapOf<String, ZenSubcommand>()
    private var defaultExecutor: ((ZenCommandContext) -> Unit)? = null
    private var defaultTabCompleter: ((ZenCommandContext) -> List<String>)? = null

    fun addSubcommand(sub: ZenSubcommand): ZenCommand {
        subcommands[sub.name.lowercase(Locale.ROOT)] = sub
        for (alias in sub.aliases) {
            subcommands[alias.lowercase(Locale.ROOT)] = sub
        }
        return this
    }

    fun sub(name: String, permission: String? = null, playerOnly: Boolean = false, block: ZenSubcommand.() -> Unit): ZenCommand {
        val sub = ZenSubcommand(name, permission, playerOnly)
        sub.block()
        return addSubcommand(sub)
    }

    fun onExecute(handler: (ZenCommandContext) -> Unit): ZenCommand {
        this.defaultExecutor = handler
        return this
    }

    fun onTabComplete(handler: (ZenCommandContext) -> List<String>): ZenCommand {
        this.defaultTabCompleter = handler
        return this
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        val player = sender as? Player
        if (playerOnly && player == null) {
            sender.sendMessage(AdventureUtil.parse("<red>Este comando solo puede ser ejecutado por jugadores en línea."))
            return true
        }

        val perm = permission
        if (perm != null && !sender.hasPermission(perm)) {
            sender.sendMessage(AdventureUtil.parse("<red>No tienes permiso para ejecutar este comando."))
            return true
        }

        val argList = args.toList()
        if (argList.isNotEmpty()) {
            val subName = argList[0].lowercase(Locale.ROOT)
            val sub = subcommands[subName]
            if (sub != null) {
                if (sub.playerOnly && player == null) {
                    sender.sendMessage(AdventureUtil.parse("<red>Este subcomando solo puede ser ejecutado por jugadores."))
                    return true
                }
                if (sub.permission != null && !sender.hasPermission(sub.permission)) {
                    sender.sendMessage(AdventureUtil.parse("<red>No tienes permiso para este subcomando."))
                    return true
                }
                val subContext = ZenCommandContext(sender, player, argList.drop(1), commandLabel)
                sub.execute(subContext)
                return true
            }
        }

        val context = ZenCommandContext(sender, player, argList, commandLabel)
        if (defaultExecutor != null) {
            defaultExecutor?.invoke(context)
        } else {
            // Mostrar ayuda automática de subcomandos
            sendHelp(sender, commandLabel)
        }
        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
        val player = sender as? Player
        if (args.isEmpty()) return emptyList()

        if (args.size == 1) {
            val current = args[0].lowercase(Locale.ROOT)
            val available = subcommands.values
                .distinctBy { it.name }
                .filter { sub -> sub.permission == null || sender.hasPermission(sub.permission) }
                .map { it.name }
                .filter { it.startsWith(current) }

            if (available.isNotEmpty()) return available
            val defTabs = defaultTabCompleter?.invoke(ZenCommandContext(sender, player, args.toList(), alias))
            if (defTabs != null) return defTabs
            return Bukkit.getOnlinePlayers().map { it.name }.filter { it.lowercase(Locale.ROOT).startsWith(current) }
        }

        val subName = args[0].lowercase(Locale.ROOT)
        val sub = subcommands[subName]
        if (sub != null) {
            val subArgs = args.drop(1)
            val context = ZenCommandContext(sender, player, subArgs, alias)
            return sub.tabComplete(context).filter { it.lowercase(Locale.ROOT).startsWith(subArgs.last().lowercase(Locale.ROOT)) }
        }

        return emptyList()
    }

    fun sendHelp(sender: CommandSender, label: String) {
        sender.sendMessage(AdventureUtil.parse("<gradient:#00e5ff:#1200ff><b>Subcomandos disponibles para /$label:</b></gradient>"))
        for (sub in subcommands.values.distinctBy { it.name }) {
            if (sub.permission == null || sender.hasPermission(sub.permission)) {
                val usageStr = if (sub.usage.isNotBlank()) " ${sub.usage}" else ""
                val descStr = if (sub.description.isNotBlank()) " <gray>- ${sub.description}" else ""
                sender.sendMessage(AdventureUtil.parse("<yellow>/$label ${sub.name}$usageStr$descStr"))
            }
        }
    }

    /**
     * Registra el comando directamente en el CommandMap del servidor en tiempo de ejecución.
     */
    fun register(fallbackPrefix: String = "zencore"): ZenCommand {
        runCatching {
            Bukkit.getCommandMap().register(fallbackPrefix, this)
        }.onFailure {
            runCatching {
                val commandMapField = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
                commandMapField.isAccessible = true
                val commandMap = commandMapField.get(Bukkit.getServer()) as CommandMap
                commandMap.register(fallbackPrefix, this)
            }
        }
        return this
    }
}
