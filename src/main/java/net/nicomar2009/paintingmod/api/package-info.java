/**
 * Public painting integration, independent of LSMcore and LSMMod. Minecraft 26.2 / NeoForge 26.2.0.88 / Java 25.
 *
 * <h2>Registration and ownership</h2>
 * The consuming mod registers its own blocks. During common setup, on both logical sides, enqueue:
 * <pre>{@code
 * PaintProviders.register(Identifier.fromNamespaceAndPath("lsmmod", "fixed_door_paint"),
 *         provider, fixedCarrierBlock, controllerBlock, proxyBlock);
 * }</pre>
 * Register actual resolved Block objects, not classes. A provider can serve multiple block types; a block
 * has at most one provider. Register only carriers that participate in this workflow. No PaintingMod block
 * registration, window compatibility registration, LSMMod import or Core dependency is needed.
 * PaintingMod suppresses ordinary block-use for its four tools on registered carriers, so a door does not
 * consume the painting click to open. It honors canceled events and item-use denial by protection mods.
 * A consumer with its own earlier interaction-event/network hook must likewise route painting tools to the
 * item hook, rather than sending an independent door-use request for the same click.
 *
 * <h2>Adapting the Core door API</h2>
 * Reviewed delivery: LSM-Modding-Team/LSMcore commit 37cec9cda43cd67fbf0f54f813dfdcdfead394d7
 * (Add reusable multiblock door structures and lifecycle API).
 * The following adaptation belongs in the consumer, which already uses Core; no Core bridge is loaded here.
 * {@code DoorService.at(ServerLevel, pos)} supplies the server view; on clients consult
 * {@code ClientDoors.availability(pos)==READY} before using {@code ClientDoors.at(pos)}.
 * Resolve an ACTIVE record with verified UUID/slot links and all required chunks loaded. Unavailable data
 * is not an empty layer. Supply all governed carrier/proxy positions and the actual paint authority position.
 *
 * Only explicitly marked {@code DoorSurface}s with {@code leaf()==null} become painting patches. Core's
 * surface declaration alone does NOT grant paintability. Glass, leaves, border, handle and unmarked panels
 * stay excluded. Use stable IDs such as {@code fixed_panel/front}, with separate patches for different planes.
 * Split a Core box into the owner's selected planar faces; a box itself is not an 8px paint surface.
 * Each {@link net.nicomar2009.paintingmod.api.PaintSurface} has an explicit allowed-cell mask and owner-defined
 * original/default finish identifiers. Its width and height count 8px cells. Use half-block grid origins in
 * piece-local coordinates and preserve cell indices; {@link net.nicomar2009.paintingmod.api.PaintFrame#placed}
 * matches Core's mirror-X/quarter-turn convention. Fixed parts never use the leaf opening transform.
 * Keep materialFace in the original piece basis as well; it selects the same vanilla source face after rotation.
 * For example, a one-block-wide/tall fixed front panel has a 2x2 mask:
 * <pre>{@code
 * BitSet allowed = new BitSet(4);
 * allowed.set(0, 4); // clear any cell intersecting glass/trim/handle before declaring the surface
 * PaintFrame local = new PaintFrame(new Vec3(0, 0, 0), Direction.EAST, Direction.UP, Direction.NORTH);
 * PaintSurface panel = new PaintSurface("fixed_panel/front", compatibilityKey,
 *         local.placed(worldOrigin, quarterTurns, mirrored), Direction.NORTH, 2, 2, allowed,
 *         originalFinishKey, defaultFinishKey);
 * }</pre>
 *
 * In {@code provider.select}, run {@code DoorHit.pick(view, query.from(), query.to())} against ALL current
 * Core surfaces, including excluded ones. Reject ambiguous hits, null surface IDs, moving leaves and unmarked
 * surfaces. Convert the selected fixed hit to the matching patch using {@code patch.cellAt(hit.location())};
 * check its side/plane and reject multiple matching patches. Never ray-test only paintable patches through
 * glazing or an open leaf. PaintingMod additionally verifies that the returned cell intersects the current ray.
 *
 * <h2>One paint authority and the controller</h2>
 * Core's {@code DoorRecord} is the sole door/leaf authority and has no paint field or PAINT DoorAction.
 * Do not repurpose MANUAL/REMOVE or copy Core leaf state into a controller to store paint.
 * Choose ONE consumer paint authority: an existing persistent controller, OR a consumer dimension store
 * keyed by structure UUID. Carrier/proxy entities contain links and derived render caches only.
 * {@link net.nicomar2009.paintingmod.api.PaintLayer} is an optional immutable value for that authority:
 * <pre>{@code
 * PaintLayer layer = PaintLayer.fresh(doorUUID); // initial ORIGINAL finish
 * PaintTarget target = layer.target(Long.toString(coreRecord.revision()), controllerPos, members, surfaces);
 * CompoundTag persisted = layer.save(surfaces);
 * PaintLayer restored = PaintLayer.load(persisted, doorUUID, surfaces);
 * }</pre>
 * Load the existing value, never initialize a new one on every resolve, load or packet. Absence of a serialized
 * key during NEW placement can initialize a layer; malformed/unavailable storage must block rather than reset.
 * If placement requires DEFAULT paint, explicitly fill allowed cells with {@code Paint.DEFAULT} once on creation.
 * Missing entries represent ORIGINAL; ID 1 represents DEFAULT; active vanilla paint IDs represent APPLIED.
 * Existing PaintingMod blocks keep their legacy storage and appearance behavior unchanged.
 *
 * In {@code authorize}, enforce consumer paint policy for every supplied cell and all relevant members.
 * There is no paint permission in Core's DoorAction enum; use the consumer's protection integration.
 * In {@code commit}, on the server thread, resolve current state again; compare structure UUID, layer revision,
 * Core record revision ({@code expected.geometryKey()}), current masks, membership, active phase and permission.
 * Prepare {@code next = currentLayer.next(expected, replacement)}. Atomically persist/install next at the ONE
 * authority. Return false with no changes if stale/denied; return true only after successful commit. Notify
 * watchers and invalidate render snapshots afterward. Never replace/remove/move blocks, toggle leaves or
 * invoke Core placement/recovery during painting, sanding or cleaning. Removal of the structure should clean
 * up its paint authority through the consumer lifecycle; unloading alone must preserve it.
 *
 * <h2>Persistence, synchronization and rendering</h2>
 * Reuse {@code PaintLayer.save/load} in the owner's initial chunk-watch and update synchronization, or
 * controller save/load and update tag/packet. Include owner identity/revision; reject wrong dimension,
 * stale or removed owners and out-of-order revisions. Render caches are immutable derived snapshots, not
 * independent storage. PaintingMod intentionally does not register another door-global store or transport.
 * The codec is versioned, bounded and rejects excluded/unknown cells; geometry migrations are explicit owner work.
 *
 * Capture {@code PaintRenderData.capture(target)} on the client thread after a valid synchronized update.
 * Publish it under {@code PaintRenderData.PROPERTY} in the owner's ModelData, or its renderer snapshot cache.
 * For each cell the renderer receives world corners, normal, local UVs and a {@code PaintAppearance}:
 * ORIGINAL uses the owner's originalFinish; DEFAULT uses its defaultFinish; APPLIED uses its vanilla block
 * material identifier. Resolve original/default keys using consumer textures/models (keys are not necessarily
 * block IDs). For APPLIED use the vanilla block model/material, including its tint/render layer, with UVs
 * anchored to the original piece basis and appearance.materialFace() for the source face.
 * Do not use Paint.UNPAINTED.blockId() as an external original finish.
 * Replace paintable face quads or clip them to the cell; avoid coplanar overlays/z-fighting. Render glass,
 * leaves and other excluded geometry normally. There is no automatic model wrapping for external blocks.
 *
 * <h2>Tool scope and bounds</h2>
 * Paint targets a selected allowed cell. Ctrl-use copies one entire authorized surface; paste requires the
 * same compatibility key, dimensions and exact mask, preserving local UVs across rotations/reflections.
 * Basic six-face/occupancy clipboards and external surface clipboards cannot be pasted into each other.
 * External clipboards currently use the ordinary paint-item appearance, not a miniature consumer-door preview.
 * Shift-use rectangles span aligned coplanar patches in ONE target with the same compatibility key/basis;
 * holes are skipped, overlapping patches rejected, and all cells authorized before one commit. A geometry
 * or paint revision change invalidates the pending rectangle. Shift-use in air cancels it. Rectangles do not
 * cross independent structures or mix external and basic-block formats. Maximum rectangle: 4096 cells;
 * target: 256 patches / 16384 cells / 4096 permission positions; patch: at most 64x64 cells.
 * Lija restores ORIGINAL and refunds each changed refundable cell only after a successful server commit;
 * DEFAULT/ORIGINAL never refund. Cleaner clears all allowed cells of the resolved target without refunds.
 * The hammer rejects registered external blocks and has no carving callback.
 *
 * <h2>Direct consumer calls</h2>
 * {@code PaintOperations.pick(query)} returns the selected provider snapshot; {@code target.data().paint(cell)}
 * reads it. {@code PaintOperations.change(query, selection, action, changes)} performs the same server permission
 * and commit path used by tools. {@code SurfacePaintPattern.capture/changes} supplies surface copying; authorize
 * READ over {@code SurfacePaintPattern.cells(surface)} before capture. Pure PaintData transformations and
 * clipboard construction do not themselves authorize edits or persist data.
 */
package net.nicomar2009.paintingmod.api;
