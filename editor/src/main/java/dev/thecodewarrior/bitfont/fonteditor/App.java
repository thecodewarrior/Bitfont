/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package dev.thecodewarrior.bitfont.fonteditor;

import dev.thecodewarrior.bitfont.fonteditor.utils.GlobalAllocations;
import org.lwjgl.glfw.*;
import org.lwjgl.nuklear.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.opengl.ARBDebugOutput.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Nuklear demo using GLFW, OpenGL and stb_truetype for rendering.
 *
 * <p>This demo is a Java port of
 * <a href="https://github.com/vurtun/nuklear/tree/master/demo/glfw_opengl3">https://github.com/vurtun/nuklear/tree/master/demo/glfw_opengl3</a>.</p>
 */
public class App {

    private static final int BUFFER_INITIAL_SIZE = 4 * 1024;

    private static final int MAX_VERTEX_BUFFER = 512 * 1024;
    private static final int MAX_ELEMENT_BUFFER = 128 * 1024;

    public static final NkAllocator ALLOCATOR;
    private static final NkDrawVertexLayoutElement.Buffer VERTEX_LAYOUT;

    private static App instance;

    static {
        ALLOCATOR = NkAllocator.create()
                .alloc((handle, old, size) -> nmemAllocChecked(size))
                .mfree((handle, ptr) -> nmemFree(ptr));

        VERTEX_LAYOUT = NkDrawVertexLayoutElement.create(4)
                .position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
                .position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(8)
                .position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(16)
                .position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
                .flip();
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        instance = new App();
        instance.run();
    }

    public static App getInstance() {
        return instance;
    }

    public static long glfwWindow() {
        return getInstance().win;
    }

    public static final long WINDOW_ID_SENTINEL = 0x3124e471L << 32;
    public static final long WINDOW_ID_MASK = 0xffffffffL << 32;
    public static final float FONT_SIZE = 18;

    private long win;

    private int
            width,
            height;

    /**
     * How many consecutive frames the app has been focused for (caps at 100). This is used to prevent the issue where
     * clicking to focus the window will create a massive delta.
     */
    private int focusedFrames;

    private int
            display_width,
            display_height;

    private NkContext ctx = NkContext.create();

    private NkBuffer cmds = NkBuffer.create();
    private NkDrawNullTexture null_texture = NkDrawNullTexture.create();

    private int vbo, vao, ebo;
    private int prog;
    private int vert_shdr;
    private int frag_shdr;
    private int uniform_tex;
    private int uniform_proj;

    private NkColorf background = NkColorf.create()
            .r(0.10f)
            .g(0.18f)
            .b(0.24f)
            .a(1.0f);
    private final MainMenu menu = new MainMenu();
    public final List<Window> windows = new ArrayList<>();
    public final List<Integer> windowIds = new ArrayList<>();

    public App() {
        windows.add(new NuklearFontWindow());
    }

