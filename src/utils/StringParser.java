package utils;

import java.util.Locale;
import java.util.Optional;

public class StringParser {
    private final String string;

    private int index;

    public StringParser(String string) {
        this.string = string;
    }

    public char peek() {
        return this.end() ? 0 : this.string.charAt(this.index);
    }

    public char peek(int offset) {
        return this.index + offset >= this.string.length() ? 0 : this.string.charAt(this.index + offset);
    }

    public char next() {
        return this.end() ? 0 : this.string.charAt(this.index++);
    }

    public int pos() {
        return this.index;
    }

    public void pos(int i) {
        this.index = i;
    }

    public boolean skipWhitespace() {
        boolean skipped = false;
        while (!this.end() && Character.isWhitespace(this.string.charAt(index))) {
            index++;
            skipped = true;
        }
        return skipped;
    }

    public boolean skipComment() {
        if (this.peek() == '/' && this.peek(1) == '/') {
            while (this.peek() != '\n' && !this.end()) {
                this.next();
            }
            return true;
        }
        return false;
    }

    public void skipWhitespaceAndComments() {
        boolean hasModified = true;

        while (hasModified) {
            hasModified = skipWhitespace() || skipComment();
        }
    }

    public String readIdentifier() {
        var b = new StringBuilder();

        if (!Character.isJavaIdentifierStart(peek())) {
            throw new RuntimeException("Invalid data!");
        }

        while (Character.isJavaIdentifierPart(this.peek())) {
            b.append(this.next());
        }

        return b.toString();
    }

    public String readLowerIdentifier() {
        var id = readIdentifier();
        return id != null ? id.toLowerCase(Locale.ROOT) : null;
    }

    public void expect(char c) {
        if (this.peek() != c) {
            //System.out.println(this.string.substring(0, this.index));
            //System.out.println("/\\".repeat(64));
            //System.out.println(this.string.substring(this.index));
            throw new RuntimeException("Invalid data! Expected '" + c + "' got '" + peek() + "'!");
        }
        this.next();
    }

    public String readString() {
        var b = new StringBuilder();

        if (peek() != '"') {
            return readIdentifier();
        }
        this.next();

        while (!this.end() && this.peek() != '"') {
            b.append(this.next());
        }
        this.next();

        return b.toString();
    }

    public Optional<String> readOptionalString(String optionalValue) {
        if (peek() != '"') {
            var id = readIdentifier();
            return id.equalsIgnoreCase(optionalValue) ? Optional.empty() : Optional.of(id);
        }
        var b = new StringBuilder();

        while (!this.end() && this.peek() != '"') {
            b.append(this.next());
        }
        this.next();

        return b.toString().equalsIgnoreCase(optionalValue) ? Optional.empty() : Optional.of(b.toString());
    }

    public boolean readBoolean() {
       return Boolean.parseBoolean(this.readLowerIdentifier());
    }

    public int readInt() {
        if (peek() == '"') {
            return Integer.parseInt(readString());
        }
        var b = new StringBuilder();

        while (!this.end() && Character.isDigit(this.peek()) || this.peek() == '_') {
            b.append(this.next());
        }

        return Integer.parseInt(b.toString());
    }

    public boolean end() {
        return this.string.length() <= this.index;
    }

    public void skipUntil(char c) {
        while (!this.end() && this.peek() != c) {
            this.next();
        }
    }
}
