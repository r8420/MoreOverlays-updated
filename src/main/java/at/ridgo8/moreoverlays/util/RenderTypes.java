package at.ridgo8.moreoverlays.util;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class RenderTypes {

    private RenderTypes() {}

    // Standard depth state: occlude by solid geometry and write to the depth buffer.
    // 26.2 switched the depth buffer to a reverse-Z convention, so the comparison that
    // previously used LESS_THAN_OR_EQUAL must now use GREATER_THAN_OR_EQUAL to keep the
    // overlays hidden behind solid blocks.
    private static final DepthStencilState OVERLAY_DEPTH_STATE =
        new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true, 0f, 0f);

    public static final RenderPipeline LINE_PIPELINE =
        RenderPipeline.builder(new RenderPipeline.Snippet[0])
            .withLocation("pipeline/debug_lines")
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            .withCull(false)
            .withDepthStencilState(OVERLAY_DEPTH_STATE)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.DEBUG_LINES)
            .build();

    private static final RenderSetup LINE_SETUP =
        RenderSetup.builder(LINE_PIPELINE)
            .createRenderSetup();

    public static final RenderPipeline TRIANGLE_PIPELINE =
        RenderPipeline.builder(new RenderPipeline.Snippet[0])
            .withLocation("position_color")
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            .withCull(false)
            .withDepthStencilState(OVERLAY_DEPTH_STATE)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .build();

    private static final RenderSetup TRIANGLE_SETUP =
        RenderSetup.builder(TRIANGLE_PIPELINE)
            .createRenderSetup();

    public static final RenderType LIGHT_OVERLAY_LINES =
        RenderType.create("light_overlay_lines", LINE_SETUP);
    public static final RenderType LIGHT_OVERLAY_TRIANGLES =
        RenderType.create("light_overlay_triangles", TRIANGLE_SETUP);

    public static final RenderType POSITION_COLOR_OVERLAY = LIGHT_OVERLAY_LINES;
}
