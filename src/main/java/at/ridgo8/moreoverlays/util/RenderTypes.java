package at.ridgo8.moreoverlays.util;

import java.util.OptionalDouble;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.VertexFormat;

public final class RenderTypes {

    private RenderTypes() {}

    private static final RenderType LINE_PIPELINE = RenderType.create("light_overlay_lines",
        256,
        RenderPipeline.builder()
            .withVertexShader("core/position_color")
            .withLocation("position_color")
            .withFragmentShader("core/position_color")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES)
            .withCull(false)
            .build(),
        RenderType.CompositeState.builder()
            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(1.0)))
            .createCompositeState(false));

    private static final RenderType TRIANGLE_PIPELINE = RenderType.create("light_overlay_triangles",
        256,
        RenderPipeline.builder()
            .withVertexShader("core/position_color")
            .withLocation("position_color")
            .withFragmentShader("core/position_color")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .withCull(false)
            .build(),
        RenderType.CompositeState.builder()
            .createCompositeState(false));

    public static final RenderType LIGHT_OVERLAY_LINES = LINE_PIPELINE;
    public static final RenderType LIGHT_OVERLAY_TRIANGLES = TRIANGLE_PIPELINE;
    
    public static final RenderType POSITION_COLOR_OVERLAY = LINE_PIPELINE;
    
    // Not used anymore; GUI overlays are drawn via GuiGraphics
    public static final RenderType GUI_OVERLAY_QUADS = RenderType.solid();
}


