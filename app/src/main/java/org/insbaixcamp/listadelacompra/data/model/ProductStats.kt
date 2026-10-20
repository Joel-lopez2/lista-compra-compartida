package org.insbaixcamp.listadelacompra.data.model

/** Resumen de los productos de una lista: cuantos hay y cuantos estan completados. */
data class ProductStats(
    val total: Int = 0,
    val done: Int = 0
)
