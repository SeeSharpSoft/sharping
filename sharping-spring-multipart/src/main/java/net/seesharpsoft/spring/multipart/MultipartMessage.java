package net.seesharpsoft.spring.multipart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MultipartMessage<T extends MultipartEntity> {
    private List<T> parts = new ArrayList();

    public List<T> getParts() {
        return Collections.unmodifiableList(parts);
    }

    public void setParts(List<T> parts) {
        this.parts = new ArrayList(parts);
    }

    public void addPart(T part) {
        this.parts.add(part);
    }
}
