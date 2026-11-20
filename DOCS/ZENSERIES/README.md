# 🌟 ZenSeries: Documentación del Ecosistema ZenCore

Bienvenido a la documentación oficial de **ZenCore** para los plugins de la serie **Zen**. ZenCore conecta e integra un ecosistema de **29 plugins hermanos** (`ZenEconomy`, `ZenCosmetics`, `ZenCustomItems`, `ZenEnchants`, `ZenPartys`, `ZenBoosters`, `ZenTags`, `ZenDiscord`, `ZenAuctions`, `ZenJobs`, `ZenHorses`, `ZenStorage`, `ZenBans`, `ZenBank`, `ZenAfkZone`, `ZenChat`, `ZenChatGames`, `ZenDragonEvent`, `ZenFairy`, `ZenForges`, `ZenHub`, `ZenQuests`, `ZenRankups`, `ZenRewards`, `ZenRogueLike`, `ZenSpawner`, `ZenBlockRegen`, `ZenContest`, `ZenCrates`).

**ZenCore** es el marco de trabajo y biblioteca base compartida (al estilo *Eco / Libreforge* de Auxilor). Su objetivo es proporcionar una infraestructura técnica de alto rendimiento en Paper/Kotlin para que ningún plugin tenga que reinventar la rueda con código repetitivo.

---

## 📚 Índice de Módulos del Núcleo

| Módulo | Archivo de Documentación | Descripción |
| :--- | :--- | :--- |
| 🎮 **Comandos Dinámicos** | [`01_comandos.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/01_comandos.md) | Creación fluida de comandos y subcomandos con registro en tiempo de ejecución y autocompletado inteligente. |
| 💬 **Mensajes y Localización** | [`02_mensajes.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/02_mensajes.md) | Soporte de MiniMessage, ActionBars, Titles, Sonidos y Placeholders PAPI contextuales. |
| 🗄️ **Base de Datos y Persistencia** | [`03_base_de_datos.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/03_base_de_datos.md) | Soporte asíncrono (`CompletableFuture`) para SQLite local o MySQL remoto con migraciones automáticas. |
| 🔮 **Hologramas Modernos** | [`04_hologramas.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/04_hologramas.md) | Hologramas de alto rendimiento usando entidades nativas `TextDisplay` de Minecraft 1.20+. |
| 📜 **Recetas de Crafteo** | [`05_recetas.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/05_recetas.md) | Registro de recetas Shaped y Shapeless con ítems personalizados y verificación en vivo. |
| ⏳ **Cooldowns y Timers** | [`06_cooldowns.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/06_cooldowns.md) | Cooldowns en memoria o persistentes (PDC de jugador) con formateo legible (`2h 15m`). |
| ⚙️ **Configuración Auto-Update** | [`07_configuracion.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/07_configuracion.md) | Gestión de archivos YAML que inyecta automáticamente nuevas claves sin sobreescribir las existentes. |
| 🎁 **Drop Engine Seguro** | [`08_drops_seguros.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/08_drops_seguros.md) | Entrega garantizada de recompensas evitando pérdidas si el inventario está lleno (suelo o buzón virtual). |
| 🗡️ **Ítems y Menús Interactivos** | [`09_items_y_menus.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/09_items_y_menus.md) | `ItemBuilder`, `ItemParser`, `ZenMenu`, máscaras visuales y paginación. |
| 🌐 **Compatibilidad de Plugins** | [`10_compatibilidad_ecosistema.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/10_compatibilidad_ecosistema.md) | Capa de ganchos desacoplada para interactuar con toda la suite de plugins Zen. |
| 👥 **Partys y Juego Grupal** | [`11_partys.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/11_partys.md) | `ZenPartyHub`, `ZenPartyHook`, `ZenPartyListener`, efectos, condiciones, filtros, mutadores y placeholders de party. |
| 🚀 **Boosters y Multiplicadores** | [`12_boosters_y_multiplicadores.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/12_boosters_y_multiplicadores.md) | `ZenBoosters`, `ZenBoosterEngine`, `ZenBoosterProvider`, `ZenBoosterHook`, `ZenBoosterListener`, vouchers, GUIs y boosters de team/party. |
| 📊 **Stats y Recursos (Maná)** | [`13_stats_y_recursos.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/13_stats_y_recursos.md) | `ZenStatEngine`, operaciones `ADDITIVE`/`PERCENTAGE`/`MULTIPLICATIVE`, atributos Paper y `mana_cost`. |
| ⛏️ **Mundo, Bloques y Triggers** | [`14_mundo_y_bloques.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/14_mundo_y_bloques.md) | `ZenBlockTracker`, `ZenWorldActions` (`mineVein`, `fellTree`, `replantCrop`, `autoSmelt`) y nuevos triggers. |
