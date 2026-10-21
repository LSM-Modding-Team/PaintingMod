Catálogo de pinturas vanilla para Minecraft Java 26.2

TOTAL: 337 bloques/apariencias únicos con superficies completas y opacas.
De los 402 candidatos cúbicos sin block entities, se excluyen 42 por
cristal o superficies incompletas/translúcidas y se agrupan 23 duplicados visuales.
La lista completa está en vanilla-paints-26.2.csv. El JSON conserva los modelos,
texturas, clases, criterios, fuentes y las exclusiones, con sus valores alfa.

Numeración PROPUESTA (todavía no usada por el mod):
0: UNPAINTED, sin pintar.
1: default_checker, cuadros grises.
2–338: los 337 bloques vanilla, por ID de bloque en orden alfabético.
339–353: las quince pinturas archivadas, manteniendo su orden original.
La tabla paint-id-conversion.csv contiene las correspondencias exactas.
Un futuro almacenamiento directo de estos IDs necesitará al menos 9 bits.
El guardado actual de cinco bits y su paleta no se modifican en esta tarea.

«Entero» exige contorno cúbico de 16 × 16 × 16 y una superficie completa opaca.
Se excluyen todos los cristales, las hojas, las rejillas, las raíces de mangle,
el hielo transparente, el hielo escarchado y los bloques de miel y slime.
El hielo compacto y el hielo azul permanecen: sus texturas son completas y opacas.
El césped permanece: su overlay se compone sobre una cara base completa y opaca.
No se exige colisión maciza: barro, nieve en polvo y arena de almas son casos expresos.
También se excluyen cactus, formas parciales, pistones que dejan de ser cúbicos al
extenderse, losas incluso con estado doble, fluidos y bloques técnicos invisibles.
Una entrada representa un bloque, no cada orientación, estado o fotograma.

Comportamiento de las futuras pinturas:
- Ninguna emitirá luz, aunque el bloque vanilla original sea luminoso.
- En los bloques con varias texturas, su elección será relativa a la posición
  de los bloques pintados y a sus relaciones espaciales.
- No se heredará la lógica funcional del bloque vanilla de origen.
Estas reglas quedan definidas para la futura integración; todavía no se registran
las pinturas vanilla ni se implementa su renderizado en el juego.

Método: inspección estática del cliente oficial 26.2, sus modelos/blockstates,
la jerarquía de clases EntityBlock y los píxeles RGBA de todas las texturas y
fotogramas. Los overlays se consideran junto con su base opaca. Sin ejecutar
Minecraft, compilaciones, generadores de datos del juego ni pruebas.
No se han convertido mundos, NBT, patrones copiados ni archivos .schem.
DEFAULT sigue guardándose como ID 16 en la paleta activa.
