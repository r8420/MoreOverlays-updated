package at.ridgo8.moreoverlays.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immediate-mode replacement for the removed {@code MultiBufferSource.BufferSource}.
 *
 * <p>26.2 dropped {@code MultiBufferSource} together with all direct vertex uploads outside of
 * vanilla chunk rendering. Geometry now has to be staged into a {@link StagedVertexBuffer},
 * uploaded to the GPU, and then drawn through {@link RenderType#prepare()} /
 * {@code PreparedRenderType#drawFromBuffer}. This helper keeps the old usage pattern
 * ({@code getBuffer(type)} to write, then a single {@code flush()} to render everything) so the
 * overlay renderers only need minimal changes.</p>
 *
 * <p>All methods must be called from the render thread inside an active render pass (the custom
 * frame-graph pass set up in the level renderer mixin).</p>
 */
public final class OverlayBufferSource {

    private static StagedVertexBuffer buffer;
    private static final Map<RenderType, StagedVertexBuffer.Draw> draws = new LinkedHashMap<>();

    private OverlayBufferSource() {}

    /** Returns a {@link VertexConsumer} that accumulates geometry for the given render type. */
    public static VertexConsumer getBuffer(RenderType type) {
        if (buffer == null) {
            buffer = new StagedVertexBuffer(() -> "moreoverlays_overlay", 4096);
        }
        StagedVertexBuffer.Draw draw = draws.computeIfAbsent(type,
            t -> buffer.appendDraw(t.format(), t.primitiveTopology()));
        return buffer.getVertexBuilder(draw);
    }

    /** Uploads and draws everything accumulated since the last flush, then resets for reuse. */
    public static void flush() {
        if (buffer == null) {
            return;
        }
        if (draws.isEmpty()) {
            buffer.endFrame();
            return;
        }

        buffer.upload();
        for (Map.Entry<RenderType, StagedVertexBuffer.Draw> entry : draws.entrySet()) {
            StagedVertexBuffer.ExecuteInfo info = buffer.getExecuteInfo(entry.getValue());
            if (info != null) {
                entry.getKey().prepare().drawFromBuffer(info);
            }
        }
        buffer.endFrame();
        draws.clear();
    }
}
