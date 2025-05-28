package com.example.nlwebspringai.mcp.model;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// A simplified representation of the snapshot data, focusing on accessible elements.
// The actual snapshot can be very complex.
@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotData {

    private String url;
    private String title;
    private List<AccessibleElement> accessibleTree; // Assuming this is a key part of the snapshot
    // Other fields like viewport, dom, etc., can be added if needed

    // Getters and setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<AccessibleElement> getAccessibleTree() {
        return accessibleTree;
    }

    public void setAccessibleTree(List<AccessibleElement> accessibleTree) {
        this.accessibleTree = accessibleTree;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccessibleElement {
        private String ref;
        private String role;
        private String name;
        private String text; // Sometimes 'text' is used instead of 'name'
        private List<AccessibleElement> children;
        private Map<String, String> attributes; // e.g., "aria-label", "placeholder"

        // Getters and setters
        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getName() {
            // Fallback to text if name is null or empty, as sometimes one or the other is used.
            if (name == null || name.trim().isEmpty()) {
                return text;
            }
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<AccessibleElement> getChildren() {
            return children;
        }

        public void setChildren(List<AccessibleElement> children) {
            this.children = children;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }

        @Override
        public String toString() {
            return "AccessibleElement{" +
                   "ref='" + ref + '\'' +
                   ", role='" + role + '\'' +
                   ", name='" + getName() + '\'' +
                   (children != null && !children.isEmpty() ? ", childrenCount=" + children.size() : "") +
                   '}';
        }
    }

    @Override
    public String toString() {
        return "SnapshotData{" +
               "url='" + url + '\'' +
               ", title='" + title + '\'' +
               ", accessibleTreeSize=" + (accessibleTree != null ? accessibleTree.size() : "null") +
               '}';
    }
}
