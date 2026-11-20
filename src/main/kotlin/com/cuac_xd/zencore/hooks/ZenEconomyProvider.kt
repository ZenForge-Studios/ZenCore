package com.cuac_xd.zencore.hooks

import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Contrato tipado del proveedor de economía multidivisa para el ecosistema ZenCore.
 * ZenEconomy implementa esta interfaz y se registra en [ZenEconomyHook.registerProvider]
 * al habilitarse, evitando reflexión frágil y garantizando resultados reales de transacción.
 */
interface ZenEconomyProvider {

    fun currencyExists(currencyId: String): Boolean

    fun getRegisteredCurrencies(): List<String>

    fun getBalance(uuid: UUID, currencyId: String): Double

    fun has(uuid: UUID, currencyId: String, amount: Double): Boolean

    fun deposit(uuid: UUID, currencyId: String, amount: Double): Boolean

    fun withdraw(uuid: UUID, currencyId: String, amount: Double): Boolean

    fun setBalance(uuid: UUID, currencyId: String, amount: Double): Boolean

    fun depositAsync(uuid: UUID, currencyId: String, amount: Double): CompletableFuture<Boolean>

    fun withdrawAsync(uuid: UUID, currencyId: String, amount: Double): CompletableFuture<Boolean>

    fun setBalanceAsync(uuid: UUID, currencyId: String, amount: Double): CompletableFuture<Boolean>

    fun transferAsync(from: UUID, to: UUID, currencyId: String, amount: Double): CompletableFuture<Boolean>

    fun exchangeAsync(uuid: UUID, fromCurrencyId: String, toCurrencyId: String, amount: Double): CompletableFuture<Boolean>

    fun format(currencyId: String, amount: Double): String

    fun formatShort(currencyId: String, amount: Double): String
}
