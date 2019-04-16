package framework.application;

public enum UploadControl {
    NONE(0), FILENAME(1), LAST(2);

    private final int tag;

    UploadControl(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public static UploadControl fromInt(int value) {
        for (UploadControl tag : UploadControl.values()) {
            if (tag.getTag() == value) {
                return tag;
            }
        }
        return null;
    }
}
