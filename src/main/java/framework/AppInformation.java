package framework;

import deskfileclient.AppCommand;
import framework.rasphandling.RaspHeader;

import java.nio.ByteBuffer;

public class AppInformation
{
    private AppCommand commandInf;
    private UploadControl controlTag;
    // If a file, identifies what file the data belongs to.
    // 0 is the default if the message does not contain file data.
    private int fileIdentifier;
    // If a file, identifies the segment of the file being sent.
    // 0 is the default if the message does not contain file data.
    private int fileCounter;
    // The
    private byte[] data;

    public enum AppField {

        // Fields of the header have their location and length assigned to them here.
        // Mind that the header length is based the location and length of FILE_COUNT.
        COMMAND(0, 1),
        TAG(1,1),
        FILE_N(2, 4),
        FILE_COUNT(6, 4);

        private final int loc;
        private final int length;

        AppField(int loc, int length) {
            this.loc = loc;
            this.length = length;
        }

        public int getFieldLoc() {
            return loc;
        }
        public int getFieldLength() {
            return length;
        }

    }

    /**
     * The information to be put into the payload of a RaspPacket.
     * @param command A command to be sent to the server.
     * @param data the data to be processed.
     */
    public AppInformation (AppCommand command, UploadControl controlTag, int fileIdentifier, int fileCounter, byte[] data) {
        this.commandInf = command;
        this.controlTag = controlTag;
        this.fileIdentifier = fileIdentifier;
        this.fileCounter = fileCounter;
        this.data = data;
    }

    public int getLength() {
        return AppField.FILE_COUNT.getFieldLoc() + AppField.FILE_COUNT.getFieldLength();
    }

    private byte[] getAppHeader() {
        ByteBuffer fieldBuf = ByteBuffer.allocate(getLength());
        fieldBuf.put(AppField.COMMAND.getFieldLoc(), (byte) this.commandInf.getCommand());
        fieldBuf.put(AppField.TAG.getFieldLoc(), (byte) this.controlTag.getTag());
        fieldBuf.putInt(AppField.FILE_N.getFieldLoc(), this.fileIdentifier);
        fieldBuf.putInt(AppField.FILE_COUNT.getFieldLoc(), this.fileCounter);
        return fieldBuf.array();
    }

    public byte[] getAppInformation() {
        byte[] appInf = new byte[getLength() + this.data.length];
        System.arraycopy(getAppHeader(),0, appInf,0, RaspHeader.getLength());
        System.arraycopy(this.data,0, appInf, RaspHeader.getLength(), this.data.length);
        return appInf;
    }


    public AppCommand getCommandInf() {
        return commandInf;
    }

    public UploadControl getControlTag() {
        return controlTag;
    }

    public byte[] getData() {
        return data;
    }

    public int getFileIdentifier() {
        return fileIdentifier;
    }

    public int getFileCounter() {
        return fileCounter;
    }
}
