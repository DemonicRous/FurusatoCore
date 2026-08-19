package dev.demonicrous.furusato.core.module;

import dev.demonicrous.furusato.api.module.ModuleState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class DependencyResolver {
    List<ModuleManager.ModuleRecord> resolve(
            Map<String, ModuleManager.ModuleRecord> records) {
        for (ModuleManager.ModuleRecord record : records.values()) {
            for (String dependency : record.metadata().requiredDependencies()) {
                if (!records.containsKey(dependency)) {
                    record.disable("missing required dependency: " + dependency);
                    break;
                }
            }
        }

        Map<String, VisitState> visits = new HashMap<String, VisitState>();
        List<ModuleManager.ModuleRecord> ordered =
                new ArrayList<ModuleManager.ModuleRecord>();
        List<String> stack = new ArrayList<String>();
        for (String id : records.keySet()) {
            visit(id, records, visits, stack, ordered);
        }
        return ordered;
    }

    private void visit(String id, Map<String, ModuleManager.ModuleRecord> records,
            Map<String, VisitState> visits, List<String> stack,
            List<ModuleManager.ModuleRecord> ordered) {
        VisitState state = visits.get(id);
        if (state == VisitState.VISITED) {
            return;
        }
        if (state == VisitState.VISITING) {
            markCycle(id, records, stack);
            return;
        }

        visits.put(id, VisitState.VISITING);
        stack.add(id);
        ModuleManager.ModuleRecord record = records.get(id);
        List<String> dependencies = new ArrayList<String>();
        dependencies.addAll(record.metadata().requiredDependencies());
        for (String optional : record.metadata().optionalDependencies()) {
            if (records.containsKey(optional)) {
                dependencies.add(optional);
            }
        }
        Collections.sort(dependencies);
        for (String dependency : dependencies) {
            if (records.containsKey(dependency)) {
                visit(dependency, records, visits, stack, ordered);
            }
        }
        stack.remove(stack.size() - 1);
        visits.put(id, VisitState.VISITED);
        if (!ordered.contains(record)) {
            ordered.add(record);
        }
    }

    private void markCycle(String repeatedId,
            Map<String, ModuleManager.ModuleRecord> records, List<String> stack) {
        int start = stack.indexOf(repeatedId);
        List<String> cycle = new ArrayList<String>(stack.subList(start, stack.size()));
        cycle.add(repeatedId);
        String detail = "dependency cycle: " + join(cycle);
        for (int index = start; index < stack.size(); index++) {
            ModuleManager.ModuleRecord record = records.get(stack.get(index));
            record.fail(detail, null);
        }
    }

    private String join(List<String> ids) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < ids.size(); index++) {
            if (index > 0) {
                builder.append(" -> ");
            }
            builder.append(ids.get(index));
        }
        return builder.toString();
    }

    private enum VisitState {
        VISITING,
        VISITED
    }
}
