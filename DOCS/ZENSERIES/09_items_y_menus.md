# 🗡️ 09. Ítems y Menús Interactivos

Referencia completa para `ItemBuilder`, `ItemParser`, `ZenMenu`, `MenuSlot`, `MenuMask` y `MenuParser`.

Para la guía detallada de este módulo, consulta:
👉 **[`DOCS/items_and_menus.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/items_and_menus.md)**

### Resumen Rápido
- **Creación de Ítems:** `ZenCoreAPI.createItem(material)` o `ZenCoreAPI.editItem(item)` con MiniMessage, texturas de cabeza en Base64, colores HEX y PDC.
- **Parser de Ítems:** `ZenCoreAPI.itemParser.parseSection(section)` para convertir secciones YAML directamente a `ItemBuilder`.
- **Menús Declarativos:** `ZenCoreAPI.createMenu(rows, title)` con máscaras (`MenuMask`), ranuras dinámicas (`MenuSlot`) y soporte nativo de paginación (`setPagedArea`, `setPreviousPageButton`, `setNextPageButton`).
- **Parser de Menús:** `ZenCoreAPI.menuParser.parse(section)` para construir interfaces gráficas completas a partir de archivos de configuración YAML.
