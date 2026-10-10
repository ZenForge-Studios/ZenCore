# CategorÃ­a: Inventario

Esta secciÃ³n documenta los efectos correspondientes a **Inventario** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `consume_item`

- **Identificador / Alias:** `CONSUME_ITEM`
- **DescripciÃ³n:** Reduce la cantidad del ítem en la mano.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: CONSUME_ITEM
    amount: 1
```

---

## `drop_item`

- **Identificador / Alias:** `DROP_ITEM`
- **DescripciÃ³n:** Suelta un ítem al suelo de forma natural.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: DROP_ITEM
    item: DIAMOND
    amount: 1
```

---

## `give_item`

- **Identificador / Alias:** `GIVE_ITEM`
- **DescripciÃ³n:** Entrega un ítem al inventario del jugador.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: GIVE_ITEM
    item: GOLD_INGOT
    amount: 5
```

---

## `mutate_drops`

- **Identificador / Alias:** `MUTATE_DROPS`
- **DescripciÃ³n:** Multiplica o altera los ítems generados al romper un bloque.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
  - type: MUTATE_DROPS
```

---

## `remove_item`

- **Identificador / Alias:** `REMOVE_ITEM`
- **DescripciÃ³n:** Retira un ítem del inventario del jugador.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: REMOVE_ITEM
    item: COAL
    amount: 1
```

---

