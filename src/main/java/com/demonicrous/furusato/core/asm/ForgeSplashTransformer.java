package com.demonicrous.furusato.core.asm;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Simplifies Forge's early splash to one smooth, unobtrusive progress bar. */
public final class ForgeSplashTransformer implements IClassTransformer {
    private static final Logger LOGGER = LogManager.getLogger("Furusato Core/Splash ASM");
    private static final String TARGET = "net.minecraftforge.fml.client.SplashProgress$2";
    private static final String DRAW_BAR_DESC =
            "(Lnet/minecraftforge/fml/common/ProgressManager$ProgressBar;)V";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || (!TARGET.equals(name) && !TARGET.equals(transformedName))) {
            return basicClass;
        }

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);
        boolean rendererReplaced = false;
        boolean extraBarsRemoved = false;

        for (MethodNode method : node.methods) {
            if ("drawBar".equals(method.name) && DRAW_BAR_DESC.equals(method.desc)) {
                replaceBarRenderer(method);
                rendererReplaced = true;
            } else if ("run".equals(method.name) && "()V".equals(method.desc)) {
                extraBarsRemoved = keepOnlyPrimaryBar(method);
            }
        }

        if (!rendererReplaced || !extraBarsRemoved) {
            LOGGER.warn("Forge splash layout did not match the expected 1.12.2 structure");
            return basicClass;
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        LOGGER.info("Installed the single-bar Furusato loading layout");
        return writer.toByteArray();
    }

    private static void replaceBarRenderer(MethodNode method) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        if (method.localVariables != null) {
            method.localVariables.clear();
        }

        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "com/demonicrous/furusato/core/asm/FurusatoSplashRenderer",
                "draw",
                DRAW_BAR_DESC,
                false
        ));
        instructions.add(new InsnNode(Opcodes.RETURN));
        method.instructions.add(instructions);
    }

    private static boolean keepOnlyPrimaryBar(MethodNode method) {
        List<MethodInsnNode> drawCalls = new ArrayList<MethodInsnNode>();
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof LdcInsnNode
                    && Float.valueOf(310.0F).equals(((LdcInsnNode) instruction).cst)) {
                ((LdcInsnNode) instruction).cst = Float.valueOf(390.0F);
            }
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if ("<init>".equals(call.name)
                        && "net/minecraftforge/fml/client/SplashProgress$Texture".equals(call.owner)
                        && call.getPrevious() != null
                        && call.getPrevious().getOpcode() == Opcodes.ICONST_0) {
                    method.instructions.set(call.getPrevious(), new InsnNode(Opcodes.ICONST_1));
                }
                if (TARGET.replace('.', '/').equals(call.owner)
                        && "drawBar".equals(call.name)
                        && DRAW_BAR_DESC.equals(call.desc)) {
                    drawCalls.add(call);
                }
            }
        }

        if (drawCalls.size() != 3) {
            return false;
        }

        AbstractInsnNode popMatrix = drawCalls.get(2).getNext();
        while (popMatrix instanceof LabelNode || !(popMatrix instanceof MethodInsnNode)
                || !"glPopMatrix".equals(((MethodInsnNode) popMatrix).name)) {
            popMatrix = popMatrix.getNext();
            if (popMatrix == null) {
                return false;
            }
        }

        LabelNode end = new LabelNode();
        method.instructions.insertBefore(popMatrix, end);
        method.instructions.insert(drawCalls.get(0), new JumpInsnNode(Opcodes.GOTO, end));
        return true;
    }
}
