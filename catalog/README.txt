Catálogo activo de pinturas vanilla para Minecraft Java 26.2

337 pinturas vanilla, sin cristales ni superficies incompletas/translúcidas.
La lista completa está en vanilla-paints-26.2.csv. El JSON conserva modelos,
texturas, clases, fuentes, criterios y exclusiones con sus valores alfa.

Numeración activa de la paleta versión 2:
0: UNPAINTED, sin pintar.
1: default_checker, cuadros grises, acabado gratuito de los bloques nuevos.
2–338: las 337 pinturas vanilla, por ID de bloque en orden alfabético.
339–353: IDs reservados para las quince pinturas archivadas, todavía inactivas.
La tabla paint-id-conversion.csv mantiene sus correspondencias originales.
El catálogo completo necesita nueve bits por ID; cada palabra guarda siete IDs.

La paleta se identifica al guardar bloques y patrones copiados. Los lectores
reconocen los campos antiguos de tres, cuatro y cinco bits. En datos sin versión,
0 sigue siendo sin pintar, 1–15 identifican las pinturas archivadas y 16 el checker.
Al leer se conserva su significado, sin confundirlos con las pinturas vanilla.
No se han abierto, recorrido ni convertido mundos o archivos .schem en esta tarea.

Cada pintura es reutilizable y tiene un ítem en la pestaña creativa del mod.
Aplicar, copiar/pegar, lijar y seleccionar rectángulos conservan sus controles.
Ctrl + clic derecho guarda las seis caras completas y sus subdivisiones, incluso
si están sin pintar. Mientras haya un patrón, el ítem muestra el bloque copiado
con sus pinturas y forma tallada. El siguiente clic derecho pega sus pinturas una
vez y restaura el icono normal. El pegado conserva la geometría del destino.
Los patrones anteriores siguen siendo legibles y un patrón inválido no se
sustituye silenciosamente por la pintura base del ítem.
La lija devuelve los ítems de pintura vanilla tratados; el checker gratuito no
produce objetos. El limpiador no devuelve pinturas ni reconstruye cubos tallados.

Renderizado:
- Las pinturas no emiten luz ni ejecutan lógica del bloque vanilla original.
- Cada cara conserva las texturas y UV del modelo vanilla, recortados a 16/8/4px.
- Los ejes de troncos y pilares siguen los vecinos pintados o vanilla coincidentes.
- El césped se ve nevado bajo nieve vanilla o pintada.
- Los demás estados parten del estado predeterminado de la apariencia vanilla;
  las variantes aleatorias se eligen de forma estable según la posición mundial.
- Se mantienen tintes por bioma, animaciones y overlays opacos completos.
- Geometría, selección y colisión dependen exclusivamente de los cubos presentes.
- El modelo recibe snapshots inmutables de pintura y ocupación, sincronizados
  después de editar o cargar datos, y actualiza las apariencias vecinas.

«Entero» exige contorno cúbico de 16 × 16 × 16 y una superficie completa opaca.
De los 402 candidatos sin block entities, 42 se excluyen por transparencia y
23 duplicados visuales se agrupan, dejando las 337 apariencias activas.
Se excluyen cristal, hojas, rejillas, raíces de mangle, hielo transparente,
hielo escarchado, miel y slime. Hielo compacto y azul permanecen por ser opacos.
El overlay del césped se compone sobre una cara base completa y opaca.
No se exige colisión maciza: barro, nieve en polvo y arena de almas son casos expresos.
Se excluyen también formas parciales, pistones extensibles, losas, fluidos,
bloques técnicos invisibles y bloques con block entities.

Recursos y APIs revisados estáticamente contra Minecraft 26.2 y NeoForge 26.2.0.88.
No se han ejecutado compilaciones, pruebas, generadores de datos de Minecraft
ni el juego. El generador de recursos del repositorio solo escribe JSON e ítems.

Ctrl + middle click (creative pick block with data) copies a paintable block's full face/subdivision palette and occupancy into its item. The item previews the painted, carved shape and restores it when placed, including cleaned surfaces. Ordinary middle click still selects the default block. Unlike the one-shot paint clipboard, placing the copied block does not consume its stored template in creative mode.

Placement: the template's local bottom attaches to the clicked face, including walls and ceilings. The six paint_up block states store the support orientation; existing states default to up. A copied template is rotated from its saved support to the new support, moving all palette squares, occupancy and texture UVs together. Hammer targeting, rectangles, selection and collisions continue using world coordinates. Paint-only paste rotates to the destination support without changing its geometry.

Paint material variants are independent of neighboring blocks and paints. Logs and other pillars keep their native default axis in the local placement frame; touching another painted block cannot change an end-grain face into bark. Grass likewise does not acquire a snowy variant from adjacent snow. Face selection, stored support rotation, texture clipping and biome tint remain in the shared block/item renderer.
