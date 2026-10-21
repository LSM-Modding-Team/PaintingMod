Catálogo de pinturas vanilla para Minecraft Java 26.2

TOTAL: 375 bloques/apariencias únicos, de 402 candidatos cúbicos sin block entities.
Se agrupan 27 duplicados visuales: 20 variantes enceradas de cobre y 7 infestados.
La lista completa está en vanilla-paints-26.2.csv. El JSON conserva los modelos,
texturas, clases, criterios, fuentes y los duplicados que se agruparon.

Numeración PROPUESTA (todavía no usada por el mod):
0: UNPAINTED, sin pintar.
1: default_checker, cuadros grises.
2–376: los 375 bloques vanilla, por ID de bloque en orden alfabético.
377–391: las quince pinturas archivadas, manteniendo su orden original.
La tabla paint-id-conversion.csv contiene las correspondencias exactas.
Un futuro almacenamiento directo de estos IDs necesitará al menos 9 bits.
El guardado actual de cinco bits y su paleta no se modifican en esta tarea.

«Entero» significa contorno y apariencia de cubo de 16 × 16 × 16. Se incluyen
cristal, hojas, raíces de mangle y rejillas cúbicas transparentes. No se exige
colisión maciza: miel, barro, nieve en polvo y arena de almas son casos expresos.
Se excluyen cactus, formas parciales, pistones que dejan de ser cúbicos al
extenderse, losas incluso con estado doble, fluidos y bloques técnicos invisibles.
Una entrada representa un bloque, no cada orientación, estado o fotograma.
La futura integración deberá definir cómo conservar tintes, transparencias,
animaciones, materiales por cara y estados; este catálogo todavía no los registra.

Método: inspección estática del cliente oficial 26.2, sus modelos/blockstates,
la jerarquía de clases EntityBlock y los píxeles RGBA de las texturas. Las
excepciones de forma se contrastaron con las clases oficiales. Sin ejecutar
Minecraft, compilaciones, generadores de datos del juego ni pruebas.
No se han convertido mundos, NBT, patrones copiados ni archivos .schem.
No se han añadido estas pinturas al juego. DEFAULT sigue guardándose como ID 16.
