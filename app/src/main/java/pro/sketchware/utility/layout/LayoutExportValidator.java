package pro.sketchware.utility.layout;

import com.besome.sketch.beans.ViewBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps malformed editor metadata from silently dropping views during XML generation.
 */
public final class LayoutExportValidator {

    private LayoutExportValidator() {
    }

    public static ArrayList<ViewBean> prepareForExport(
            String layoutName, ArrayList<ViewBean> views) {
        Map<String, ViewBean> viewsById = new HashMap<>();

        for (ViewBean view : views) {
            if (view == null || view.id == null || view.id.isBlank()) {
                throw invalidLayout(layoutName, "contains a widget without an ID");
            }
            if (viewsById.put(view.id, view) != null) {
                throw invalidLayout(layoutName, "contains duplicate widget ID \"" + view.id + "\"");
            }
        }

        for (ViewBean view : views) {
            String parentId = view.parent;
            if (isRoot(parentId)) {
                view.parent = "root";
                continue;
            }

            ViewBean parent = viewsById.get(parentId);
            if (parent == null) {
                throw invalidLayout(layoutName, "contains orphan widget \"" + view.id
                        + "\" with missing parent \"" + parentId + "\"");
            }
            if (parent == view) {
                throw invalidLayout(layoutName, "contains self-parented widget \"" + view.id + "\"");
            }
            if (!parent.getClassInfo().a("ViewGroup")) {
                throw invalidLayout(layoutName, "uses non-container widget \"" + parentId
                        + "\" as parent of \"" + view.id + "\"");
            }
            view.parentType = parent.type;
        }

        for (ViewBean view : views) {
            verifyNoParentCycle(layoutName, view, viewsById);
        }

        return views;
    }

    public static void verifyXmlContainsAllViews(
            String layoutName, String xml, List<ViewBean> views, boolean customOverride) {
        if (xml == null || xml.isBlank()) {
            throw invalidLayout(layoutName, "generated empty XML");
        }

        List<String> missingIds = new ArrayList<>();
        for (ViewBean view : views) {
            if ("include".equals(view.convert)) {
                continue;
            }
            String id = view.id;
            if (!xml.contains("@+id/" + id) && !xml.contains("@id/" + id)) {
                missingIds.add(id);
            }
        }

        if (!missingIds.isEmpty()) {
            String source = customOverride ? "custom XML override" : "generated XML";
            throw invalidLayout(layoutName, source + " is missing widget IDs: "
                    + String.join(", ", missingIds));
        }
    }

    private static void verifyNoParentCycle(
            String layoutName, ViewBean start, Map<String, ViewBean> viewsById) {
        Set<String> visited = new HashSet<>();
        ViewBean current = start;

        while (!isRoot(current.parent)) {
            if (!visited.add(current.id)) {
                throw invalidLayout(layoutName, "contains a parent cycle at widget \""
                        + start.id + "\"");
            }
            ViewBean parent = viewsById.get(current.parent);
            if (parent == null) {
                throw invalidLayout(layoutName, "contains orphan widget \"" + current.id + "\"");
            }
            current = parent;
        }
    }

    private static boolean isRoot(String parentId) {
        return parentId == null || parentId.isBlank() || "root".equals(parentId);
    }

    private static IllegalStateException invalidLayout(String layoutName, String reason) {
        return new IllegalStateException("Layout \"" + layoutName + "\" " + reason + ".");
    }
}
