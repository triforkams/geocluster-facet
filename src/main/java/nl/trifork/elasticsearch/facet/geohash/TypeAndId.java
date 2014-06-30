package nl.trifork.elasticsearch.facet.geohash;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * Unique identifier of a document within an index
 */
public class TypeAndId {
    private final String type;
    private final String id;

    public TypeAndId(String type, String id) {
        this.type = type;
        this.id = id;
    }

    public String type() {
        return type;
    }

    public String id() {
        return id;
    }

    public void writeTo(StreamOutput out) throws IOException {

        out.writeString(type);
        out.writeString(id);
    }

    public static TypeAndId readFrom(StreamInput in) throws IOException {

        return new TypeAndId(in.readString(), in.readString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeAndId typeAndId = (TypeAndId) o;

        if (!id.equals(typeAndId.id)) return false;
        if (!type.equals(typeAndId.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }
}
