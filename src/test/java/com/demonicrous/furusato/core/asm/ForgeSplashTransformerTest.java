package com.demonicrous.furusato.core.asm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.minecraftforge.fml.client.SplashProgress;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class ForgeSplashTransformerTest {
    private static final String TARGET = "net.minecraftforge.fml.client.SplashProgress$2";

    @Test
    public void replacesForgeSplashWithSingleBarRenderer() throws IOException {
        byte[] original = readClassBytes();
        byte[] transformed = new ForgeSplashTransformer().transform(TARGET, TARGET, original);
        ClassNode node = new ClassNode();
        new ClassReader(transformed).accept(node, 0);

        MethodNode drawBar = findMethod(node, "drawBar");
        MethodNode run = findMethod(node, "run");
        assertNotNull(drawBar);
        assertNotNull(run);

        int rendererCalls = 0;
        for (AbstractInsnNode instruction : drawBar.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode
                    && "com/demonicrous/furusato/core/asm/FurusatoSplashRenderer"
                    .equals(((MethodInsnNode) instruction).owner)) {
                rendererCalls++;
            }
        }
        assertEquals(1, rendererCalls);

        boolean skipsExtraBars = false;
        for (AbstractInsnNode instruction : run.instructions.toArray()) {
            if (instruction instanceof JumpInsnNode
                    && instruction.getPrevious() instanceof MethodInsnNode
                    && "drawBar".equals(((MethodInsnNode) instruction.getPrevious()).name)) {
                skipsExtraBars = true;
                break;
            }
        }
        assertTrue(skipsExtraBars);
    }

    private static byte[] readClassBytes() throws IOException {
        String resource = "/" + TARGET.replace('.', '/') + ".class";
        try (InputStream input = SplashProgress.class.getResourceAsStream(resource)) {
            assertNotNull(input);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private static MethodNode findMethod(ClassNode node, String name) {
        for (MethodNode method : node.methods) {
            if (name.equals(method.name)) {
                return method;
            }
        }
        return null;
    }
}
