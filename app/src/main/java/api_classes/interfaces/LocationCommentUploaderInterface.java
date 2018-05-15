package api_classes.interfaces;

/**
 * Created by dmitry on 06.09.17.
 */

public interface LocationCommentUploaderInterface {
    void onSuccessUploadLocationComment();
    void onErrorUploadLocationComment(String errorText);
}
