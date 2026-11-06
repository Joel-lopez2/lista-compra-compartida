package org.insbaixcamp.listadelacompra.util

import java.text.Normalizer

/** Categoria sugerida para un producto: su nombre y el emoji que se le pone si hay que crearla. */
data class SuggestedCategory(val name: String, val icon: String)

/**
 * Extra: clasificacion automatica de productos.
 *
 * Dado el nombre de un producto ("leche", "Yogur natural"...) devuelve la categoria a la
 * que pertenece ("Lácteos") usando un diccionario de palabras clave en castellano y catalan.
 * Si no reconoce el producto devuelve null y el producto se queda "Sin categoria".
 */
object AutoCategorizer {

    private class Rule(val category: SuggestedCategory, words: List<String>, phrases: List<String> = emptyList()) {
        val words: Set<String> = words.map { normalize(it) }.toSet()
        val phrases: List<String> = phrases.map { normalize(it) }
    }

    // El orden importa: las reglas con frases ("papel higienico") van antes que las generales.
    private val rules = listOf(
        Rule(
            SuggestedCategory("Higiene", "🧴"),
            words = listOf(
                "champu", "xampu", "gel", "jabon", "sabo", "desodorante", "desodorant", "colonia",
                "compresa", "tampon", "pañal", "panyal", "toallita", "cepillo", "raspall",
                "dentifrico", "maquinilla", "algodon", "cotonet", "bastoncillo"
            ),
            phrases = listOf("papel higienico", "paper higienic", "pasta de dientes", "pasta de dents", "crema hidratante", "crema solar")
        ),
        Rule(
            SuggestedCategory("Limpieza", "🧽"),
            words = listOf(
                "lejia", "lleixiu", "fregasuelos", "limpiacristales", "detergente", "detergent",
                "suavizante", "suavitzant", "friegaplatos", "lavavajillas", "bayeta", "baieta",
                "estropajo", "fregall", "ambientador", "desinfectante", "desengrasante",
                "escoba", "fregona", "esponja"
            ),
            phrases = listOf("papel de cocina", "paper de cuina", "bolsas de basura", "bosses d escombraries", "papel aluminio")
        ),
        Rule(
            SuggestedCategory("Lácteos", "🥛"),
            words = listOf(
                "leche", "llet", "yogur", "iogurt", "queso", "formatge", "mantequilla", "mantega",
                "nata", "kefir", "requeson", "mozzarella", "parmesano", "cuajada",
                "natillas", "flan", "batido", "lacteo", "mató", "mato"
            )
        ),
        Rule(
            SuggestedCategory("Frutas y verduras", "🥦"),
            words = listOf(
                "manzana", "poma", "platano", "banana", "naranja", "taronja", "limon", "llimona",
                "pera", "uva", "raim", "fresa", "maduixa", "melon", "sandia", "meloco", "melocoton",
                "kiwi", "pina", "mango", "aguacate", "alvocat", "cereza", "cirera", "ciruela",
                "tomate", "tomaquet", "lechuga", "enciam", "cebolla", "ceba", "ajo", "all",
                "patata", "creilla", "zanahoria", "pastanaga", "pimiento", "pebrot", "calabacin",
                "carabassa", "berenjena", "albergínia", "alberginia", "pepino", "cogombre",
                "brocoli", "espinaca", "espinacs", "coliflor", "champinon", "xampinyo", "seta",
                "judia", "mongeta", "puerro", "porro", "apio", "api", "fruta", "fruita",
                "verdura", "hortalizas", "ensalada", "rucula", "calabaza", "maiz", "guisantes", "pesols"
            )
        ),
        Rule(
            SuggestedCategory("Carnes y pescados", "🥩"),
            words = listOf(
                "pollo", "pollastre", "ternera", "vedella", "cerdo", "porc", "cordero",
                "carne", "pavo", "gall", "jamon", "pernil", "chorizo", "xoriço", "salchicha",
                "botifarra", "bacon", "beicon", "lomo", "filete", "hamburguesa", "salchichon",
                "fuet", "longaniza", "pescado", "peix", "salmon", "salmo", "merluza", "lluç", "atun",
                "tonyina", "bacalao", "bacallà", "gamba", "gambas", "mejillon", "musclo", "calamar",
                "sepia", "sardina", "anchoa", "anxova", "lubina", "dorada", "pulpo", "pop", "marisco"
            )
        ),
        Rule(
            SuggestedCategory("Panadería", "🥖"),
            words = listOf(
                "pan", "pa", "barra", "baguette", "croissant", "cruasan", "bollo", "brioche",
                "magdalena", "donut", "galleta", "galeta", "tostada", "biscote", "panecillo",
                "ensaimada", "tarta", "pastel", "pastis", "bizcocho", "cereales", "napolitana", "empanada"
            )
        ),
        Rule(
            SuggestedCategory("Bebidas", "🥤"),
            words = listOf(
                "agua", "aigua", "refresco", "refresc", "cola", "coca", "fanta", "zumo", "suc",
                "cerveza", "cervesa", "vino", "vi", "cava", "cafe", "te", "infusion", "infusio",
                "tonica", "gaseosa", "sidra", "licor", "whisky", "ron", "ginebra", "vodka",
                "isotonica", "energetica", "bebida", "beguda", "sangria", "horchata"
            )
        ),
        Rule(
            SuggestedCategory("Despensa", "🍝"),
            words = listOf(
                "arroz", "arros", "pasta", "macarron", "espagueti", "fideos", "fideus", "harina",
                "farina", "azucar", "sucre", "sal", "aceite", "oli", "vinagre", "lenteja", "llentia",
                "garbanzo", "cigro", "alubia", "conserva", "atun", "tomate frito", "salsa", "mayonesa",
                "ketchup", "mostaza", "mostassa", "huevo", "ou", "ous", "huevos", "miel", "mel",
                "chocolate", "xocolata", "cacao", "mermelada", "confitura", "frutos secos", "nuez",
                "avellana", "almendra", "ametlla", "cacahuete", "pipas", "patatas fritas", "snack",
                "especia", "pimienta", "oregano", "comino", "caldo", "sopa", "cuscus", "quinoa", "avena", "garbanzos"
            )
        )
    )

    /** Devuelve la categoria sugerida para el producto, o null si no lo reconoce. */
    fun suggest(productName: String): SuggestedCategory? {
        val text = normalize(productName)
        if (text.isBlank()) return null

        val tokens = text.split(" ").filter { it.isNotBlank() }
        for (rule in rules) {
            if (rule.phrases.any { text.contains(it) }) return rule.category
        }
        for (rule in rules) {
            if (tokens.any { token -> matches(rule, token) }) return rule.category
        }
        return null
    }

    /** Compara una palabra con las claves de la regla, tolerando plurales sencillos. */
    private fun matches(rule: Rule, token: String): Boolean {
        if (token in rule.words) return true
        val candidates = buildList {
            if (token.endsWith("es") && token.length > 4) add(token.dropLast(2))
            if (token.endsWith("s") && token.length > 3) add(token.dropLast(1))
        }
        return candidates.any { it in rule.words }
    }

    /** Minusculas, sin tildes y con espacios simples ("Plátano  Maduro" -> "platano maduro"). */
    fun normalize(text: String): String {
        val decomposed = Normalizer.normalize(text.lowercase().trim(), Normalizer.Form.NFD)
        return decomposed
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("[^a-zñç0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
