# Categoría: Estado_del_item

Esta sección recopila todos los efectos pertenecientes a **estado_del_item** registrados en ZenCore.

---

## `add_durability`

Aumenta o restaura la durabilidad del ítem en mano o afectado.

## Ejemplo de Configuración

```yaml
effects:
  - type: ADD_DURABILITY
    amount: 10
```

---

## `damage_item`

Reduce la durabilidad del ítem equipado o utilizado.

## Ejemplo de Configuración

```yaml
effects:
  - type: DAMAGE_ITEM
    amount: 2
```

---

## `rename_item`

Renombra el ítem equipado con formato MiniMessage.

## Ejemplo de Configuración

```yaml
effects:
  - type: RENAME_ITEM
    name: '<gradient:#ff0000:#ffff00>Espada Carmesí</gradient>'
```

---

## `repair_armor`

Repara una cantidad fija o porcentual de todas las piezas de armadura equipadas.

## Ejemplo de Configuración

```yaml
effects:
  - type: REPAIR_ARMOR
    amount: 25
```

---

## `repair_item`

Repara el ítem sostenido en la mano principal.

## Ejemplo de Configuración

```yaml
effects:
  - type: REPAIR_ITEM
    amount: 50
```

---

## `set_custom_model_data`

Establece el CustomModelData del ítem para texturas personalizadas.

## Ejemplo de Configuración

```yaml
effects:
  - type: SET_CUSTOM_MODEL_DATA
    data: 10001
```

---

## `set_unbreakable`

Convierte el ítem en irrompible o restaura su estado rompible.

## Ejemplo de Configuración

```yaml
effects:
  - type: SET_UNBREAKABLE
    unbreakable: true
```

---

## `soulbound`

Evita que el ítem caiga al morir manteniéndolo en el inventario.

## Ejemplo de Configuración

```yaml
effects:
  - type: SOULBOUND
    keep_on_death: true
```

---

