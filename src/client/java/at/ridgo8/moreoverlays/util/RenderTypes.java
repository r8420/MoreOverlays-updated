package at.ridgo8.moreoverlays.util;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class RenderTypes {

    private RenderTypes() {}

    // Standard depth state: occlude by solid geometry (LEQUAL) and write to the depth buffer.
    // Without this, the pipeline has no depth test and the overlays render through blocks.
    private static final DepthStencilState OVERLAY_DEPTH_STATE =
        new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true, 0f, 0f);

    private static final RenderPipeline.Snippet MATRICES_PROJECTION_SNIPPET =
        RenderPipeline.builder(new RenderPipeline.Snippet[0])
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .buildSnippet();


    public static final RenderPipeline LINE_PIPELINE =
        RenderPipeline.builder(new RenderPipeline.Snippet[]{MATRICES_PROJECTION_SNIPPET})
            .withLocation("pipeline/debug_lines")
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withCull(false)
            .withDepthStencilState(OVERLAY_DEPTH_STATE)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, Mode.DEBUG_LINES)
            .build();

    private static final RenderSetup LINE_SETUP =
        RenderSetup.builder(LINE_PIPELINE)
            .bufferSize(256)
            .createRenderSetup();

    public static final RenderPipeline TRIANGLE_PIPELINE =
        RenderPipeline.builder(new RenderPipeline.Snippet[]{MATRICES_PROJECTION_SNIPPET})
            .withVertexShader("core/position_color")
            .withLocation("position_color")
            .withFragmentShader("core/position_color")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .withCull(false)
            .withDepthStencilState(OVERLAY_DEPTH_STATE)
            .build();

    private static final RenderSetup TRIANGLE_SETUP =
        RenderSetup.builder(TRIANGLE_PIPELINE)
            .bufferSize(256)
            .createRenderSetup();

    public static final RenderType LIGHT_OVERLAY_LINES =
        RenderType.create("light_overlay_lines", LINE_SETUP);
    public static final RenderType LIGHT_OVERLAY_TRIANGLES =
        RenderType.create("light_overlay_triangles", TRIANGLE_SETUP);

    public static final RenderType POSITION_COLOR_OVERLAY = LIGHT_OVERLAY_LINES;
}



