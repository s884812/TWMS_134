package tools.data.output;

/**
 * Provides an interface to an output stream of bytes.
 * 
 * @author Frz
 * @since Revision 323
 * @version 1.0
 */
public interface ByteOutputStream {

    /**
     * Writes a byte to the stream.
     *
     * @param b The byte to write.
     */
    void writeByte(byte b);
}