    private void run() {
        GLFWErrorCallback.createPrint().set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize glfw");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        if (Platform.get() == Platform.MACOSX) {
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        }
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);

        int WINDOW_WIDTH = 1280;
        int WINDOW_HEIGHT = 640;

        win = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Bitfont", NULL, NULL);
        if (win == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(win);
        GLCapabilities caps = GL.createCapabilities();
        Callback debugProc = GLUtil.setupDebugMessageCallback();

        if (caps.OpenGL43) {
            GL43.glDebugMessageControl(GL43.GL_DEBUG_SOURCE_API, GL43.GL_DEBUG_TYPE_OTHER, GL43.GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer) null, false);
        } else if (caps.GL_KHR_debug) {
            KHRDebug.glDebugMessageControl(
                    KHRDebug.GL_DEBUG_SOURCE_API,
                    KHRDebug.GL_DEBUG_TYPE_OTHER,
                    KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
                    (IntBuffer) null,
                    false
            );
        } else if (caps.GL_ARB_debug_output) {
            glDebugMessageControlARB(GL_DEBUG_SOURCE_API_ARB, GL_DEBUG_TYPE_OTHER_ARB, GL_DEBUG_SEVERITY_LOW_ARB, (IntBuffer) null, false);
        }

        NkContext ctx = setupWindow(win);

        nk_style_set_font(ctx, NuklearFonts.getSans("Medium", FONT_SIZE).getUserFont());

        glfwShowWindow(win);
        while (!glfwWindowShouldClose(win)) {
            /* Input */
            newFrame();

            menu.setFullWidth(width);
            menu.setFullHeight(height);
            menu.push(ctx);
            for (Window window : windows.toArray(new Window[0])) {
                // nk_end zeros out the scroll for god knows what reason
                ctx.input().mouse().scroll_delta().set(Input.INSTANCE.getScrollX(), Input.INSTANCE.getScrollY());
                window.push(ctx);
            }
            ctx.input().mouse().scroll_delta().set(Input.INSTANCE.getScrollX(), Input.INSTANCE.getScrollY());

            try (MemoryStack stack = stackPush()) {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);

                glfwGetWindowSize(win, width, height);
                glViewport(0, 0, width.get(0), height.get(0));

                glClearColor(background.r(), background.g(), background.b(), background.a());
            }
            glClear(GL_COLOR_BUFFER_BIT);
            /*
             * IMPORTANT: `nk_glfw_render` modifies some global OpenGL state
             * with blending, scissor, face culling, depth test and viewport and
             * defaults everything back into a default state.
             * Make sure to either a.) save and restore or b.) reset your own state after
             * rendering the UI.
             */
            render(NK_ANTI_ALIASING_ON, MAX_VERTEX_BUFFER, MAX_ELEMENT_BUFFER);
            glfwSwapBuffers(win);
        }

        shutdown();

        glfwFreeCallbacks(win);
        if (debugProc != null) {
            debugProc.free();
        }
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void setupContext() {
        String NK_SHADER_VERSION = Platform.get() == Platform.MACOSX ? "#version 150\n" : "#version 300 es\n";
        String vertex_shader =
                NK_SHADER_VERSION +
                        "uniform mat4 ProjMtx;\n" +
                        "in vec2 Position;\n" +
                        "in vec2 TexCoord;\n" +
                        "in vec4 Color;\n" +
                        "out vec2 Frag_UV;\n" +
                        "out vec4 Frag_Color;\n" +
                        "void main() {\n" +
                        "   Frag_UV = TexCoord;\n" +
                        "   Frag_Color = Color;\n" +
                        "   gl_Position = ProjMtx * vec4(Position.xy, 0, 1);\n" +
                        "}\n";
        String fragment_shader =
                NK_SHADER_VERSION +
                        "precision mediump float;\n" +
                        "uniform sampler2D Texture;\n" +
                        "in vec2 Frag_UV;\n" +
                        "in vec4 Frag_Color;\n" +
                        "out vec4 Out_Color;\n" +
                        "void main(){\n" +
                        "   Out_Color = Frag_Color * texture(Texture, Frag_UV.st);\n" +
                        "}\n";

        nk_buffer_init(cmds, ALLOCATOR, BUFFER_INITIAL_SIZE);
        prog = glCreateProgram();
        vert_shdr = glCreateShader(GL_VERTEX_SHADER);
        frag_shdr = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(vert_shdr, vertex_shader);
        glShaderSource(frag_shdr, fragment_shader);
        glCompileShader(vert_shdr);
        glCompileShader(frag_shdr);
        if (glGetShaderi(vert_shdr, GL_COMPILE_STATUS) != GL_TRUE) {
            throw new IllegalStateException();
        }
        if (glGetShaderi(frag_shdr, GL_COMPILE_STATUS) != GL_TRUE) {
            throw new IllegalStateException();
        }
        glAttachShader(prog, vert_shdr);
        glAttachShader(prog, frag_shdr);
        glLinkProgram(prog);
        if (glGetProgrami(prog, GL_LINK_STATUS) != GL_TRUE) {
            throw new IllegalStateException();
        }

        uniform_tex = glGetUniformLocation(prog, "Texture");
        uniform_proj = glGetUniformLocation(prog, "ProjMtx");
        int attrib_pos = glGetAttribLocation(prog, "Position");
        int attrib_uv = glGetAttribLocation(prog, "TexCoord");
        int attrib_col = glGetAttribLocation(prog, "Color");

        {
            // buffer setup
            vbo = glGenBuffers();
            ebo = glGenBuffers();
            vao = glGenVertexArrays();

            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

            glEnableVertexAttribArray(attrib_pos);
            glEnableVertexAttribArray(attrib_uv);
            glEnableVertexAttribArray(attrib_col);

            glVertexAttribPointer(attrib_pos, 2, GL_FLOAT, false, 20, 0);
            glVertexAttribPointer(attrib_uv, 2, GL_FLOAT, false, 20, 8);
            glVertexAttribPointer(attrib_col, 4, GL_UNSIGNED_BYTE, true, 20, 16);
        }

        {
            // null texture setup
            int nullTexID = glGenTextures();

            null_texture.texture().id(nullTexID);
            null_texture.uv().set(0.5f, 0.5f);

            glBindTexture(GL_TEXTURE_2D, nullTexID);
            try (MemoryStack stack = stackPush()) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1, 1, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, stack.ints(0xFFFFFFFF));
            }
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        }

        glBindTexture(GL_TEXTURE_2D, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private NkContext setupWindow(long win) {
        glfwSetScrollCallback(win, (window, xoffset, yoffset) -> {
            try (MemoryStack stack = stackPush()) {
                float factor = 160;
                float x = (float) xoffset * factor;
                float y = (float) yoffset * factor;
                Input.INSTANCE.addScroll(x, y);
                nk_input_scroll(ctx, NkVec2.mallocStack(stack).set(x, y));
            }
        });
        glfwSetCharCallback(win, (window, codepoint) -> nk_input_unicode(ctx, codepoint));
        glfwSetKeyCallback(win, this::process_key);
        glfwSetCursorPosCallback(win, (window, xpos, ypos) -> nk_input_motion(ctx, (int) xpos, (int) ypos));
        glfwSetMouseButtonCallback(win, (window, button, action, mods) -> {
            Input.INSTANCE.setMouse(button, action == GLFW_PRESS);

            try (MemoryStack stack = stackPush()) {
                DoubleBuffer cx = stack.mallocDouble(1);
                DoubleBuffer cy = stack.mallocDouble(1);

                glfwGetCursorPos(window, cx, cy);

                int x = (int) cx.get(0);
                int y = (int) cy.get(0);

                if(focusedFrames == 0) // if we just got focus, set the previous position to prevent any delta
                    ctx.input().mouse().prev().set(x, y);
                nk_input_motion(ctx, x, y);

                int nkButton;
                switch (button) {
                    case GLFW_MOUSE_BUTTON_RIGHT:
                        nkButton = NK_BUTTON_RIGHT;
                        break;
                    case GLFW_MOUSE_BUTTON_MIDDLE:
                        nkButton = NK_BUTTON_MIDDLE;
                        break;
                    default:
                        nkButton = NK_BUTTON_LEFT;
                }
                nk_input_button(ctx, nkButton, x, y, action == GLFW_PRESS);

                // clear all the crashed flags
                for(Window it : windows) {
                    it.setCrashed(false);
                }
            }
        });

        nk_init(ctx, ALLOCATOR, null);
        ctx.clip()
                .copy((handle, text, len) -> {
                    if (len == 0) {
                        return;
                    }

                    try (MemoryStack stack = stackPush()) {
                        ByteBuffer str = stack.malloc(len + 1);
                        memCopy(text, memAddress(str), len);
                        str.put(len, (byte) 0);

                        glfwSetClipboardString(win, str);
                    }
                })
                .paste((handle, edit) -> {
                    long text = nglfwGetClipboardString(win);
                    if (text != NULL) {
                        nnk_textedit_paste(edit, text, nnk_strlen(text));
                    }
                });

        setupContext();
        return ctx;
    }

    private void process_key(long window, int key, int scancode, int action, int mods) {
        Input.INSTANCE.setKey(key, action == GLFW_PRESS || action == GLFW_REPEAT, action == GLFW_REPEAT);

        boolean press = action == GLFW_PRESS || action == GLFW_REPEAT;
        switch (key) {
            case GLFW_KEY_LEFT_SHIFT:
            case GLFW_KEY_RIGHT_SHIFT:
                if(action == GLFW_REPEAT) nk_input_key(ctx, NK_KEY_SHIFT, false);
                nk_input_key(ctx, NK_KEY_SHIFT, press);
                break;
            case GLFW_KEY_LEFT_CONTROL:
            case GLFW_KEY_RIGHT_CONTROL:
                if(action == GLFW_REPEAT) nk_input_key(ctx, NK_KEY_CTRL, false);
                nk_input_key(ctx, NK_KEY_CTRL, press);
                break;
            case GLFW_KEY_DELETE:
                if(action == GLFW_REPEAT) nk_input_key(ctx, NK_KEY_DEL, false);
                nk_input_key(ctx, NK_KEY_DEL, press);
                break;
            case GLFW_KEY_ENTER:
                if(action == GLFW_REPEAT) nk_input_key(ctx, NK_KEY_ENTER, false);
                nk_input_key(ctx, NK_KEY_ENTER, press);
                break;
            case GLFW_KEY_TAB:
                if(action == GLFW_REPEAT) nk_input_key(ctx, NK_KEY_TAB, false);
                nk_input_key(ctx, NK_KEY_TAB, press);
                break;
            case GLFW_KEY_BACKSPACE:
                if(action == GLFW_REPEAT) nk_input_key(ctx, NK_KEY_BACKSPACE, false);
                nk_input_key(ctx, NK_KEY_BACKSPACE, press);
                break;
            case GLFW_KEY_UP:
                if(action == GLFW_REPEAT) nk_input_key(ctx, NK_KEY_UP, false);
                nk_input_key(ctx, NK_KEY_UP, press);
                break;
            case GLFW_KEY_DOWN:
                if(action == GLFW_REPEAT) nk_input_key(ctx, NK_KEY_DOWN, false);
                nk_input_key(ctx, NK_KEY_DOWN, press);
                break;
            case GLFW_KEY_LEFT:
                if(action == GLFW_REPEAT) nk_input_key(ctx, NK_KEY_LEFT, false);
                nk_input_key(ctx, NK_KEY_LEFT, press);
                break;
            case GLFW_KEY_RIGHT:
                if(action == GLFW_REPEAT) nk_input_key(ctx, NK_KEY_RIGHT, false);
                nk_input_key(ctx, NK_KEY_RIGHT, press);
                break;
        }

        Input.INSTANCE.setModifier(GLFW_MOD_ALT, (mods & GLFW_MOD_ALT) != 0);
        Input.INSTANCE.setModifier(GLFW_MOD_CONTROL, (mods & GLFW_MOD_CONTROL) != 0);
        Input.INSTANCE.setModifier(GLFW_MOD_SHIFT, (mods & GLFW_MOD_SHIFT) != 0);
        Input.INSTANCE.setModifier(GLFW_MOD_SUPER, (mods & GLFW_MOD_SUPER) != 0);
        Input.INSTANCE.setModifier(GLFW_MOD_CAPS_LOCK, (mods & GLFW_MOD_CAPS_LOCK) != 0);
        Input.INSTANCE.setModifier(GLFW_MOD_NUM_LOCK, (mods & GLFW_MOD_NUM_LOCK) != 0);

        int modifiers = mods & ~GLFW_MOD_CAPS_LOCK & ~GLFW_MOD_NUM_LOCK;

        switch (key) {
            case GLFW_KEY_C:
                keyShortcut(press, modifiers, GLFW_MOD_CONTROL, GLFW_MOD_SUPER, NK_KEY_COPY);
                break;
            case GLFW_KEY_X:
                keyShortcut(press, modifiers, GLFW_MOD_CONTROL, GLFW_MOD_SUPER, NK_KEY_CUT);
                break;
            case GLFW_KEY_V:
                keyShortcut(press, modifiers, GLFW_MOD_CONTROL, GLFW_MOD_SUPER, NK_KEY_PASTE);
                break;
            case GLFW_KEY_Z:
                keyShortcut(press, modifiers, GLFW_MOD_CONTROL, GLFW_MOD_SUPER, NK_KEY_TEXT_UNDO);
                keyShortcut(press, modifiers, -1, GLFW_MOD_SHIFT | GLFW_MOD_SUPER, NK_KEY_TEXT_REDO);
                break;
            case GLFW_KEY_Y:
                keyShortcut(press, modifiers, GLFW_MOD_CONTROL, -1, NK_KEY_TEXT_REDO);
                break;
            case GLFW_KEY_A:
                keyShortcut(press, modifiers, GLFW_MOD_CONTROL, GLFW_MOD_SUPER, NK_KEY_TEXT_SELECT_ALL);
                break;
//            case GLFW_KEY_:
//                nk_input_key(ctx, NK_KEY_TEXT_INSERT_MODE, press);
//                break;
//            case GLFW_KEY_:
//                nk_input_key(ctx, NK_KEY_TEXT_REPLACE_MODE, press);
//                break;
//            case GLFW_KEY_:
//                nk_input_key(ctx, NK_KEY_TEXT_RESET_MODE, press);
//                break;
            case GLFW_KEY_LEFT:
                keyShortcut(press, modifiers & ~GLFW_MOD_SHIFT, -1, GLFW_MOD_SUPER, NK_KEY_TEXT_LINE_START);
                keyShortcut(press, modifiers & ~GLFW_MOD_SHIFT, -1, GLFW_MOD_ALT, NK_KEY_TEXT_WORD_LEFT);
                keyShortcut(press, modifiers & ~GLFW_MOD_SHIFT, GLFW_MOD_CONTROL, -1, NK_KEY_TEXT_WORD_LEFT);
                break;
            case GLFW_KEY_RIGHT:
                keyShortcut(press, modifiers & ~GLFW_MOD_SHIFT, -1, GLFW_MOD_SUPER, NK_KEY_TEXT_LINE_END);
                keyShortcut(press, modifiers & ~GLFW_MOD_SHIFT, -1, GLFW_MOD_ALT, NK_KEY_TEXT_WORD_RIGHT);
                keyShortcut(press, modifiers & ~GLFW_MOD_SHIFT, GLFW_MOD_CONTROL, -1, NK_KEY_TEXT_WORD_RIGHT);
                break;
            case GLFW_KEY_HOME:
                keyShortcut(press, modifiers & ~GLFW_MOD_SHIFT, 0, 0, NK_KEY_TEXT_LINE_START);
                break;
            case GLFW_KEY_END:
                keyShortcut(press, modifiers & ~GLFW_MOD_SHIFT, 0, 0, NK_KEY_TEXT_LINE_END);
                break;
            case GLFW_KEY_UP:
                keyShortcut(press, modifiers & ~GLFW_MOD_SHIFT, -1, GLFW_MOD_SUPER, NK_KEY_TEXT_START);
                break;
            case GLFW_KEY_DOWN:
                keyShortcut(press, modifiers & ~GLFW_MOD_SHIFT, -1, GLFW_MOD_SUPER, NK_KEY_TEXT_END);
                break;
//            case GLFW_KEY_:
//                nk_input_key(ctx, NK_KEY_SCROLL_START, press);
//                break;
//            case GLFW_KEY_:
//                nk_input_key(ctx, NK_KEY_SCROLL_END, press);
//                break;
//            case GLFW_KEY_:
//                nk_input_key(ctx, NK_KEY_SCROLL_DOWN, press);
//                break;
//            case GLFW_KEY_:
//                nk_input_key(ctx, NK_KEY_SCROLL_UP, press);
//                break;
        }
    }

    private void keyShortcut(boolean press, int modifiers, int targetMods, int macTargetMods, int key) {
        int target = Platform.get() == Platform.MACOSX ? macTargetMods : targetMods;
        if (target == -1) return;
        if (press && modifiers == target)
            nk_input_key(ctx, key, true);
        else if (nk_input_is_key_down(ctx.input(), key))
            nk_input_key(ctx, key, false);
    }

    private void newFrame() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);

            glfwGetWindowSize(win, w, h);
            width = w.get(0);
            height = h.get(0);


            glfwGetFramebufferSize(win, w, h);
            display_width = w.get(0);
            display_height = h.get(0);
        }

        Input.INSTANCE.flush();
        nk_input_begin(ctx);
        glfwPollEvents();

        NkMouse mouse = ctx.input().mouse();
        if (mouse.grab()) {
            glfwSetInputMode(win, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        } else if (mouse.grabbed()) {
            float prevX = mouse.prev().x();
            float prevY = mouse.prev().y();
            glfwSetCursorPos(win, prevX, prevY);
            mouse.pos().x(prevX);
            mouse.pos().y(prevY);
        } else if (mouse.ungrab()) {
            glfwSetInputMode(win, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }

        nk_input_end(ctx);

        if(glfwGetWindowAttrib(win, GLFW_FOCUSED) == GLFW_FALSE) {
            focusedFrames = 0;
        } else if(focusedFrames < 100) {
            focusedFrames++;
        }
    }

    private void render(int AA, int max_vertex_buffer, int max_element_buffer) {
        try (MemoryStack stack = stackPush()) {
            // setup global state
            glEnable(GL_BLEND);
            glBlendEquation(GL_FUNC_ADD);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDisable(GL_CULL_FACE);
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_SCISSOR_TEST);
            glActiveTexture(GL_TEXTURE0);

            // setup program
            glUseProgram(prog);
            glUniform1i(uniform_tex, 0);
            glUniformMatrix4fv(uniform_proj, false, stack.floats(
                    2.0f / width, 0.0f, 0.0f, 0.0f,
                    0.0f, -2.0f / height, 0.0f, 0.0f,
                    0.0f, 0.0f, -1.0f, 0.0f,
                    -1.0f, 1.0f, 0.0f, 1.0f
            ));
            glViewport(0, 0, display_width, display_height);
        }

        {
            // convert from command queue into draw list and draw to screen

            // allocate vertex and element buffer
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

            glBufferData(GL_ARRAY_BUFFER, max_vertex_buffer, GL_STREAM_DRAW);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, max_element_buffer, GL_STREAM_DRAW);

            // load draw vertices & elements directly into vertex + element buffer
            ByteBuffer vertices = Objects.requireNonNull(glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, max_vertex_buffer, null));
            ByteBuffer elements = Objects.requireNonNull(glMapBuffer(GL_ELEMENT_ARRAY_BUFFER, GL_WRITE_ONLY, max_element_buffer, null));
            try (MemoryStack stack = stackPush()) {
                // fill convert configuration
                NkConvertConfig config = NkConvertConfig.callocStack(stack)
                        .vertex_layout(VERTEX_LAYOUT)
                        .vertex_size(20)
                        .vertex_alignment(4)
                        .null_texture(null_texture)
                        .circle_segment_count(22)
                        .curve_segment_count(22)
                        .arc_segment_count(22)
                        .global_alpha(1.0f)
                        .shape_AA(AA)
                        .line_AA(AA);

                // setup buffers to load vertices and elements
                NkBuffer vbuf = NkBuffer.mallocStack(stack);
                NkBuffer ebuf = NkBuffer.mallocStack(stack);

                nk_buffer_init_fixed(vbuf, vertices/*, max_vertex_buffer*/);
                nk_buffer_init_fixed(ebuf, elements/*, max_element_buffer*/);
                nk_convert(ctx, cmds, vbuf, ebuf, config);
            }
            glUnmapBuffer(GL_ELEMENT_ARRAY_BUFFER);
            glUnmapBuffer(GL_ARRAY_BUFFER);

            // iterate over and execute each draw command
            float fb_scale_x = (float) display_width / (float) width;
            float fb_scale_y = (float) display_height / (float) height;

            windowIds.clear();
            long offset = NULL;
            for (NkDrawCommand cmd = nk__draw_begin(ctx, cmds); cmd != null; cmd = nk__draw_next(cmd, cmds, ctx)) {
                if((cmd.userdata().ptr() & WINDOW_ID_MASK) == WINDOW_ID_SENTINEL) {
                    int windowId = (int)(cmd.userdata().ptr() & ~WINDOW_ID_MASK);
                    if(!windowIds.contains(windowId)) windowIds.add(windowId);
                }
                if (cmd.elem_count() == 0) {
                    continue;
                }
                glBindTexture(GL_TEXTURE_2D, cmd.texture().id());
                glScissor(
                        (int) (cmd.clip_rect().x() * fb_scale_x),
                        (int) ((height - (int) (cmd.clip_rect().y() + cmd.clip_rect().h())) * fb_scale_y),
                        (int) (cmd.clip_rect().w() * fb_scale_x),
                        (int) (cmd.clip_rect().h() * fb_scale_y)
                );
                glDrawElements(GL_TRIANGLES, cmd.elem_count(), GL_UNSIGNED_SHORT, offset);
                offset += cmd.elem_count() * 2;
            }
            nk_clear(ctx);
            nk_buffer_clear(cmds);
        }

        // default OpenGL state
        glUseProgram(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glDisable(GL_BLEND);
        glDisable(GL_SCISSOR_TEST);
    }

    private void destroy() {
        glDetachShader(prog, vert_shdr);
        glDetachShader(prog, frag_shdr);
        glDeleteShader(vert_shdr);
        glDeleteShader(frag_shdr);
        glDeleteProgram(prog);
        glDeleteTextures(null_texture.texture().id());
        // I would jump through hoops to delete all the font atlas textures, but the process is already ending, there's
        // no point adding that complexity
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        nk_buffer_free(cmds);

        GL.setCapabilities(null);
    }

    private void shutdown() {
        GlobalAllocations.free();
        menu.free();
        for (Window window : windows) {
            window.free();
        }

        Objects.requireNonNull(ctx.clip().copy()).free();
        Objects.requireNonNull(ctx.clip().paste()).free();
        nk_free(ctx);
        destroy();
        Objects.requireNonNull(ALLOCATOR.alloc()).free();
        Objects.requireNonNull(ALLOCATOR.mfree()).free();
    }

}