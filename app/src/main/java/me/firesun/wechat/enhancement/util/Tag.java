package me.firesun.wechat.enhancement.util;

import java.util.ArrayList;
import java.util.HashMap;

public class Tag {

    private String mPath;
    private String mName;
    private ArrayList<Tag> mChildren = new ArrayList<>();
    private String mContent;

    Tag(String path, String name) {
        mPath = path;
        mName = name;
    }

    void addChild(Tag tag) {
        mChildren.add(tag);
    }

    String getName() {
        return mName;
    }

    String getContent() {
        return mContent;
    }

    void setContent(String content) {
        boolean hasContent = false;
        if (content != null) {
            for (int i = 0; i < content.length(); ++i) {
                char c = content.charAt(i);
                if ((c != ' ') && (c != '\n')) {
                    hasContent = true;
                    break;
                }
            }
        }
        if (hasContent) {
            mContent = content;
        }
    }

    ArrayList<Tag> getChildren() {
        return mChildren;
    }

    boolean hasChildren() {
        return (mChildren.size() > 0);
    }

    int getChildrenCount() {
        return mChildren.size();
    }

    Tag getChild(int index) {
        if ((index >= 0) && (index < mChildren.size())) {
            return mChildren.get(index);
        }
        return null;
    }

    HashMap<String, ArrayList<Tag>> getGroupedElements() {
        HashMap<String, ArrayList<Tag>> groups = new HashMap<>();
        for (Tag child : mChildren) {
            String key = child.getName();
            ArrayList<Tag> group = groups.get(key);
            if (group == null) {
                group = new ArrayList<>();
                groups.put(key, group);
            }
            group.add(child);
        }
        return groups;
    }

    String getPath() {
        return mPath;
    }

    @Override
    public String toString() {
        return "Tag: " + mName + ", " + mChildren.size() + " children, Content: " + mContent;
    }
}