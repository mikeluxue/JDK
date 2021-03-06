/*
 * Copyright (c) 2009, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package org.graalvm.compiler.nodes;

import org.graalvm.compiler.debug.DebugContext;
import org.graalvm.compiler.graph.NodeClass;
import org.graalvm.compiler.lir.gen.LIRGeneratorTool;
import org.graalvm.compiler.nodeinfo.NodeInfo;
import org.graalvm.compiler.nodes.spi.LIRLowerable;
import org.graalvm.compiler.nodes.spi.Lowerable;
import org.graalvm.compiler.nodes.spi.LoweringTool;
import org.graalvm.compiler.nodes.spi.NodeLIRBuilderTool;

import jdk.vm.ci.meta.DeoptimizationAction;
import jdk.vm.ci.meta.DeoptimizationReason;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.Value;

@NodeInfo(shortName = "Deopt", nameTemplate = "Deopt {p#reason/s}")
public final class DeoptimizeNode extends AbstractDeoptimizeNode implements Lowerable, LIRLowerable {
    public static final int DEFAULT_DEBUG_ID = 0;

    public static final NodeClass<DeoptimizeNode> TYPE = NodeClass.create(DeoptimizeNode.class);
    protected final DeoptimizationAction action;
    protected final DeoptimizationReason reason;
    protected int debugId;
    protected final JavaConstant speculation;

    public DeoptimizeNode(DeoptimizationAction action, DeoptimizationReason reason) {
        this(action, reason, DEFAULT_DEBUG_ID, JavaConstant.NULL_POINTER, null);
    }

    public DeoptimizeNode(DeoptimizationAction action, DeoptimizationReason reason, JavaConstant speculation) {
        this(action, reason, DEFAULT_DEBUG_ID, speculation, null);
    }

    public DeoptimizeNode(DeoptimizationAction action, DeoptimizationReason reason, int debugId, JavaConstant speculation, FrameState stateBefore) {
        super(TYPE, stateBefore);
        assert action != null;
        assert reason != null;
        assert speculation.getJavaKind() == JavaKind.Object;
        this.action = action;
        this.reason = reason;
        this.debugId = debugId;
        this.speculation = speculation;
    }

    public DeoptimizationAction action() {
        return action;
    }

    public DeoptimizationReason reason() {
        return reason;
    }

    @Override
    public void lower(LoweringTool tool) {
        tool.getLowerer().lower(this, tool);
    }

    @SuppressWarnings("deprecation")
    public int getDebugId() {
        int deoptDebugId = debugId;
        if (deoptDebugId == DEFAULT_DEBUG_ID) {
            DebugContext debug = getDebug();
            if ((debug.isDumpEnabledForMethod() || debug.isLogEnabledForMethod())) {
                deoptDebugId = this.getId();
            }
        }
        return deoptDebugId;
    }

    public void setDebugId(int debugId) {
        assert debugId != DEFAULT_DEBUG_ID;
        this.debugId = debugId;
    }

    @Override
    public void generate(NodeLIRBuilderTool gen) {
        LIRGeneratorTool tool = gen.getLIRGeneratorTool();
        Value actionAndReason = tool.emitJavaConstant(tool.getMetaAccess().encodeDeoptActionAndReason(action, reason, getDebugId()));
        Value speculationValue = tool.emitJavaConstant(speculation);
        gen.getLIRGeneratorTool().emitDeoptimize(actionAndReason, speculationValue, gen.state(this));
    }

    @Override
    public ValueNode getActionAndReason(MetaAccessProvider metaAccess) {
        return ConstantNode.forConstant(metaAccess.encodeDeoptActionAndReason(action, reason, getDebugId()), metaAccess, graph());
    }

    @Override
    public ValueNode getSpeculation(MetaAccessProvider metaAccess) {
        return ConstantNode.forConstant(speculation, metaAccess, graph());
    }

    public JavaConstant getSpeculation() {
        return speculation;
    }

    @NodeIntrinsic
    public static native void deopt(@ConstantNodeParameter DeoptimizationAction action, @ConstantNodeParameter DeoptimizationReason reason);
}
