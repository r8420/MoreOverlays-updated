package at.ridgo8.moreoverlays.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class RenderTypes {

    private RenderTypes() {}

    private static final RenderPipeline.Snippet MATRICES_PROJECTION_SNIPPET =
        RenderPipeline.builder(new RenderPipeline.Snippet[0])
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .buildSnippet();


    private static final RenderPipeline LINE_PIPELINE =
        RenderPipeline.builder(new RenderPipeline.Snippet[]{MATRICES_PROJECTION_SNIPPET})
            .withLocation("pipeline/debug_lines")
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, Mode.DEBUG_LINES)
            .build();

    private static final RenderSetup LINE_SETUP =
        RenderSetup.builder(LINE_PIPELINE)
            .bufferSize(256)
            .createRenderSetup();

    private static final RenderPipeline TRIANGLE_PIPELINE =
        RenderPipeline.builder(new RenderPipeline.Snippet[]{MATRICES_PROJECTION_SNIPPET})
            .withVertexShader("core/position_color")
            .withLocation("position_color")
            .withFragmentShader("core/position_color")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .withCull(false)
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



