/*
 * Copyright 2011 Splunk, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.splunk.atom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AtomEntry extends AtomObject {
    public String published;
    public Map content;

    static AtomEntry create() {
        return new AtomEntry();
    }

    static AtomEntry create(Element element) {
        AtomEntry entry = AtomEntry.create();
        entry.load(element);
        return entry;
    }

    @Override void init(Element element) {
        String name = element.getTagName();
        if (name.equals("published")) {
            this.published = element.getTextContent().trim();
        }
        else if (name.equals("content")) {
            this.content = parseContent(element);
        }
        else {
            super.init(element);
        }
    }

    // Return a filtered list of child Element nodes.
    static ArrayList<Element> getChildElements(Element element) {
        ArrayList<Element> result = new ArrayList<Element>();
        for (Node child = element.getFirstChild();
             child != null;
             child = child.getNextSibling())
            if (child.getNodeType() == Node.ELEMENT_NODE)
                result.add((Element)child);
        return result;
    }

    // Parse the <content> element of an Atom entry.
    Map parseContent(Element element) {
        assert(element.getTagName().equals("content"));

        Map content = null;

        List<Element> children = getChildElements(element);

        int count = children.size();

        // Expect content to be empty or a single <dict element
        assert(count == 0 || count == 1);

        if (count == 1) {
            Element child = children.get(0);
            content = parseDict(child);
        }

        return content;
    }

    // Parse a <dict> element and return a corresponding Map object.
    Map parseDict(Element element) {
        assert(element.getTagName().equals("s:dict"));

        if (!element.hasChildNodes()) return null;

        List<Element> children = getChildElements(element);

        int count = children.size();
        if (count == 0) return null;

        HashMap result = new HashMap();
        for (Element child : children) {
            assert(child.getTagName().equals("s:key"));
            String key = child.getAttribute("name");
            Object value = parseValue(child);
            if (value != null) result.put(key, value);
        }
        return result;
    }

    // Parse a <list> element and return a corresponding List object.
    List parseList(Element element) {
        assert(element.getTagName().equals("s:list"));

        if (!element.hasChildNodes()) return null;

        List<Element> children = getChildElements(element);

        int count = children.size();
        if (count == 0) return null;

        List result = new ArrayList(count);
        for (Element child : children) {
            assert(child.getTagName().equals("s:item"));
            Object value = parseValue(child);
            if (value != null) result.add(value);
        }
        return result;
    }

    // Parse the value content of a dict/key or a list/item element. The
    // value is either text, a <dict> or a <list> element.
    Object parseValue(Element element) {
        String name = element.getTagName();

        assert(name.equals("s:key") || name.equals("s:item"));

        if (!element.hasChildNodes()) return null;

        List<Element> children = getChildElements(element);

        int count = children.size();

        // If no element children, then it must be a text value
        if (count == 0) return element.getTextContent();

        // If its not a text value, then expect a single child element.
        assert(children.size() == 1);

        Element child = children.get(0);

        name = child.getTagName();

        if (name.equals("s:dict"))
            return parseDict(child);

        if (name.equals("s:list"))
            return parseList(child);

        assert(false); // Unreached
        return null;
    }
}
