package exhibition.util.render.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;

public class OutlineShader {

    public static final OutlineShader instance;

    Framebuffer entityFBO, overlayFBO;

    static {
        instance = new OutlineShader();
    }

    public OutlineShader() {
        setupFBO();
    }

    private void setupFBO() {
        entityFBO = new Framebuffer(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, false);
        overlayFBO = new Framebuffer(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, false);
    }

    private int createShader(String shaderCode, int shaderType) throws Exception {
        int shader = 0;
        try {
            //Create shader program
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

            if (shader == 0)
                return 0;

            //Load and compile shader src
            ARBShaderObjects.glShaderSourceARB(shader, shaderCode);
            ARBShaderObjects.glCompileShaderARB(shader);

            //Check for errors
            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
                throw new RuntimeException("_error_ creating shader: " + getLogInfo(shader));

            return shader;
        } catch (Exception exc) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw exc;
        }
    }

    private static String getLogInfo(int obj) {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    private int shaderProgramID = -1, vertexShaderID = -1, fragmentShaderID = -1, diffuseSamperUniformID = -1, texelSizeUniformID = -1;

    public void initialize() {
        if (this.shaderProgramID == -1) {
            this.shaderProgramID = ARBShaderObjects.glCreateProgramObjectARB();
            try {
                if (this.vertexShaderID == -1) {
                    //Store vertex shader code
                    String vertexShaderCode =
                            "#version 120 \n" +
                                    "void main() { \n" +
                                    "gl_TexCoord[0] = gl_MultiTexCoord0; \n" +
                                    "gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex; \n" +
                                    "}";
                    //Create vertex shader
                    this.vertexShaderID = this.createShader(vertexShaderCode, ARBVertexShader.GL_VERTEX_SHADER_ARB);
                }
                if (this.fragmentShaderID == -1) {
                    //Store fragment shader
                    String fragmentShaderCode =
                            "#version 120 \n" +
                                    "uniform sampler2D DiffuseSamper; \n" +
                                    "uniform vec2 TexelSize; \n" +
                                    "void main(){ \n" +
                                    "vec4 centerCol = texture2D(DiffuseSamper, gl_TexCoord[0].st); \n" +
                                    "if(centerCol.r == 1.0f && centerCol.g == 1.0f && centerCol.b == 1.0f) { \n" +
                                    "gl_FragColor = vec4(0, 0, 0, 0); \n" +
                                    "return; \n" +
                                    "} \n" +
                                    "vec4 colAvg = vec4(0, 0, 0, 0); \n" +
                                    "for(int xo = -7; xo < 7; xo++) { \n" +
                                    "for(int yo = -7; yo < 7; yo++) { \n" +
                                    "vec4 currCol = texture2D(DiffuseSamper, gl_TexCoord[0].st + vec2(xo * TexelSize.x, yo * TexelSize.y)); \n" +
                                    "if(currCol.r != 0.0F || currCol.g != 0.0F || currCol.b != 0.0F) { \n" +
                                    "colAvg += vec4(1, 0, 0, max(0, (6.0f - sqrt(xo*xo*1.0f + yo*yo*1.0f)) / 2.0F)); \n" +
                                    "} \n" +
                                    "} \n" +
                                    "} \n" +
                                    "colAvg.a /= 64.0F; \n" +
                                    "gl_FragColor = colAvg; \n" +
                                    "}";
                    //Create fragment shader
                    this.fragmentShaderID = this.createShader(fragmentShaderCode, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
                }
            } catch (Exception ex) {
                this.shaderProgramID = -1;
                this.vertexShaderID = -1;
                this.fragmentShaderID = -1;
                ex.printStackTrace();
            }
            if (this.shaderProgramID != -1) {
                //Attach vertex shader to shader program
                ARBShaderObjects.glAttachObjectARB(this.shaderProgramID, this.vertexShaderID);

                //Attach fragment shader to shader program
                ARBShaderObjects.glAttachObjectARB(this.shaderProgramID, this.fragmentShaderID);

                //Link (compile) shader program
                ARBShaderObjects.glLinkProgramARB(this.shaderProgramID);

                //Check for errors
                if (ARBShaderObjects.glGetObjectParameteriARB(this.shaderProgramID, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
                    System.err.println(getLogInfo(this.shaderProgramID));
                    return;
                }
                ARBShaderObjects.glValidateProgramARB(this.shaderProgramID);
                if (ARBShaderObjects.glGetObjectParameteriARB(this.shaderProgramID, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
                    System.err.println(getLogInfo(this.shaderProgramID));
                    return;
                }

                //Unbind shader
                ARBShaderObjects.glUseProgramObjectARB(0);

                //Get diffuse sampler uniform location (ID)
                this.diffuseSamperUniformID = ARBShaderObjects.glGetUniformLocationARB(this.shaderProgramID, "DiffuseSamper");
                //Get texel size uniform location (ID)
                this.texelSizeUniformID = ARBShaderObjects.glGetUniformLocationARB(this.shaderProgramID, "TexelSize");
            }
        }
    }

    public void uploadSample() {
        //Set sampler uniform to texture unit 0
        ARBShaderObjects.glUniform1iARB(diffuseSamperUniformID, 0);

        //Activate texture unit 0
        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        //Bind FBO texture, sets texture unit 0
        this.entityFBO.bindFramebufferTexture();
    }

    public void uploadTexelSize() {
        //Getting scaled resolution
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        //Creating float buffer with the values
        FloatBuffer texelSizeBuffer = BufferUtils.createFloatBuffer(2);
        texelSizeBuffer.position(0);
        texelSizeBuffer.put(1.0F / sr.getScaledWidth());
        texelSizeBuffer.put(1.0F / sr.getScaledHeight());

        //This is important
        texelSizeBuffer.flip();

        //Upload to uniform
        ARBShaderObjects.glUniform2ARB(this.texelSizeUniformID, texelSizeBuffer);
    }

    public void clear() {
        //Bind result FBO before rendering
        //I wrote this ESP for MC 1.7.2, the field name has probably changed.
        this.overlayFBO.bindFramebuffer(false);

        //Clear result FBO texture
        GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);

        //Bind entity FBO before rendering
        //I wrote this ESP for MC 1.7.2, the field name has probably changed.
        this.entityFBO.bindFramebuffer(false);

        //Clear entity FBO texture
        GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
    }

}
