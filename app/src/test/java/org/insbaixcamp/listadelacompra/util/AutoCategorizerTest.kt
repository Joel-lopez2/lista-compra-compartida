package org.insbaixcamp.listadelacompra.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutoCategorizerTest {

    private fun category(name: String) = AutoCategorizer.suggest(name)?.name

    @Test
    fun leche_va_a_lacteos() = assertEquals("Lácteos", category("leche"))

    @Test
    fun llet_en_catalan_va_a_lacteos() = assertEquals("Lácteos", category("llet"))

    @Test
    fun ignora_mayusculas_y_tildes() {
        assertEquals("Frutas y verduras", category("PLÁTANO"))
        assertEquals("Lácteos", category("  Yogur Natural "))
    }

    @Test
    fun entiende_plurales_sencillos() {
        assertEquals("Frutas y verduras", category("manzanas"))
        assertEquals("Despensa", category("huevos"))
        assertEquals("Frutas y verduras", category("tomates"))
    }

    @Test
    fun frase_con_varias_palabras() {
        assertEquals("Carnes y pescados", category("pechuga de pollo"))
        assertEquals("Lácteos", category("queso rallado"))
    }

    @Test
    fun papel_higienico_va_a_higiene_y_no_a_otra_categoria() {
        assertEquals("Higiene", category("papel higiénico"))
        assertEquals("Higiene", category("pasta de dientes"))
    }

    @Test
    fun papel_de_cocina_va_a_limpieza() = assertEquals("Limpieza", category("papel de cocina"))

    @Test
    fun cada_categoria_principal() {
        assertEquals("Panadería", category("pan"))
        assertEquals("Bebidas", category("agua"))
        assertEquals("Limpieza", category("lejía"))
        assertEquals("Despensa", category("arroz"))
        assertEquals("Carnes y pescados", category("salmón"))
    }

    @Test
    fun producto_desconocido_devuelve_null() {
        assertNull(AutoCategorizer.suggest("xyzzy"))
        assertNull(AutoCategorizer.suggest(""))
        assertNull(AutoCategorizer.suggest("   "))
    }

    @Test
    fun la_categoria_trae_icono() {
        assertEquals("🥛", AutoCategorizer.suggest("leche")?.icon)
    }

    @Test
    fun normalize_quita_tildes_y_espacios() {
        assertEquals("platano maduro", AutoCategorizer.normalize("  Plátano   Maduro "))
    }
}
